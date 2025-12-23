package com.ecom.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    public String uploadFileToProject(MultipartFile file, String folder)
            throws IOException {

        if (file == null || file.isEmpty()) {
            return "default.jpg";
        }

        String projectPath = System.getProperty("user.dir");

        String uploadDir = projectPath +
                "/src/main/resources/static/img/" + folder;

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);

        Files.copy(file.getInputStream(),
                   filePath,
                   StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }
}
