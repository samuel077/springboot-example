package com.example.helloworld.service.model;

import lombok.Data;

@Data
public class GithubRepoItem {
    private String name;
    private String full_name;
    private String html_url;
    private String description;
    private String language;
    private int stargazers_count;
}
