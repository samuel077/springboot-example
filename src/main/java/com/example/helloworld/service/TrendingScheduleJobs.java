package com.example.helloworld.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrendingScheduleJobs {

    private final GithubTrendingService githubTrendingService;

    @Scheduled(cron = "0 0 1 * * *") // 每天凌晨1點
    public void updateTrendingDaily() {
        System.out.println("🕐 自動排程啟動 → 更新 GitHub Trending 資料");

        githubTrendingService.clearRepoCacheAndDB();
        githubTrendingService.fetchAndStoreTrendingRepos();

        System.out.println("🕐 自動排程結束");
    }
}
