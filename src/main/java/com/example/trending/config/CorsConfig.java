package com.example.trending.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class CorsConfig {

//    @Bean
//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                registry.addMapping("/**")
//                        .allowedOriginPatterns("*") // 支援所有來源 (但不能配合 allowedOrigins("*") + credentials)
//                        .allowedMethods("*")        // GET, POST, PUT, DELETE, OPTIONS...
//                        .allowedHeaders("*")        // 允許所有 headers，包括 Authorization
//                        .allowCredentials(true)     // 允許攜帶 Cookie 或 Authorization
//                        .maxAge(3600);              // 預檢請求快取一小時
//            }
//        };
//    }
}