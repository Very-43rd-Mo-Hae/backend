package com.very.relink.chat.infrastructure.s3;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chat.storage.s3")
public record S3ChatStorageProperties(
        String bucket,
        String region,
        String publicBaseUrl,
        long uploadExpiresIn
) {
    public S3ChatStorageProperties {
        if (bucket == null || bucket.isBlank()) {
            bucket = "";
        }
        if (region == null || region.isBlank()) {
            region = "ap-northeast-2";
        }
        if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
            publicBaseUrl = bucket.isBlank()
                    ? ""
                    : "https://%s.s3.%s.amazonaws.com".formatted(bucket, region);
        }
        if (uploadExpiresIn <= 0) {
            uploadExpiresIn = 300;
        }
    }
}
