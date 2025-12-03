package com.org.mistralest.controller;

import com.org.mistralest.dto.response.EstimationResponse;
import com.org.mistralest.dto.response.FileItem;
import com.org.mistralest.service.EstimationService;
import com.org.mistralest.service.FileHistoryService;
import com.org.mistralest.util.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/estimation")
public class EstimationController {

    private final String uploadDir;

    private final FileHistoryService fileHistoryService;
    private final EstimationService service;

    public EstimationController(@Value("${storage.upload-dir}") String uploadDir, FileHistoryService fileHistoryService, EstimationService service) {
        this.uploadDir = uploadDir;
        this.fileHistoryService = fileHistoryService;
        this.service = service;
    }

    @PostMapping(value = "/get-estimate", consumes = "multipart/form-data",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EstimationResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "filename", required = false)String filename,
            @RequestParam(value = "history")Boolean history) throws Exception {
        String docId = "doc-" + java.time.LocalDate.now().toString().replaceAll("-", "") + "-" + UUID.randomUUID().toString().substring(0,4);
        String finalName = (filename == null || filename.isBlank()) ? file.getOriginalFilename() : filename;
        if(!history){
            File savedFile = FileUtil.saveMultipartFile(file, uploadDir, docId);
            System.out.println(savedFile.getName()+" File saved successfully.");
        }

        EstimationResponse resp = service.generateEstimation(file);
        return ResponseEntity.ok(resp);
    }


    @GetMapping("/history")
    public ResponseEntity<List<FileItem>> getHistory() {
        return ResponseEntity.ok(fileHistoryService.listFiles());
    }

}
