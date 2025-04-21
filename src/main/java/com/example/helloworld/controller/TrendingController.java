package com.example.helloworld.controller;

import com.example.helloworld.db.model.RepoInfo;
import com.example.helloworld.service.GithubTrendingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TrendingController {

    private final GithubTrendingService service;

    @GetMapping("/refresh")
    public String refreshTrending() {
        service.fetchAndStoreTrendingRepos();
        return "OK";
    }

    @GetMapping("/repos")
    public List<RepoInfo> getRepos() {
        return service.getAllRepos();
    }

}
