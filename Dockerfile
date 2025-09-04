# ================= STAGE 1: Build =================
# Gradle과 JDK를 사용하여 애플리케이션을 빌드하는 단계
FROM eclipse-temurin:17-jdk-jammy AS builder

# gradlew 실행에 필요한 필수 패키지를 설치
RUN apt-get update && apt-get install -y findutils && rm -rf /var/lib/apt/lists/*

# 작업 디렉토리 설정
WORKDIR /workspace

# 빌드 캐시 효율성을 위해 의존성 파일부터 복사
COPY build.gradle gradlew ./
COPY gradle ./gradle
COPY settings.gradle .

# 소스코드 복사
COPY src src

# Gradle 빌드를 실행하여 JAR 파일을 생성합니다.
RUN ./gradlew build -x test

# ================= STAGE 2: Final Image =================
# 실제 실행에 필요한 최소한의 JRE 환경으로 최종 이미지를 만듭니다.
FROM eclipse-temurin:17-jre-jammy

# 타임존 환경변수 설정
ENV TZ=Asia/Seoul

# 시스템의 시간대를 서울로 설정합니다.
RUN apt-get update && \
    apt-get install -y tzdata && \
    rm -rf /var/lib/apt/lists/*

# 빌드 스테이지(builder)에서 생성된 JAR 파일만 안전하게 복사해옵니다.
COPY --from=builder /workspace/build/libs/*.jar app.jar

# 컨테이너의 안정성을 위해 JVM 힙 메모리 옵션을 명시합니다.
ENTRYPOINT ["java", "-Xms512m", "-Xmx512m", "-jar", "/app.jar"]