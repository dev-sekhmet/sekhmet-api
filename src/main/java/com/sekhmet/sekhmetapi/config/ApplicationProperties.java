package com.sekhmet.sekhmetapi.config;

import lombok.Getter;
import lombok.Setter;
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
    private final SmsProperties sms = new SmsProperties();

    public S3Properties getS3() {
        return s3;
    }

    public SmsProperties getSms() {
        return sms;
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

    @Getter
    @Setter
    public static class SmsProperties {

        private final TwilioPreperties twilio = new TwilioPreperties();
        private String passwordPhoneNumberSecret;

        @Getter
        @Setter
        public static class TwilioPreperties {

            private String accountSid;
            private String authToken;
            private String verifySid;
        }
    }
}
