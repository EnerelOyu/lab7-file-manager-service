package com.example.demo.service;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

@Service
public class FileService {

    private final AmazonS3 amazonS3;

    @Value("${s3.bucket.name}")
    private String bucketName;

    @Value("${s3.endpoint}")
    private String endpoint;

    public FileService(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String fileName = UUID.randomUUID() + "_" + originalName;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        PutObjectRequest request = new PutObjectRequest(
                bucketName,
                fileName,
                file.getInputStream(),
                metadata
        ).withCannedAcl(CannedAccessControlList.PublicRead);

        amazonS3.putObject(request);

        return "https://" + bucketName + ".sgp1.digitaloceanspaces.com/" + fileName;
    }

    //Presigned URL авдаг. Frontend нь энэ URL ашиглан шууд S3 руу файл upload хийх боломжтой.
    public String generatePresignedUrl(String fileName, String contentType) {
        // Файлын нэр үүсгэх
        String uniqueFileName = UUID.randomUUID() + "_" + fileName;

        // 15 минут хүчинтэй URL үүсгэх
        Date expiration = new Date(System.currentTimeMillis() + (15 * 60 * 1000));

        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, uniqueFileName)
                .withMethod(HttpMethod.PUT)
                .withExpiration(expiration)
                .withContentType(contentType);

        URL presignedUrl = amazonS3.generatePresignedUrl(request);

        return presignedUrl.toString();
    }
}