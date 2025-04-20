# ----------- 1st Stage: Build Jar using Gradle -----------
FROM gradle:8.5-jdk21 AS build

# 將原始碼複製進去，包含 build.gradle、settings.gradle、src 等
COPY --chown=gradle:gradle . /home/gradle/project

# 切換工作目錄
WORKDIR /home/gradle/project

# Build 可執行 Jar（--no-daemon 減少資源）
RUN gradle build --no-daemon


# ----------- 2nd Stage: Minimal runtime image -----------
FROM eclipse-temurin:21-jre-alpine

# 設定工作目錄
WORKDIR /app

# 複製 build 出來的 Jar
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

# 對外開放 port（預設 Spring Boot 使用 8080）
EXPOSE 8080

# 啟動 Spring Boot 應用程式
ENTRYPOINT ["java", "-jar", "app.jar"]
