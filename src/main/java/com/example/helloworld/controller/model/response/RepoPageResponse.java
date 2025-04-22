package com.example.helloworld.controller.model.response;


import com.example.helloworld.db.model.RepoInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RepoPageResponse {
    private List<RepoInfo> content;
    private long totalCount;
    private int page;
    private int size;
}
