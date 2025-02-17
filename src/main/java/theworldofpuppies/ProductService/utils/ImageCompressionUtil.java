package theworldofpuppies.ProductService.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;

@Component
public class ImageCompressionUtil {

    public static MultipartFile compressImage(MultipartFile originalImage, long maxSizeMB) throws IOException {
        BufferedImage image = ImageIO.read(originalImage.getInputStream());
        ByteArrayOutputStream compressedImageStream = new ByteArrayOutputStream();

        // Set the initial compression quality
        float quality = 0.9f;

        // Compress the image until it is under the desired size
        while (compressedImageStream.toByteArray().length > maxSizeMB * 1024 * 1024) {
            compressedImageStream.reset();
            ImageIO.write(image, "jpg", compressedImageStream);

            // Reduce the quality gradually to achieve the desired size
            quality -= 0.1f;
            if (quality < 0.1f) {
                throw new IOException("Unable to compress the image below " + maxSizeMB + "MB");
            }
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedImageStream.toByteArray());
        return new MultipartFile() {
            @Override
            public String getName() {
                return originalImage.getName();
            }

            @Override
            public String getOriginalFilename() {
                return originalImage.getOriginalFilename();
            }

            @Override
            public String getContentType() {
                return originalImage.getContentType();
            }

            @Override
            public boolean isEmpty() {
                return compressedImageStream.size() == 0;
            }

            @Override
            public long getSize() {
                return compressedImageStream.size();
            }

            @Override
            public byte[] getBytes() throws IOException {
                return compressedImageStream.toByteArray();
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return inputStream;
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                Files.copy(inputStream, dest.toPath());
            }
        };
    }
}
