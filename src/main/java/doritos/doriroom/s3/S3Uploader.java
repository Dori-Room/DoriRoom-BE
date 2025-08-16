package doritos.doriroom.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import doritos.doriroom.s3.exception.ImageUploadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Uploader {
    private final AmazonS3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp"); // 파일 확장자 지정
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 파일 크기 지정 (10MB)
    private static final int MAX_FILE_COUNT = 5; // 한번에 업로드 가능한 파일 개수 지정


    // 단일 이미지 업로드
    public String uploadFile(MultipartFile multipartFile, String dirName) {
        if (multipartFile == null || multipartFile.isEmpty())   return null; // 파일이 없는 경우 그냥 넘김

        validateFile(multipartFile);
        return uploadToS3(multipartFile, dirName);
    }

    // 다중 이미지 업로드
    public List<String> uploadFiles(List<MultipartFile> multipartFiles, String dirName) {
        if (multipartFiles == null || multipartFiles.isEmpty())     return List.of();
        if (multipartFiles.size() > MAX_FILE_COUNT) {
            throw new ImageUploadException("한 번에 최대 " + MAX_FILE_COUNT+"개의 파일만 업로드할 수 있습니다.");
        }

        // 트랜잭션 수동 롤백 처리
        List<String> uploadedFiles = new ArrayList<>();

        try {
            for (MultipartFile file : multipartFiles) {
                if (file != null && !file.isEmpty()) {
                    validateFile(file);
                    uploadedFiles.add(uploadToS3(file, dirName)); // 업로드 된 파일의 url 추가
                }
            }
            log.info("다중 파일 업로드 완료: {} 개 파일", uploadedFiles.size());
            return uploadedFiles;
        }  catch (Exception e) {
            log.error("다중 파일 업로드 실패", e);

            uploadedFiles.forEach(file -> { // 업로드된 일부 파일들을 삭제
                try {
                    deleteFile(file);
                    log.info("롤백: 파일 삭제 완료 - {}", file);
                } catch (Exception de){
                    log.warn("롤백 중 파일 삭제 실패: {}", file, de);
                }
            });
            throw e;
        }
    }

    // 단일 이미지 삭제
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank())   return;

        try {
            AmazonS3URI s3Uri = new AmazonS3URI(fileUrl); // URI 파싱
            s3Client.deleteObject(s3Uri.getBucket(), s3Uri.getKey()); // 이미지 삭제
            log.info("S3 파일 삭제 성공: {}", fileUrl);
        } catch (IllegalArgumentException e) {
            log.warn("유효하지 않은 S3 URL 형식입니다: {}", fileUrl);
        } catch (Exception e) {
            log.error("S3 파일 삭제 중 오류가 발생했습니다.", e);
            throw new ImageUploadException("이미지 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 다중 이미지 삭제
    public void deleteFiles(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty())     return;

        fileUrls.forEach(this::deleteFile);
    }


    /* 내부 메서드 */

    // s3에 파일 업로드
    private String uploadToS3(MultipartFile multipartFile, String dirName) {
        String fileName = generateFileName(multipartFile.getOriginalFilename());
        String key = dirName + "/" + fileName;

        // 메타 데이터 추가
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(multipartFile.getContentType());
        metadata.setContentLength(multipartFile.getSize());


        // s3 업로드
        try (InputStream inputStream = multipartFile.getInputStream()) {
            s3Client.putObject(
                    new PutObjectRequest(bucket, key, inputStream, metadata)
                            .withCannedAcl(CannedAccessControlList.PublicRead)  );
            log.info("S3 이미지 업로드 성공: {}", key);
        } catch (IOException e) {
            log.error("S3 파일 업로드 중 IO 에러 발생", e);
            throw new ImageUploadException("파일 업로드에 실패했습니다.");
        }
        return s3Client.getUrl(bucket, key).toString(); // 업로드된 파일의 url
    }

    // 파일 검증
    private void validateFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE ) {
            throw new ImageUploadException("파일 크기가 최대 용량(10MB)을 초과했습니다.");
        }
        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        if (! ALLOWED_EXTENSIONS.contains(extension)){
            throw new ImageUploadException("지원하지 않는 파일 형식입니다. (지원하는 형식: "+ ALLOWED_EXTENSIONS +")");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ImageUploadException("이미지 파일만 업로드 가능합니다.");
        }
    }

    // 고유 파일명 랜덤 생성
    private String generateFileName(String fileName) {
        String extension = getFileExtension(fileName); // 확장자 추출
        return UUID.randomUUID().toString() + "." + extension;
    }

    // 파일 확장자 추출
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty() || !fileName.contains(".")) {
            throw new ImageUploadException("잘못된 파일명입니다.");
        }
        return StringUtils.getFilenameExtension(fileName);
    }

}
