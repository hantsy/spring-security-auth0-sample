package com.example.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "blog")
@Data
public class BlogProperties {
    private String title = "Nobody's Blog";
    private String description = "Description of Nobody's Blog";
    private String author = "Nobody";
}