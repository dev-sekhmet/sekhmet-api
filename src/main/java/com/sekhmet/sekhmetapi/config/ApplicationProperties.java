package com.sekhmet.sekhmetapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Sekhmet Api.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * See {@link tech.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private final S3Properties s3 = new S3Properties();

    public S3Properties getS3() {
        return s3;
    }

    public static class S3Properties {

        private String region;
        private String endpoint;
        private String bucket;

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }
    }
}
