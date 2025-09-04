# ================= STAGE 1: Build =================
# Gradle과 JDK를 사용하여 애플리케이션을 빌드하는 단계
FROM openjdk:17-jdk AS builder

# 작업 디렉토리 설정
WORKDIR /workspace

# Gradle Wrapper와 빌드 스크립트 복사
COPY gradlew .
COPY gradle gradle

# build.gradle 파일 복사
COPY build.gradle .
# settings.gradle 파일이 있다면 그것도 복사합니다.
# COPY settings.gradle .

# 소스코드 복사
COPY src src

# Gradle 빌드 실행
RUN ./gradlew build -x test

# ================= STAGE 2: Final Image =================
# 실제 실행에 필요한 최소한의 환경으로 이미지를 만드는 단계
FROM openjdk:17-jre-alpine

# 타임존 환경변수 설정
ENV TZ=Asia/Seoul

# 시스템의 시간대를 서울로 설정
RUN apk add --no-cache tzdata

# 빌드 스테이지(builder)에서 생성된 JAR 파일만 복사
COPY --from=builder /workspace/build/libs/*.jar app.jar

# JVM 메모리 옵션 추가!
ENTRYPOINT ["java", "-Xms512m", "-Xmx512m", "-jar", "/app.jar"]