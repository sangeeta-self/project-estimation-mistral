package com.org.mistralest.util;

import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;


public class FileUtil {

    public static File saveMultipartFile(MultipartFile file, String uploadDir, String docId, String methodology) throws Exception {
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Failed to create directory: " + uploadDir);
            }
        }
        if (!dir.isDirectory()) {
            throw new IOException("Upload path is not a directory: " + uploadDir);
        }
        String original = file.getOriginalFilename();
        String name = getFileName(original == null ? "file" : original);
        // sanitize methodology
        String methodPart = (methodology == null || methodology.isBlank())
                ? "unknown"
                : methodology.toLowerCase().replaceAll("[^a-zA-Z0-9]", "_");
        // final filename
        String finalName = docId + "_" + methodPart + "_" + name;
        File saved = Path.of(uploadDir, docId + "_" + finalName).toFile();

        FileUtils.copyInputStreamToFile(file.getInputStream(), saved);
        return saved;
    }

    private static String getFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
    }



}
