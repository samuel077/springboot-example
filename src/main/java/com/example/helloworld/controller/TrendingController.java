package com.example.helloworld.controller;

import com.example.helloworld.db.model.RepoInfo;
import com.example.helloworld.service.GithubTrendingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    public Page<RepoInfo> getRepos(@PageableDefault(size = 10, sort = "stars", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.getAllRepos(pageable);
    }

}
