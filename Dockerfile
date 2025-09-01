FROM openjdk:17-jdk

# 시간대 데이터 패키지 설치 (Debian/Ubuntu 기반)
RUN apt-get update && apt-get install -y tzdata

# 환경 변수 및 시간대 설정
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]