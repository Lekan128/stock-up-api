package com.business.business.s3;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileUploadResponse {
    public String filePath;
    public String url;
    public LocalDateTime dateTime;
}
