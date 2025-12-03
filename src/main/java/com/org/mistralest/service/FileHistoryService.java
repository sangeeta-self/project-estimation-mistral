package com.org.mistralest.service;

import com.org.mistralest.dto.response.FileItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileHistoryService {

    private final File uploadDir;

    public FileHistoryService(@Value("${storage.upload-dir}") String uploadDir) {
        this.uploadDir = new File(uploadDir);
    }

    public List<FileItem> listFiles() {

        if (!uploadDir.exists() || !uploadDir.isDirectory()) {
            return List.of();
        }

        File[] files = uploadDir.listFiles();
        if (files == null) return List.of();

        return Arrays.stream(files)
                .filter(File::isFile)
                .map(f -> new FileItem(f.getName(), f.getAbsolutePath()))
                .collect(Collectors.toList());
    }

}
