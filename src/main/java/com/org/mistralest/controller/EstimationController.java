package com.org.mistralest.controller;

import com.org.mistralest.dto.response.EstimationResponse;
import com.org.mistralest.service.EstimationService;
import com.org.mistralest.util.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@RestController
@RequestMapping("/api/estimation")
public class EstimationController {

    private final String uploadDir;


    private final EstimationService service;

    public EstimationController(@Value("${storage.upload-dir}") String uploadDir, EstimationService service) {
        this.uploadDir = uploadDir;
        this.service = service;
    }

    @PostMapping(value = "/get-estimate", consumes = "multipart/form-data",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EstimationResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "filename", required = false)String filename,
            @RequestParam(value = "feedback", required = false)String feedback) throws Exception {
        String docId = "doc-" + java.time.LocalDate.now().toString().replaceAll("-", "") + "-" + UUID.randomUUID().toString().substring(0,4);
        String finalName = (filename == null || filename.isBlank()) ? file.getOriginalFilename() : filename;
        File savedFile = FileUtil.saveMultipartFile(file, uploadDir, docId);
        System.out.println(savedFile.getName()+" File saved successfully.");
        EstimationResponse resp = service.generateEstimation(file, feedback);
        return ResponseEntity.ok(resp);
    }


}
