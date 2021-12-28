package com.sekhmet.sekhmetapi.config;

import java.time.*;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.elasticsearch.config.ElasticsearchConfigurationSupport;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;

@Configuration
public class ElasticsearchConfiguration extends ElasticsearchConfigurationSupport {

    @Bean
    @Override
    public ElasticsearchCustomConversions elasticsearchCustomConversions() {
        return new ElasticsearchCustomConversions(
            Arrays.asList(
                new ZonedDateTimeWritingConverter(),
                new ZonedDateTimeReadingConverter(),
                new InstantWritingConverter(),
                new InstantReadingConverter(),
                new LocalDateWritingConverter(),
                new LocalDateTimeReadingConverter(),
                new LocalDateTimeWritingConverter(),
                new LocalDateReadingConverter()
            )
        );
    }

    @WritingConverter
    static class ZonedDateTimeWritingConverter implements Converter<ZonedDateTime, String> {

        @Override
        public String convert(ZonedDateTime source) {
            if (source == null) {
                return null;
            }
            return source.toInstant().toString();
        }
    }

    @ReadingConverter
    static class ZonedDateTimeReadingConverter implements Converter<String, ZonedDateTime> {

        @Override
        public ZonedDateTime convert(String source) {
            if (source == null) {
                return null;
            }
            return Instant.parse(source).atZone(ZoneId.systemDefault());
        }
    }

    @WritingConverter
    static class InstantWritingConverter implements Converter<Instant, String> {

        @Override
        public String convert(Instant source) {
            if (source == null) {
                return null;
            }
            return source.toString();
        }
    }

    @ReadingConverter
    static class InstantReadingConverter implements Converter<String, Instant> {

        @Override
        public Instant convert(String source) {
            if (source == null) {
                return null;
            }
            return Instant.parse(source);
        }
    }

    @WritingConverter
    static class LocalDateWritingConverter implements Converter<LocalDate, String> {

        @Override
        public String convert(LocalDate source) {
            if (source == null) {
                return null;
            }
            return source.toString();
        }
    }

    @WritingConverter
    static class LocalDateTimeWritingConverter implements Converter<LocalDateTime, String> {

        @Override
        public String convert(LocalDateTime source) {
            if (source == null) {
                return null;
            }
            return source.toString();
        }
    }

    @ReadingConverter
    static class LocalDateReadingConverter implements Converter<String, LocalDate> {

        @Override
        public LocalDate convert(String source) {
            if (source == null) {
                return null;
            }
            return LocalDate.parse(source);
        }
    }

    @ReadingConverter
    static class LocalDateTimeReadingConverter implements Converter<String, LocalDateTime> {

        @Override
        public LocalDateTime convert(String source) {
            if (source == null) {
                return null;
            }
            return LocalDateTime.parse(source);
        }
    }
}
