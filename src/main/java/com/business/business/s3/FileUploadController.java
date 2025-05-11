package com.business.business.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api/s3")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(MultipartFile file) {
        return new ResponseEntity<>(fileService.uploadFile(file), HttpStatus.OK);
    }

    @PostMapping("/upload/{productId}")
    public ResponseEntity<String> uploadProductImage(
            @RequestBody MultipartFile file,
            @PathVariable UUID productId
    ) {
        return new ResponseEntity<>(fileService.uploadProductImage(file, productId), HttpStatus.OK);
    }
}
