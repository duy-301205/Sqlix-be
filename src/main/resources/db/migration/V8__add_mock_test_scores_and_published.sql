-- Migration V8: Bổ sung điểm đạt, điểm tối đa và trạng thái xuất bản cho bảng mock_tests
ALTER TABLE mock_tests
    ADD COLUMN IF NOT EXISTS passing_score NUMERIC(5, 2) NOT NULL DEFAULT 7.0,
    ADD COLUMN IF NOT EXISTS max_score INT NOT NULL DEFAULT 10,
    ADD COLUMN IF NOT EXISTS is_published BOOLEAN NOT NULL DEFAULT TRUE;
