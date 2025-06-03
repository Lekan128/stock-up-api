package com.business.business.s3;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.business.business.auth.AuthService;
import com.business.business.exception.FileUploadException;
import com.business.business.image.ImageCompressionService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Value("${aws.s3.accessKey}")
    private String accessKey;

    @Value("${aws.s3.secretKey}")
    private String secretKey;

    private static final String PRODUCT_IMAGE = "productImages";

    private final AuthService authService;
    
    private AmazonS3 s3Client;

    @PostConstruct
    private void initialize() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(Regions.AF_SOUTH_1)
                .build();
    }

    public FileUploadResponse uploadFile(MultipartFile multipartFile) {
        FileUploadResponse fileUploadResponse = new FileUploadResponse();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String todayDate = dateTimeFormatter.format(LocalDate.now());
        String filePath = "";
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(multipartFile.getContentType());
            objectMetadata.setContentLength(multipartFile.getSize());
            filePath = todayDate+"/"+multipartFile.getOriginalFilename();
            s3Client.putObject(bucketName, filePath, multipartFile.getInputStream(), objectMetadata);
            String url = s3Client.getUrl(bucketName, filePath).toString();
            fileUploadResponse.setFilePath(filePath);
            fileUploadResponse.setUrl(url);
            fileUploadResponse.setDateTime(LocalDateTime.now());
        } catch (IOException e) {
            log.error("Error occurred ==> {}", e.getMessage());
            throw new FileUploadException("Error occurred in file upload ==> "+e.getMessage());
        }
        return fileUploadResponse;
    }

    private final ImageCompressionService imageCompressionService;

    /**
     * Uploads product image to S3 with automatic compression
     *
     * @param multipartFile Image file to upload
     * @param productId Product identifier
     * @return S3 URL of uploaded image
     */
    public String uploadProductImage(MultipartFile multipartFile, UUID productId) {
        String url = "";
        File compressedFile = null;

        try {
            // Prepare metadata
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(multipartFile.getContentType());

            // Determine if compression is needed
            boolean needsCompression = ImageCompressionService.shouldCompressImage(multipartFile);

            // Generate S3 file path
            String filePath = getProductImageFilePath(productId.toString(), multipartFile.getOriginalFilename());

            // Handle compression if needed
            if (needsCompression) {
                compressedFile = imageCompressionService.compressForS3(multipartFile);
                objectMetadata.setContentLength(compressedFile.length());
                s3Client.putObject(bucketName, filePath, new FileInputStream(compressedFile), objectMetadata);
            } else {
                // Upload original file
                objectMetadata.setContentLength(multipartFile.getSize());
                s3Client.putObject(bucketName, filePath, multipartFile.getInputStream(), objectMetadata);
            }

            // Generate S3 URL
            url = s3Client.getUrl(bucketName, filePath).toString();

        } catch (Exception e) {
            log.error("Image upload failed: {}", e.getMessage());
            throw new RuntimeException("Image upload failed: " + e.getMessage());
        } finally {
            // Cleanup temporary file
            if (compressedFile != null && compressedFile.exists()) {
                boolean deleted = compressedFile.delete();
                if (!deleted) {
                    log.warn("Failed to delete temp file: {}", compressedFile.getAbsolutePath());
                }
            }
        }

        return url;
    }

    /**
     * filename: productImage/{@param productIdString}/{@param productIdString}.extension
     * multipartFile.getOriginalFilename()= @fileName
    * */
    private String getProductImageFilePath(String productIdString, String fileName){
        // filename:productImage/{productId}/{productId}.extension
        return PRODUCT_IMAGE + "/" + productIdString + "/" + productIdString + getFileExtension(fileName);
    }

    private static String getFileExtension(String fileName){
        if (fileName == null) return "";
        int lastIndexOfDot = fileName.lastIndexOf(".");
        if (lastIndexOfDot<0) return "";
        return fileName.substring(lastIndexOfDot);
    }
}
