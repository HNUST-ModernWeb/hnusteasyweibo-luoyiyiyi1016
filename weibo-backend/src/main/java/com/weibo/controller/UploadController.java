package com.weibo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
public class UploadController {

    @Value("${upload.local-path}")
    private String uploadPath;

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) throws IOException {
        File dir = new File(uploadPath);
        if (!dir.exists()) dir.mkdirs();

        String name = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        file.transferTo(new File(dir, name));

        return "/img/" + name;
    }
}
