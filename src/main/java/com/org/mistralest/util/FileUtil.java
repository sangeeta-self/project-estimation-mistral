package com.org.mistralest.util;

import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;


public class FileUtil {

    public static File saveMultipartFile(MultipartFile file, String uploadDir, String docId) throws Exception {
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
        String fileNme = getFileName(original == null ? "file" : original);
        File saved = Path.of(uploadDir, docId + "_" + fileNme).toFile();

        FileUtils.copyInputStreamToFile(file.getInputStream(), saved);
        return saved;
    }

    private static String getFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
    }


    private static String safeString(String s) {
        return s == null ? "" : s.trim();
    }

}
