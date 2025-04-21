package com.example.helloworld.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubRepoItem {
    private String name;
    private String full_name;
    private String html_url;
    private String description;
    private String language;
    private int stargazers_count;
}
