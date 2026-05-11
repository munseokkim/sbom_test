package com.derabbit.seolstudy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileConfig {

    @Value("${spring.file.upload-dir}")
    private String fileDir;

    @Bean
    public String fileUploadDir() {
        return fileDir;
    }
}
