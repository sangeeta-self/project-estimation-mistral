package com.org.mistralest.controller;

import com.org.mistralest.dto.response.EstimationResponse;
import com.org.mistralest.service.EstimationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/estimation")
public class EstimationController {


    private final EstimationService service;

    public EstimationController(EstimationService service) {
        this.service = service;
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EstimationResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String feedback
    ) throws Exception {
        EstimationResponse resp = service.generateEstimation(file, feedback);
        return ResponseEntity.ok(resp);
    }
}
