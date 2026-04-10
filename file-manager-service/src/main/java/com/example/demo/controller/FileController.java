package com.example.demo.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.service.FileService;

@RestController
@RequestMapping("/files")
@CrossOrigin
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String url = fileService.uploadFile(file);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "url", url
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Upload failed"
            ));
        }
    }

    @GetMapping("/presigned-url")
    public ResponseEntity<?> getPresignedUrl(
            @RequestParam("fileName") String fileName,
            @RequestParam(value = "contentType", defaultValue = "application/octet-stream") String contentType) {
        try {
            String presignedUrl = fileService.generatePresignedUrl(fileName, contentType);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "presignedUrl", presignedUrl,
                    "bucket", "sgp1.digitaloceanspaces.com"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to generate presigned URL"
            ));
        }
    }
}