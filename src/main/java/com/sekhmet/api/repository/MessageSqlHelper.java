package com.sekhmet.api.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class MessageSqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("uid", table, columnPrefix + "_uid"));
        columns.add(Column.aliased("created_at", table, columnPrefix + "_created_at"));
        columns.add(Column.aliased("image", table, columnPrefix + "_image"));
        columns.add(Column.aliased("video", table, columnPrefix + "_video"));
        columns.add(Column.aliased("audio", table, columnPrefix + "_audio"));
        columns.add(Column.aliased("system", table, columnPrefix + "_system"));
        columns.add(Column.aliased("sent", table, columnPrefix + "_sent"));
        columns.add(Column.aliased("received", table, columnPrefix + "_received"));
        columns.add(Column.aliased("pending", table, columnPrefix + "_pending"));

        return columns;
    }
}
