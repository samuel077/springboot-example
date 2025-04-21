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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String fullName;
    private String url;
    private String description;
    private String language;
    private int stars;
}