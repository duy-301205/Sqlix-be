package com.example.sqlix.service.practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SqlReadOnlyValidatorTest {
    @Test
    void acceptsSelectAndDoesNotTreatCommentsOrStringLiteralsAsCommands() {
        assertThat(SqlReadOnlyValidator.isSingleSelect(
                "SELECT 'DROP TABLE users' AS note /* DELETE */ FROM products;"))
                .isTrue();
    }

    @Test
    void rejectsMutatingCommandsAndMultipleStatements() {
        assertThat(SqlReadOnlyValidator.isSingleSelect("DELETE FROM products")).isFalse();
        assertThat(SqlReadOnlyValidator.isSingleSelect("SELECT 1; DROP TABLE products")).isFalse();
        assertThat(SqlReadOnlyValidator.isSingleSelect("SELECT * INTO copied_products FROM products")).isFalse();
        assertThat(SqlReadOnlyValidator.isSingleSelect("SELECT * FROM public.users")).isFalse();
        assertThat(SqlReadOnlyValidator.isSingleSelect("SELECT * FROM \"public\".\"users\"")).isFalse();
        assertThat(SqlReadOnlyValidator.isSingleSelect("SELECT * FROM pg_catalog.pg_class")).isFalse();
        assertThat(SqlReadOnlyValidator.isSingleSelect("SELECT * FROM pg_class")).isFalse();
        assertThat(SqlReadOnlyValidator.isSingleSelect("SELECT pg_read_file('/etc/passwd')")).isFalse();
    }
}
