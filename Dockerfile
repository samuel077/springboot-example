FROM eclipse-temurin:21-jdk-alpine

# 建立 app 資料夾
WORKDIR /app

# 把 jar 複製進來
COPY build/libs/helloworld-0.0.1-SNAPSHOT.jar app.jar

# 指定執行 jar
ENTRYPOINT ["java", "-jar", "app.jar"]
