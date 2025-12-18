package com.org.mistralest.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.stream.Collectors;

public class DocumentTextExtractor {
    public static String extractText(MultipartFile file) throws Exception {
        String name = file.getOriginalFilename().toLowerCase();

        if (name.endsWith(".pdf")) {
            try (PDDocument doc = PDDocument.load(file.getInputStream())) {
                return new PDFTextStripper().getText(doc);
            }
        }

        if (name.endsWith(".docx")) {
            try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
                return doc.getParagraphs()
                        .stream()
                        .map(XWPFParagraph::getText)
                        .collect(Collectors.joining("\n"));
            }
        }

        return new String(file.getBytes());
    }

    public static String extractText(String filePath) throws Exception {
        File file = new File(filePath);
        String name = file.getName().toLowerCase();

        if (name.endsWith(".pdf")) {
            try (PDDocument doc = PDDocument.load(file)) {
                return new PDFTextStripper().getText(doc);
            }
        }

        if (name.endsWith(".docx")) {
            try (XWPFDocument doc = new XWPFDocument(new FileInputStream(file))) {
                return doc.getParagraphs()
                        .stream()
                        .map(XWPFParagraph::getText)
                        .collect(Collectors.joining("\n"));
            }
        }

        return new String(Files.readAllBytes(file.toPath()));
    }

}
