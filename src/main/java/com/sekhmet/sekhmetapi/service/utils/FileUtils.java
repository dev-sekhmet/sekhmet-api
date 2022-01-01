package com.sekhmet.sekhmetapi.service.utils;

public class FileUtils {

    public static final String IMAGE = "image";
    public static final String VIDEO = "video";
    public static final String AUDIO = "audio";
    public static final String FILE = "file";
    public static final String KEY_FORMAT = "chat-content/%s/%s/%s";
    public static final String UNDERSCORE = "_";
    public static final String HIPHEN = "-";

    public static String getFileType(String contentType) {
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
}
