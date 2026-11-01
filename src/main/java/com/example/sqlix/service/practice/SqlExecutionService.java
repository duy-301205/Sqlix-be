package com.example.sqlix.service.practice;

import com.example.sqlix.dto.response.QueryResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SqlExecutionService {
    private static final String INVALID_QUERY_MESSAGE =
            "Chỉ chấp nhận một câu lệnh SELECT hợp lệ.";
    private static final String EXECUTION_ERROR_MESSAGE =
            "Không thể chạy truy vấn. Vui lòng kiểm tra cú pháp, tên bảng và tên cột.";
    private static final String SANDBOX_ERROR_MESSAGE =
            "Không thể khởi tạo môi trường dữ liệu thực hành.";

    private final JdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public QueryResultDto executeSelect(String initSqlScript, String query, int maxRows) {
        if (!SqlReadOnlyValidator.isSingleSelect(query)) {
            return QueryResultDto.error(INVALID_QUERY_MESSAGE);
        }
        try {
            return jdbcTemplate.execute((ConnectionCallback<QueryResultDto>) connection ->
                    executeInTemporarySchema(connection, initSqlScript, query, maxRows));
        } catch (DataAccessException exception) {
            return QueryResultDto.error(EXECUTION_ERROR_MESSAGE);
        } catch (RuntimeException exception) {
            return QueryResultDto.error(SANDBOX_ERROR_MESSAGE);
        }
    }

    private QueryResultDto executeInTemporarySchema(
            Connection connection,
            String initSqlScript,
            String query,
            int maxRows) throws SQLException {
        String schemaName = "practice_sandbox_" + UUID.randomUUID().toString().replace("-", "");
        boolean originalAutoCommit = connection.getAutoCommit();
        boolean originalReadOnly = connection.isReadOnly();
        boolean schemaCreated = false;
        try {
            connection.setReadOnly(false);
            connection.setAutoCommit(false);
            try (Statement setup = connection.createStatement()) {
                setup.execute("CREATE SCHEMA \"" + schemaName + "\"");
                schemaCreated = true;
                setup.execute("SET search_path TO \"" + schemaName + "\", pg_catalog");
            }

            if (initSqlScript == null || initSqlScript.isBlank()) {
                throw new IllegalStateException("Practice database has no initialization script");
            }
            ScriptUtils.executeSqlScript(connection,
                    new EncodedResource(new ByteArrayResource(initSqlScript.getBytes(StandardCharsets.UTF_8))));
            connection.commit();

            connection.setReadOnly(true);
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                statement.setMaxRows(maxRows);
                statement.setQueryTimeout(10);
                try (ResultSet resultSet = statement.executeQuery(query)) {
                    return toQueryResult(resultSet);
                }
            } finally {
                connection.rollback();
            }
        } finally {
            cleanupSandbox(connection, schemaName, schemaCreated, originalReadOnly, originalAutoCommit);
        }
    }

    private QueryResultDto toQueryResult(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metadata = resultSet.getMetaData();
        List<String> columns = new ArrayList<>(metadata.getColumnCount());
        for (int column = 1; column <= metadata.getColumnCount(); column++) {
            columns.add(metadata.getColumnLabel(column));
        }

        List<List<Object>> rows = new ArrayList<>();
        while (resultSet.next()) {
            List<Object> row = new ArrayList<>(metadata.getColumnCount());
            for (int column = 1; column <= metadata.getColumnCount(); column++) {
                row.add(toJsonFriendlyValue(resultSet.getObject(column)));
            }
            rows.add(row);
        }
        return new QueryResultDto(columns, rows, null);
    }

    private Object toJsonFriendlyValue(Object value) throws SQLException {
        if (value instanceof Timestamp timestamp) {
            return timestamp.toInstant();
        }
        if (value instanceof Array sqlArray) {
            try {
                Object array = sqlArray.getArray();
                if (array instanceof Object[] values) {
                    List<Object> result = new ArrayList<>(values.length);
                    for (Object item : values) {
                        result.add(toJsonFriendlyValue(item));
                    }
                    return result;
                }
                return array;
            } finally {
                sqlArray.free();
            }
        }
        if (value != null && value.getClass().getName().equals("org.postgresql.util.PGobject")) {
            return value.toString();
        }
        return value;
    }

    private void cleanupSandbox(
            Connection connection,
            String schemaName,
            boolean schemaCreated,
            boolean originalReadOnly,
            boolean originalAutoCommit) throws SQLException {
        SQLException cleanupError = null;
        try {
            if (!connection.getAutoCommit()) {
                connection.rollback();
            }
            connection.setReadOnly(false);
            connection.setAutoCommit(true);
            try (Statement cleanup = connection.createStatement()) {
                if (schemaCreated) {
                    cleanup.execute("DROP SCHEMA IF EXISTS \"" + schemaName + "\" CASCADE");
                }
                cleanup.execute("RESET search_path");
            }
        } catch (SQLException exception) {
            cleanupError = exception;
        } finally {
            try {
                connection.setReadOnly(originalReadOnly);
                connection.setAutoCommit(originalAutoCommit);
            } catch (SQLException exception) {
                if (cleanupError == null) {
                    cleanupError = exception;
                } else {
                    cleanupError.addSuppressed(exception);
                }
            }
        }
        if (cleanupError != null) {
            throw cleanupError;
        }
    }
}
