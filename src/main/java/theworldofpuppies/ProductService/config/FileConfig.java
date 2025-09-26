package theworldofpuppies.ProductService.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class FileConfig {

    @Value("${access.key.id}")
    private String accessKey;

    @Value("${access.key.secret}")
    private String accessSecret;

    @Value("${s3.region.name}")
    private String region;

    @Bean
    public AmazonS3 getAmazonS3Client() {
        final BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(accessKey, accessSecret);
        return AmazonS3Client.builder()
                .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials))
                .withRegion(region)
                .build();
    }
}
