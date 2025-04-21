package com.example.helloworld.service;

import com.example.helloworld.db.model.RepoInfo;
import com.example.helloworld.db.repository.RepoInfoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class GithubTrendingService {

    private final RepoInfoRepository repoInfoRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private final String GITHUB_TRENDING_API = "https://github-trending-api.waningflow.com/repositories?since=daily";

    public List<RepoInfo> getAllRepos() {
        return repoInfoRepository.findAll();
    }

    public void fetchAndStoreTrendingRepos() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String json = restTemplate.getForObject(GITHUB_TRENDING_API, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(json);

            List<RepoInfo> repos = StreamSupport.stream(root.spliterator(), false)
                    .map(node -> RepoInfo.builder()
                            .name(node.get("name").asText())
                            .author(node.get("author").asText())
                            .url(node.get("url").asText())
                            .description(node.get("description").asText())
                            .language(node.get("language").asText())
                            .stars(node.get("stars").asInt())
                            .build())
                    .collect(Collectors.toList());

            // 存進 PostgreSQL
            repoInfoRepository.saveAll(repos);

            // 存進 Redis（清除舊的再存新的）
            redisTemplate.delete("trending");
            redisTemplate.opsForList().rightPushAll("trending", repos);

            System.out.println("✅ GitHub trending repos 已寫入 DB & Redis");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
