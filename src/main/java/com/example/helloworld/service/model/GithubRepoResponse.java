package com.example.helloworld.service.model;

import lombok.Data;

import java.util.List;

@Data
public class GithubRepoResponse {
    private int total_count;
    private boolean incomplete_results;
    private List<GithubRepoItem> items;
}
