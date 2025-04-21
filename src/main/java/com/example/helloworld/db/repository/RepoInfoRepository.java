package com.example.helloworld.db.repository;

import com.example.helloworld.db.model.RepoInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepoInfoRepository extends JpaRepository<RepoInfo, Long> {
}

