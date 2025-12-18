package com.org.mistralest.service;

import com.org.mistralest.dto.response.FileItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
                .map(f -> {
                    String name = f.getName();
                    String date = extractDateFromFileName(name);
                    String method = extractMethodologyFromFileName(name);
                    String title = extractTitleFromFileName(name);
                    return new FileItem(name, f.getAbsolutePath(), date, title, method);
                })
                .sorted((a, b) -> {
                    try {
                        LocalDate da = LocalDate.parse(a.getUploadedDate(), DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                        LocalDate db = LocalDate.parse(b.getUploadedDate(), DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                        return db.compareTo(da); // NEWEST first
                    } catch (Exception e) {
                        return 0; // fallback if date fails
                    }
                })
                .collect(Collectors.toList());
    }


    public String extractMethodologyFromFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) return "unknown";

        String[] parts = fileName.split("_");
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i].trim();
            if (p.isEmpty()) continue;
            if (p.toLowerCase().matches("^doc-\\d{8}.*")) {
                continue; // skip doc-id-like parts
            }
            return p.toLowerCase();
        }

        // fallback: if no non-doc part found but there are >=3 parts, pick the middle one (best-effort)
        if (parts.length >= 3) return parts[1].toLowerCase();

        return "unknown";
    }

    public String getFileNameFromPath(String filePath) {
        if (filePath == null) return "";
        return new File(filePath).getName();   // works on Windows + Linux
    }


    private String extractTitleFromFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) return fileName;
        String nameWithoutExt = fileName.replaceFirst("\\.[^.]+$", "");
        String[] parts = nameWithoutExt.split("_");

        int methodIndex = -1;
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i].trim();
            if (p.isEmpty()) continue;
            if (p.toLowerCase().matches("^doc-\\d{8}.*")) {
                continue;
            }
            methodIndex = i;
            break;
        }
        if (methodIndex >= 0 && methodIndex < parts.length - 1) {
            String[] titleParts = Arrays.copyOfRange(parts, methodIndex + 1, parts.length);
            return String.join("_", titleParts);
        }

        String[] hy = nameWithoutExt.split("-", 3);
        if (hy.length == 3) {
            return hy[2];
        }

        return nameWithoutExt; // fallback
    }



    private String extractDateFromFileName(String fileName) {
        String[] parts = fileName.split("-");
        if (parts.length > 1) {
            String datePart = parts[1];

            if (datePart.matches("\\d{8}")) {
                try {
                    DateTimeFormatter input = DateTimeFormatter.ofPattern("yyyyMMdd");
                    DateTimeFormatter output = DateTimeFormatter.ofPattern("yyyy/MM/dd");

                    LocalDate date = LocalDate.parse(datePart, input);
                    return date.format(output);
                } catch (Exception ignored) {}
            }
        }

        return "Unknown";
    }


}
