package com.org.mistralest.controller;

import com.org.mistralest.dto.request.EstimationRequest;
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

import javax.validation.Valid;
import java.io.File;
import java.util.List;
import java.util.Map;
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

    @PostMapping(value = "/get-estimate",  consumes = MediaType.MULTIPART_FORM_DATA_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam("methodology") String methodology,
            @RequestParam(value = "filename", required = false) String filename,
            @RequestParam(value = "history", required = false, defaultValue = "false") boolean history) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "BRD file is required"));
            }


            if (methodology == null || methodology.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "methodology is required (agile|waterfall)"));
            }
            methodology = methodology.trim().toLowerCase();
            if (!"agile".equals(methodology) && !"waterfall".equals(methodology)) {
                return ResponseEntity.badRequest().body(Map.of("error", "unsupported methodology. Use 'agile' or 'waterfall'"));
            }
            String docId = "doc-" + java.time.LocalDate.now().toString().replaceAll("-", "") + "-" + UUID.randomUUID().toString().substring(0,4);
            String finalName = (filename == null || filename.isBlank()) ? file.getOriginalFilename() : filename;

            if (!history) {
                File saved = com.org.mistralest.util.FileUtil.saveMultipartFile(file, uploadDir, docId,methodology);
                System.out.println("Saved File successfully to " + saved.getAbsolutePath());
            }

            EstimationResponse resp = service.generateEstimation(file,methodology);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(Map.of("error", iae.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Estimation failed", "details", ex.getMessage()));
        }
    }


    @GetMapping("/history")
    public ResponseEntity<List<FileItem>> getHistory() {
        return ResponseEntity.ok(fileHistoryService.listFiles());
    }

    @PostMapping(value = "/get-history", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EstimationResponse> getHistory(
            @RequestBody FileItem request) throws Exception {
        EstimationResponse resp = service.generateHistory(request.getAbsolutePath());
        return ResponseEntity.ok(resp);
    }


}
