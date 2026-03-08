package marcomanfrin.atixbackend.tools;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket}")
    private String bucket;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Bean
    public boolean initBucket(MinioClient minioClient) throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
        String policy = """
                {"Version":"2012-10-17","Statement":[{"Effect":"Allow",\
                "Principal":{"AWS":["*"]},"Action":["s3:GetObject"],\
                "Resource":["arn:aws:s3:::%s/*"]}]}
                """.formatted(bucket);
        minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                .bucket(bucket).config(policy).build());
        return true;
    }
}
