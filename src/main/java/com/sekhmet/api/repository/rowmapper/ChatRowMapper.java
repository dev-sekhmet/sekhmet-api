package com.sekhmet.api.repository.rowmapper;

import com.sekhmet.api.domain.Chat;
import com.sekhmet.api.service.ColumnConverter;
import io.r2dbc.spi.Row;
import java.util.UUID;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Chat}, with proper type conversions.
 */
@Service
public class ChatRowMapper implements BiFunction<Row, String, Chat> {

    private final ColumnConverter converter;

    public ChatRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Chat} stored in the database.
     */
    @Override
    public Chat apply(Row row, String prefix) {
        Chat entity = new Chat();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setGuid(converter.fromRow(row, prefix + "_guid", UUID.class));
        entity.setIcon(converter.fromRow(row, prefix + "_icon", String.class));
        entity.setName(converter.fromRow(row, prefix + "_name", String.class));
        return entity;
    }
}
