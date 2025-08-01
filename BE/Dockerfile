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
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
COPY --from=build /app/target/*.jar app.jar

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