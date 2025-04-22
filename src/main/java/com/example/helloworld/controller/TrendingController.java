package com.example.helloworld.controller;

import com.example.helloworld.db.model.RepoInfo;
import com.example.helloworld.db.repository.RepoInfoRepository;
import com.example.helloworld.service.GithubTrendingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class TrendingController {

    private final GithubTrendingService service;
    private final RedisTemplate redisTemplate;
    private final RepoInfoRepository repoInfoRepository;

    @GetMapping("/refresh")
    public String refreshTrending() {
        service.fetchAndStoreTrendingRepos();
        return "OK";
    }

    @GetMapping("/repos")
    public List<RepoInfo> getRepos(@RequestParam int page, @RequestParam int size) {
        String cacheKey = "repos:page:" + page + ":size:" + size;

        List<RepoInfo> cached = (List<RepoInfo>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            System.out.println("🔥 Redis cache hit!");
            return cached;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("stars").descending());
        List<RepoInfo> data = repoInfoRepository.findAll(pageable).getContent();

        redisTemplate.opsForValue().set(cacheKey, data, Duration.ofHours(24)); // 寫入 Redis，1 小時有效
        System.out.println("💾 Cache miss → 從 DB 拿資料並寫入 Redis");
        return data;
    }

}
