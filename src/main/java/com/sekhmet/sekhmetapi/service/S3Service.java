package com.sekhmet.sekhmetapi.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.sekhmet.sekhmetapi.web.rest.MessageResource;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class S3Service {

    public static final String IMAGE = "image";
    public static final String VIDEO = "video";
    public static final String AUDIO = "audio";
    public static final String FILE = "file";
    public static final String KEY_FORMAT = "%s/%s/%s";
    public static final String UNDERSCORE = "_";
    public static final String HIPHEN = "-";
    private final Logger log = LoggerFactory.getLogger(MessageResource.class);
    private final AmazonS3 amazonS3;

    @Value("${application.s3.bucket}")
    private String bucket;

    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public PutResult putMedia(String messageId, MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            ObjectMetadata meta = new ObjectMetadata();
            String fileType = getFolderType(Objects.requireNonNull(file.getContentType()));
            meta.setContentLength(inputStream.available());
            meta.setContentType(file.getContentType());
            String key = buildKey(messageId, fileType, file);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, inputStream, meta);

            putObjectRequest = putObjectRequest.withCannedAcl(CannedAccessControlList.AuthenticatedRead);

            PutObjectResult result = amazonS3.putObject(putObjectRequest);
            if (result != null) {
                return new PutResult().key(key).fileType(fileType);
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

    private String getFolderType(String contentType) {
        if (contentType.startsWith(IMAGE)) {
            return IMAGE;
        } else if (contentType.startsWith(VIDEO)) {
            return VIDEO;
        } else if (contentType.startsWith(AUDIO)) {
            return AUDIO;
        } else {
            return FILE;
        }
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
