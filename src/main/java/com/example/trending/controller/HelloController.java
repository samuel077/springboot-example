package com.example.trending.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HelloController {

    @Value("${app.version}")
    private String appVersion;

    @GetMapping("/")
    public String hello() {
        return "Hello World! v: " + appVersion;
    }
}

