package com.example.helloworld.controller;

import com.example.helloworld.controller.model.response.RepoPageResponse;
import com.example.helloworld.db.model.RepoInfo;
import com.example.helloworld.db.repository.RepoInfoRepository;
import com.example.helloworld.service.GithubTrendingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
@Slf4j
public class TrendingController {

    private final GithubTrendingService service;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RepoInfoRepository repoInfoRepository;
    private final ObjectMapper objectMapper;


    @GetMapping("/hello-ci")
    public String ciHint() {
        log.info("[Debug] First logging logging here");
        return "Hello, CI, After changing the logs";
    }

    @GetMapping("/refresh")
    public String refreshTrending() {
        service.fetchAndStoreTrendingRepos();
        return "OK";
    }

    @GetMapping("/manual-clear-data")
    public String manualClearStorage() {
        service.clearRepoCacheAndDB();
        return "Remove all OK";
    }

    @GetMapping("/repos")
    public RepoPageResponse getRepos(@RequestParam int page, @RequestParam int size) {
        String cacheKey = "repos:page:" + page + ":size:" + size;
        log.info("[Debug] cacheKey: {}", cacheKey);

        // 1. 嘗試從 Redis 拿資料
        Object cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            log.info("[Debug] reading data from redis for page: {}", page);
            RepoPageResponse response = objectMapper.convertValue(cached, RepoPageResponse.class);
            return response;
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
        log.info("[Debug] redis miss, get data from DB and cache it");

        return response;
    }

}
