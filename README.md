
# GitHub Trending Aggregator

一個支援定時更新、分頁查詢、Redis 快取的 Spring Boot 專案。從 GitHub Trending 自動抓取熱門 repository，支援查詢、快取與簡易 API 服務，設計目標為模擬中型服務架構，適合面試展示與後端實作練習。

---

## 🛠️ 技術棧

- Spring Boot 3.x
- Spring Data JPA + PostgreSQL
- Redis (Upstash / ElastiCache)
- Spring Scheduler (Cron)
- Docker / Render / AWS（準備轉移）
- (選配) LINE Bot / Swagger / GitHub Actions

---

## 📌 專案架構圖

```
GitHub API → [Scheduled Job / /refresh]
               ↓
        ┌────────────┐
        │ PostgreSQL │ ← /repos (fallback)
        └────────────┘
               ↑
            Redis ← /repos (快取命中)
```

---

## ✅ 已完成功能清單

| 功能模組             | 說明 |
|----------------------|------|
| Trending 抓取        | 呼叫 GitHub Search API 並解析資料 |
| DB 寫入              | 使用 JPA 寫入 PostgreSQL |
| Redis 快取           | 分頁查詢結果寫入 Redis、含 TTL |
| `/repos` API         | 分頁查詢 API，支援從 Redis 讀取 |
| DTO 包裝             | 查詢結果回傳 `content` + `totalCount` 等 |
| 快取自動過期         | 寫入時加 TTL，命中後滑動延長存活時間 |
| 排程任務（cron）     | 每天定時更新 GitHub Trending，更新快取與資料庫 |
| 日誌紀錄             | 支援 log 輸出 cache hit / miss 狀況 |
| Local 測試與 Render 部署 | 使用 Docker + Gradle 完成 local build & 雲端部署 |

---

## 🚧 開發中 / 計畫中功能

- [ ] 使用 @Async 處理 refresh / 排程非同步寫入
- [ ] 移轉部署到 AWS（EC2 / App Runner / Beanstalk）
- [ ] 建立 Redis 快取統計 `/cache/stats` endpoint
- [ ] 增加查詢條件（語言、stars、關鍵字）
- [ ] 整合 LINE Bot（回傳當日熱門 repo）
- [ ] 加上 Swagger UI（自動 API 文件）
- [ ] 增加健康檢查 `/health`
- [ ] 加入 Flyway 做 schema migration
- [ ] GitHub Actions 做 CI/CD 自動部署

---

## 🧪 如何本地啟動

```bash
# 1. build 專案
./gradlew clean build

# 2. 執行應用程式
java -jar build/libs/helloworld-0.0.1-SNAPSHOT.jar
```

使用環境變數：

```
DATABASE_URL=jdbc:postgresql://...（你的 RDS or Render 資訊）
REDIS_URL=rediss://...（Upstash or ElastiCache）
```

---

## ☁️ 部署到 AWS（TODO）

部署選項：

- [ ] EC2 + Docker
- [ ] AWS App Runner（推薦，搭配 GitHub 自動部署）
- [ ] Elastic Beanstalk

需整合項目：

- RDS for PostgreSQL
- ElastiCache for Redis
- IAM / VPC / 記憶體限制 / AutoScaling 等部署參數

---

## 📄 授權

MIT License.
