package com.example.trending.db.repository;

import com.example.trending.db.model.RepoInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepoInfoRepository extends JpaRepository<RepoInfo, Long> {
}

