package com.sekhmet.api.repository.rowmapper;

import com.sekhmet.api.domain.Message;
import com.sekhmet.api.service.ColumnConverter;
import io.r2dbc.spi.Row;
import java.time.LocalDate;
import java.util.UUID;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Message}, with proper type conversions.
 */
@Service
public class MessageRowMapper implements BiFunction<Row, String, Message> {

    private final ColumnConverter converter;

    public MessageRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Message} stored in the database.
     */
    @Override
    public Message apply(Row row, String prefix) {
        Message entity = new Message();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setUid(converter.fromRow(row, prefix + "_uid", UUID.class));
        entity.setCreatedAt(converter.fromRow(row, prefix + "_created_at", LocalDate.class));
        entity.setImage(converter.fromRow(row, prefix + "_image", String.class));
        entity.setVideo(converter.fromRow(row, prefix + "_video", String.class));
        entity.setAudio(converter.fromRow(row, prefix + "_audio", String.class));
        entity.setSystem(converter.fromRow(row, prefix + "_system", Boolean.class));
        entity.setSent(converter.fromRow(row, prefix + "_sent", Boolean.class));
        entity.setReceived(converter.fromRow(row, prefix + "_received", Boolean.class));
        entity.setPending(converter.fromRow(row, prefix + "_pending", Boolean.class));
        return entity;
    }
}
