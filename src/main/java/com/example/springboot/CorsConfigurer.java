package com.example.springboot;

import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class CorsConfigurer implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(org.springframework.web.cors.CorsConfiguration.ALL)
                        .allowedMethods(org.springframework.web.cors.CorsConfiguration.ALL)
                        .allowedHeaders(org.springframework.web.cors.CorsConfiguration.ALL);
    }
}
