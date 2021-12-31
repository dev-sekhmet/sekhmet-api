package com.sekhmet.sekhmetapi.service;

import static com.sekhmet.sekhmetapi.service.utils.FileUtils.*;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.sekhmet.sekhmetapi.service.utils.FileUtils;
import java.io.InputStream;
import java.util.UUID;
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

    public PutResult putMedia(String messageId, MultipartFile file) {
        try {
            InputStream content = file.getInputStream();
            ObjectMetadata meta = new ObjectMetadata();
            String fileName = file.getOriginalFilename();
            MediaType mediaType = null;
            if (MediaTypeFactory.getMediaType(fileName).isPresent()) {
                mediaType = MediaTypeFactory.getMediaType(fileName).get();
                log.debug("spring mediaType {}", mediaType);
            }
            assert mediaType != null;
            String fileType = FileUtils.getFileType(mediaType.toString());

            meta.setContentLength(content.available());
            meta.setContentType(mediaType.toString());
            String key = buildKey(messageId, fileType, file);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, content, meta);

            putObjectRequest = putObjectRequest.withCannedAcl(CannedAccessControlList.AuthenticatedRead);

            PutObjectResult result = amazonS3.putObject(putObjectRequest);
            if (result != null) {
                return new PutResult().key(key).fileType(mediaType.toString());
            }
        } catch (Exception e) {
            log.error("Error uploading file on S3: ", e);
        }

        return null;
    }

    private String buildKey(String messageId, String fileType, MultipartFile file) {
        String name = file.getOriginalFilename();
        assert name != null;
        String fileId = UUID.randomUUID() + HIPHEN + name.replace(" ", UNDERSCORE);
        return buildKey(messageId, fileType, fileId);
    }

    private String buildKey(String messageId, String fileType, String fileId) {
        return String.format(KEY_FORMAT, messageId, fileType, fileId);
    }

    public S3Object getMedia(String messageId, String fileType, String fileId) {
        String key = buildKey(messageId, fileType, fileId);
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
