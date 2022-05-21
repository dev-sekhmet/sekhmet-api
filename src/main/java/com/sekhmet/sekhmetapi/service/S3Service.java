package com.sekhmet.sekhmetapi.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class S3Service {

    private final Logger log = LoggerFactory.getLogger(S3Service.class);
    private final AmazonS3 amazonS3;

    @Value("${application.s3.bucket}")
    private String bucket;

    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public PutResult putMedia(String key, MultipartFile file) {
        try {
            InputStream content = file.getInputStream();
            ObjectMetadata meta = new ObjectMetadata();
            String fileName = file.getOriginalFilename();
            MediaType mediaType = null;
            if (MediaTypeFactory.getMediaType(fileName).isPresent()) {
                mediaType = MediaTypeFactory.getMediaType(fileName).get();
                log.debug("spring mediaType {}", mediaType);
            } else {
                mediaType = (MediaType.parseMediaTypes(file.getContentType()).get(0));
            }
            meta.setContentLength(content.available());
            meta.setContentType(mediaType.toString());
            String completeKey = key + "." + mediaType.getSubtype();
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, completeKey, content, meta);

            putObjectRequest = putObjectRequest.withCannedAcl(CannedAccessControlList.AuthenticatedRead);

            PutObjectResult result = amazonS3.putObject(putObjectRequest);
            if (result != null) {
                return new PutResult().key(completeKey).fileType(mediaType.toString());
            }
        } catch (Exception e) {
            log.error("Error uploading file on S3: ", e);
        }

        return null;
    }

    public S3Object getMedia(String key) {
        return amazonS3.getObject(bucket, key);
    }

    public static class PutResult {

        private String key;
        private String fileType;

        public String getKey() {
            return key;
        }

        public String getFileType() {
            return fileType;
        }

        public PutResult key(String key) {
            this.key = key;
            return this;
        }

        public PutResult fileType(String fileType) {
            this.fileType = fileType;
            return this;
        }
    }
}
