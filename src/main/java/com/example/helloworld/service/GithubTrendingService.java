package com.example.helloworld.service;

import com.example.helloworld.db.model.RepoInfo;
import com.example.helloworld.db.repository.RepoInfoRepository;
import com.example.helloworld.service.model.GithubRepoItem;
import com.example.helloworld.service.model.GithubRepoResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GithubTrendingService {

    private final RepoInfoRepository repoInfoRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private final String GITHUB_TRENDING_API = "https://api.github.com/search/repositories?q=stars:>1000&sort=stars&order=desc";

    public void fetchAndStoreTrendingRepos() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String json = restTemplate.getForObject(GITHUB_TRENDING_API, String.class);

            ObjectMapper mapper = new ObjectMapper();
            GithubRepoResponse response = mapper.readValue(json, GithubRepoResponse.class);
            List<GithubRepoItem> items = response.getItems();
            System.out.println("抓到熱門 repo 數量：" + items.size());

            List<RepoInfo> repos = items.stream()
                    .map(item -> RepoInfo.builder()
                            .name(item.getName())
                            .fullName(item.getFull_name())
                            .url(item.getHtml_url())
                            .description(item.getDescription())
                            .language(item.getLanguage())
                            .stars(item.getStargazers_count())
                            .build())
                    .collect(Collectors.toList());

            // 存進 PostgreSQL
            repoInfoRepository.saveAll(repos);

            System.out.println("✅ 寫入 DB 成功，筆數：" + repos.size());

            // 存進 Redis（清除舊的再存新的）
            redisTemplate.delete("trending");
            redisTemplate.opsForList().rightPushAll("trending", repos);

            System.out.println("✅ GitHub trending repos 已寫入 DB & Redis");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearRepoCacheAndDB() {
        repoInfoRepository.deleteAll();
        Set<String> keys = redisTemplate.keys("repos:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            System.out.println("🧹 清除 Redis 快取，共刪除 " + keys.size() + " 筆");
        }
    }

}
