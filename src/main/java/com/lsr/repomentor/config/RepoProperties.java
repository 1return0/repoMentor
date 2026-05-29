package com.lsr.repomentor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "repomentor.repo")
public class RepoProperties {
    private String basePath;
}
