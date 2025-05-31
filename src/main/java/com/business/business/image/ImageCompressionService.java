package com.business.business.image;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.*;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service
@Slf4j
public class ImageCompressionService {

    // Compression configuration
    private static final long MAX_SIZE_BYTES = 2 * 1024 * 1024;  // 2MB threshold
    private static final int MAX_ITERATIONS = 5;                   // Prevent infinite loops
    private static final float MIN_QUALITY = 0.4f;                 // Minimum quality preservation
    private static final float MIN_SCALE = 0.5f;                   // Smallest dimension reduction

    // Supported formats and their compression capabilities
    private static final Map<String, Boolean> FORMATS_WITH_QUALITY = new HashMap<>();
    static {
        FORMATS_WITH_QUALITY.put("jpg", true);
        FORMATS_WITH_QUALITY.put("jpeg", true);
        FORMATS_WITH_QUALITY.put("webp", true);
        FORMATS_WITH_QUALITY.put("png", false);
        FORMATS_WITH_QUALITY.put("gif", false);
        FORMATS_WITH_QUALITY.put("bmp", false);
    }

    public static boolean shouldCompressImage(MultipartFile multipartFile){
        return multipartFile.getSize() > MAX_SIZE_BYTES;
    }

    /**
     * Compresses images larger than 10MB while preserving quality
     *
     * @param multipartFile Original image file
     * @return Temporary compressed file (caller must delete)
     * @throws IOException If compression fails
     */
    public File compressForS3(MultipartFile multipartFile) throws IOException {
        // 1. CHECK SIZE THRESHOLD - Skip if under limit
        if (!shouldCompressImage(multipartFile)) {
            return null;
        }

        // 2. DETECT IMAGE FORMAT
        String formatName;
        try {
            // Create fresh stream for format detection
            formatName = detectImageFormat(multipartFile.getInputStream());
        } catch (IOException e) {
            throw new IOException("Format detection failed: " + e.getMessage());
        }

        if (!FORMATS_WITH_QUALITY.containsKey(formatName)) {
            throw new IOException("Unsupported image format: " + formatName);
        }

        // 3. READ IMAGE DATA
        BufferedImage sourceImage;
        try (InputStream is = multipartFile.getInputStream()) {
            sourceImage = ImageIO.read(is);
        }

        if (sourceImage == null) {
            throw new IOException("Failed to decode image data");
        }

        // 4. SETUP COMPRESSION PARAMETERS
        float quality = 0.9f;  // Start with high quality
        float scale = 1.0f;     // Start with original dimensions
        boolean supportsQuality = FORMATS_WITH_QUALITY.get(formatName);
        File compressedFile = null;

        // 5. ITERATIVE COMPRESSION LOOP
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            // Create temp file (auto-deleted on JVM exit)
            compressedFile = File.createTempFile("compressed-", "." + formatName);
            compressedFile.deleteOnExit();

            // Calculate scaled dimensions
            int newWidth = (int) (sourceImage.getWidth() * scale);
            int newHeight = (int) (sourceImage.getHeight() * scale);

            // Scale image with high-quality interpolation
            BufferedImage scaledImage = scaleImage(sourceImage, newWidth, newHeight);

            // Apply format-specific compression
            compressImage(scaledImage, compressedFile, formatName, quality);

            // Check if under size limit
            if (compressedFile.length() <= MAX_SIZE_BYTES) {
                return compressedFile;  // Success
            }

            // Adjust parameters for next iteration
            quality = Math.max(MIN_QUALITY, quality * 0.7f);  // Reduce quality
            scale = Math.max(MIN_SCALE, scale * 0.8f);        // Reduce dimensions

            // Reset quality if format doesn't support it
            if (!supportsQuality) quality = 1.0f;
        }

        // Return best attempt after max iterations
        return compressedFile;
    }

    /**
     * Detects image format from file signature (magic numbers)
     *
     * @param is Input stream of image data
     * @return Format name (jpg, png, etc.)
     * @throws IOException If format unrecognized
     */
    /**
     * Detects image format from file signature (magic numbers)
     *
     * @param is Input stream of image data
     * @return Format name (jpg, png, etc.)
     * @throws IOException If format unrecognized
     */
    private String detectImageFormat(InputStream is) throws IOException {
        // Use BufferedInputStream to support mark/reset
        BufferedInputStream bis = new BufferedInputStream(is);
        bis.mark(16);  // Mark the beginning

        try {
            byte[] header = new byte[12];
            int bytesRead = bis.read(header);

            if (bytesRead < 12) {
                throw new IOException("Insufficient data for format detection");
            }

            // JPEG: Starts with FF D8
            if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8) {
                return "jpg";
            }

            // PNG: Starts with 89 50 4E 47
            if (header[0] == (byte) 0x89 && header[1] == 0x50 &&
                    header[2] == 0x4E && header[3] == 0x47) {
                return "png";
            }

            // WebP: Starts with RIFF and WEBP at offset 8
            if (header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F' &&
                    header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
                return "webp";
            }

            throw new IOException("Unrecognized image format");
        } finally {
            try {
                bis.reset();  // Reset to original position
            } catch (IOException e) {
                log.warn("Stream reset failed: {}", e.getMessage());
            }
        }
    }

    /**
     * High-quality image scaling with bicubic interpolation
     *
     * @param source Original image
     * @param width Target width
     * @param height Target height
     * @return Scaled image with maintained aspect ratio
     */
    private BufferedImage scaleImage(BufferedImage source, int width, int height) {
        // Determine image type with transparency support
        int imageType = source.getTransparency() == Transparency.OPAQUE ?
                BufferedImage.TYPE_INT_RGB :
                BufferedImage.TYPE_INT_ARGB;

        BufferedImage scaled = new BufferedImage(width, height, imageType);

        // Configure high-quality rendering
        Graphics2D graphics = scaled.createGraphics();

        // Preserve transparency
        if (scaled.getTransparency() != Transparency.OPAQUE) {
            graphics.setComposite(AlphaComposite.Src);
        }

        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Perform scaling
        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();

        return scaled;
    }

    /**
     * Format-specific image compression
     *
     * @param image Image to compress
     * @param outputFile Target file
     * @param format Format name (jpg, png, etc.)
     * @param quality Compression quality (0.0-1.0)
     */
    private void compressImage(BufferedImage image, File outputFile,
                               String format, float quality) throws IOException {
        try (OutputStream os = new FileOutputStream(outputFile)) {
            // Get appropriate image writer
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
            if (!writers.hasNext()) {
                throw new IOException("No compressor available for: " + format);
            }

            ImageWriter writer = writers.next();

            // Configure compression parameters
            ImageWriteParam params = writer.getDefaultWriteParam();

            // Set quality if supported by format
            if (params.canWriteCompressed()) {
                params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                params.setCompressionQuality(quality);

                // Special handling for formats
                if ("jpg".equalsIgnoreCase(format) || "jpeg".equalsIgnoreCase(format)) {
                    ((JPEGImageWriteParam) params).setOptimizeHuffmanTables(true);
                } else if ("png".equalsIgnoreCase(format)) {
                    params.setCompressionType("Deflate");
                    params.setCompressionQuality(0.5f);  // Doesn't affect size but required
                }
            }

            // Write compressed image
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {
                writer.setOutput(ios);
                writer.write(null, new IIOImage(image, null, null), params);
            }
            writer.dispose();
        }
    }
}