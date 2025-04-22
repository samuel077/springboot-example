package com.example.helloworld.controller;

import com.example.helloworld.controller.model.response.RepoPageResponse;
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
    private final RedisTemplate<String, Object> redisTemplate;
    private final RepoInfoRepository repoInfoRepository;

    @GetMapping("/refresh")
    public String refreshTrending() {
        service.fetchAndStoreTrendingRepos();
        return "OK";
    }

    @GetMapping("/repos")
    public RepoPageResponse getRepos(@RequestParam int page, @RequestParam int size) {
        String cacheKey = "repos:page:" + page + ":size:" + size;

        // 1. 嘗試從 Redis 拿資料
        RepoPageResponse cached = (RepoPageResponse) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            System.out.println("🔥 從 Redis 快取中回傳 page " + page);
            return cached;
        }

        // 2. 查 DB + 寫入 Redis
        Pageable pageable = PageRequest.of(page, size, Sort.by("stars").descending());
        Page<RepoInfo> pageData = repoInfoRepository.findAll(pageable);

        RepoPageResponse response = new RepoPageResponse(
                pageData.getContent(),
                pageData.getTotalElements(),
                page,
                size
        );

        redisTemplate.opsForValue().set(cacheKey, response, Duration.ofMinutes(30));
        System.out.println("💾 Redis miss → 從 DB 查資料並快取 page " + page);

        return response;
    }

}
