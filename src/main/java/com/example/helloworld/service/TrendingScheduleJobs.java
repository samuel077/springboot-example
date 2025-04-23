package com.example.helloworld.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrendingScheduleJobs {

    private final GithubTrendingService githubTrendingService;

    @Scheduled(cron = "0 * * * * *") // 每1分鐘執行一次
    public void updateTrendingDaily() {
        System.out.println("🕐 自動排程啟動 → 更新 GitHub Trending 資料");

        githubTrendingService.clearRepoCache();

        githubTrendingService.fetchAndStoreTrendingRepos();

        System.out.println("🕐 自動排程結束");
    }
}
