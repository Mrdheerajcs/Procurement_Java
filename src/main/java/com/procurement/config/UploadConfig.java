package com.procurement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.io.File;

@Configuration
public class UploadConfig {

    @Value("${app.upload.base-dir:C:/uploads}")
    private String baseDir;

    @PostConstruct
    public void init() {
        createDirectory(baseDir);
        createDirectory(baseDir + "/bids");
        createDirectory(baseDir + "/bids/technical");
        createDirectory(baseDir + "/bids/financial");
        createDirectory(baseDir + "/tenders");
        createDirectory(baseDir + "/clarification");
        createDirectory(baseDir + "/mpr");
        createDirectory(baseDir + "/profiles");
        createDirectory(baseDir + "/contracts");
        createDirectory(baseDir + "/contracts/pbg");
    }

    private void createDirectory(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("Created directory: " + path);
            }
        }
    }
}