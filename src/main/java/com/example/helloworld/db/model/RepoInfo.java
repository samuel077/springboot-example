package com.example.helloworld.db.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "repo_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepoInfo {

    @Id
    @Column(length = 512)
    private String fullName;

    @Column(length = 1000)
    private String description;

    private String language;
    private int stars;
    private String name;

    @Column(length = 512)
    private String url;
}