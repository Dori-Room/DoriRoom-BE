FROM openjdk:17-jdk-slim

# 타임존 환경변수 설정
ENV TZ=Asia/Seoul

# 시스템의 시간대를 서울로 설정
RUN apt-get update && \
    apt-get install -y tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone && \
    dpkg-reconfigure -f noninteractive tzdata

COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]