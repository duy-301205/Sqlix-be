package com.example.sqlix.service.practice;

import java.util.Locale;
import java.util.regex.Pattern;

final class SqlReadOnlyValidator {
    private static final Pattern FORBIDDEN = Pattern.compile(
            "\\b(INSERT|UPDATE|DELETE|MERGE|DROP|ALTER|CREATE|TRUNCATE|GRANT|REVOKE|CALL|COPY|DO|VACUUM|ANALYZE|INTO)\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern DANGEROUS_FUNCTION = Pattern.compile(
            "\\b(PG_READ_FILE|PG_READ_BINARY_FILE|PG_LS_DIR|PG_STAT_FILE|LO_IMPORT|LO_EXPORT|"
                    + "DBLINK|PG_SLEEP|PG_TERMINATE_BACKEND|PG_CANCEL_BACKEND|SET_CONFIG|NEXTVAL|SETVAL)\\s*\\(",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern QUALIFIED_RELATION = Pattern.compile(
            "\\b(FROM|JOIN)\\s+[A-Z_][A-Z0-9_$]*\\s*\\.", Pattern.CASE_INSENSITIVE);
    private static final Pattern SYSTEM_RELATION = Pattern.compile(
            "\\b(FROM|JOIN)\\s+(?:ONLY\\s+)?(?:LATERAL\\s+)?PG_[A-Z0-9_$]*\\b",
            Pattern.CASE_INSENSITIVE);

    private SqlReadOnlyValidator() {
    }

    static boolean isSingleSelect(String sql) {
        if (sql == null || sql.isBlank()) {
            return false;
        }
        String visibleSql = maskStringsAndComments(sql).trim();
        if (visibleSql.endsWith(";")) {
            visibleSql = visibleSql.substring(0, visibleSql.length() - 1).trim();
        }
        if (visibleSql.isEmpty() || visibleSql.indexOf(';') >= 0) {
            return false;
        }
        String upperSql = visibleSql.toUpperCase(Locale.ROOT);
        return upperSql.matches("(?s)^SELECT\\b.*")
                && !FORBIDDEN.matcher(upperSql).find()
                && !DANGEROUS_FUNCTION.matcher(upperSql).find()
                && !QUALIFIED_RELATION.matcher(upperSql).find()
                && !SYSTEM_RELATION.matcher(upperSql).find();
    }

    private static String maskStringsAndComments(String sql) {
        StringBuilder masked = new StringBuilder(sql.length());
        int i = 0;
        while (i < sql.length()) {
            char current = sql.charAt(i);
            if (current == '-' && i + 1 < sql.length() && sql.charAt(i + 1) == '-') {
                masked.append("  ");
                i += 2;
                while (i < sql.length() && sql.charAt(i) != '\n' && sql.charAt(i) != '\r') {
                    masked.append(' ');
                    i++;
                }
            } else if (current == '/' && i + 1 < sql.length() && sql.charAt(i + 1) == '*') {
                masked.append("  ");
                i += 2;
                int depth = 1;
                while (i < sql.length() && depth > 0) {
                    if (i + 1 < sql.length() && sql.charAt(i) == '/' && sql.charAt(i + 1) == '*') {
                        masked.append("  ");
                        i += 2;
                        depth++;
                    } else if (i + 1 < sql.length() && sql.charAt(i) == '*' && sql.charAt(i + 1) == '/') {
                        masked.append("  ");
                        i += 2;
                        depth--;
                    } else {
                        masked.append(sql.charAt(i) == '\n' ? '\n' : ' ');
                        i++;
                    }
                }
                if (depth != 0) {
                    return "";
                }
            } else if (current == '\'' || current == '"') {
                char quote = current;
                boolean stringLiteral = quote == '\'';
                if (stringLiteral) {
                    masked.append(' ');
                }
                i++;
                boolean closed = false;
                while (i < sql.length()) {
                    char c = sql.charAt(i++);
                    if (c == quote) {
                        if (i < sql.length() && sql.charAt(i) == quote) {
                            masked.append(stringLiteral ? ' ' : quote);
                            i++;
                        } else {
                            closed = true;
                            break;
                        }
                    } else if (c == '\\' && quote == '\'' && i < sql.length()) {
                        masked.append(' ');
                        i++;
                    } else {
                        masked.append(stringLiteral ? (c == '\n' ? '\n' : ' ') : c);
                    }
                }
                if (!closed) {
                    return "";
                }
            } else if (current == '$') {
                int delimiterEnd = i + 1;
                while (delimiterEnd < sql.length()
                        && (Character.isLetterOrDigit(sql.charAt(delimiterEnd)) || sql.charAt(delimiterEnd) == '_')) {
                    delimiterEnd++;
                }
                if (delimiterEnd < sql.length() && sql.charAt(delimiterEnd) == '$') {
                    String delimiter = sql.substring(i, delimiterEnd + 1);
                    int closing = sql.indexOf(delimiter, delimiterEnd + 1);
                    if (closing < 0) {
                        return "";
                    }
                    int end = closing + delimiter.length();
                    for (int j = i; j < end; j++) {
                        masked.append(sql.charAt(j) == '\n' ? '\n' : ' ');
                    }
                    i = end;
                } else {
                    masked.append(current);
                    i++;
                }
            } else {
                masked.append(current);
                i++;
            }
        }
        return masked.toString();
    }
}
