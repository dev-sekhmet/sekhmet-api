package com.sekhmet.api.repository.rowmapper;

import com.sekhmet.api.domain.ChatMember;
import com.sekhmet.api.domain.enumeration.ChatMemberScope;
import com.sekhmet.api.service.ColumnConverter;
import io.r2dbc.spi.Row;
import java.util.UUID;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link ChatMember}, with proper type conversions.
 */
@Service
public class ChatMemberRowMapper implements BiFunction<Row, String, ChatMember> {

    private final ColumnConverter converter;

    public ChatMemberRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link ChatMember} stored in the database.
     */
    @Override
    public ChatMember apply(Row row, String prefix) {
        ChatMember entity = new ChatMember();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setUid(converter.fromRow(row, prefix + "_uid", UUID.class));
        entity.setScope(converter.fromRow(row, prefix + "_scope", ChatMemberScope.class));
        return entity;
    }
}
