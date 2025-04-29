package com.example.trending.service;

import com.example.trending.db.model.RepoInfo;
import com.example.trending.db.repository.RepoInfoRepository;
import com.example.trending.service.model.GithubRepoItem;
import com.example.trending.service.model.GithubRepoResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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

            log.info("Write DB success, count: {}", repos.size());

            // 存進 Redis（清除舊的再存新的）
            redisTemplate.delete("trending");
            redisTemplate.opsForList().rightPushAll("trending", repos);

            log.info("[Debug] github trending repo write to DB and Redis successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearRepoCacheAndDB() {
        repoInfoRepository.deleteAll();
        Set<String> keys = redisTemplate.keys("repos:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("[Debug] remove redis successfully, total key count: {}", keys.size());
        }
    }

}
