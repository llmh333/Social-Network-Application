## --- Giai đoạn 1: Build ứng dụng (Tối ưu Layer Caching) ---
## Bắt đầu với image maven
#FROM maven:3.9.10-amazoncorretto-17 AS build
#
## Tạo thư mục làm việc
#WORKDIR /app
#
## 1. Chỉ copy file pom.xml trước
## Điều này tận dụng Docker layer caching. Docker sẽ chỉ tải lại dependencies khi pom.xml thay đổi.
#COPY pom.xml .
#
## 2. Tải tất cả dependencies về
#RUN mvn dependency:go-offline
#
## 3. Copy toàn bộ mã nguồn
#COPY src ./src
#
## 4. Build ứng dụng (dependencies đã được tải sẵn)
#RUN mvn package -DskipTests
#
#
## --- Giai đoạn 2: Tạo image chạy (Siêu nhẹ và giới hạn RAM) ---
## Sử dụng image JRE (Java Runtime Environment) trên nền Alpine -> Nhỏ và nhẹ hơn rất nhiều
#FROM amazoncorretto:17.0.15-al2023-headless
#
## Tạo thư mục làm việc
#WORKDIR /app
#
## Sao chép file JAR đã build từ giai đoạn trước
#COPY --from=build /app/target/*.jar app.jar
#
## QUAN TRỌNG: Giới hạn bộ nhớ cho JVM
## -Xms128m: Bộ nhớ heap ban đầu là 128MB
## -Xmx384m: Bộ nhớ heap tối đa là 384MB
## Các giá trị này nên nhỏ hơn giới hạn memory bạn đặt trong docker-compose.yml
#ENTRYPOINT ["java", "-Xms128m", "-Xmx384m", "-jar", "app.jar"]
#
## Mở cổng 8080
#EXPOSE 8080
# --- Giai đoạn 1: Build ứng dụng ---
FROM maven:3.9.10-amazoncorretto-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests -q


# --- Giai đoạn 2: Runtime tối ưu ---
FROM openjdk:17-alpine

# Cài đặt FFmpeg (chỉ runtime, không dev packages)
RUN apk update && apk add --no-cache ffmpeg

WORKDIR /app

# Copy JAR và set ownership
COPY --from=build --chown=appuser:appuser /app/target/*.jar app.jar

# JVM flags tối ưu cho container
ENTRYPOINT ["java", \
    "-server", \
    "-XX:+UseG1GC", \
    "-XX:+UseStringDeduplication", \
    "-XX:+OptimizeStringConcat", \
    "-Xms128m", \
    "-Xmx384m", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]

EXPOSE 8080