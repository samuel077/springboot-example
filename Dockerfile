# -------- 1st Stage --------
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# 複製全部檔案（包含 gradlew、wrapper）
COPY . .

# 確保 gradlew 有執行權限
RUN chmod +x ./gradlew

# 執行 build
RUN ./gradlew build --no-daemon


# -------- 2nd Stage --------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
