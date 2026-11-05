-- =========================================================
-- MIGRATION MODULE 6: MOCK TEST HISTORY (LỊCH SỬ THI THỬ)
-- =========================================================

-- 1. BỔ SUNG CÁC CỘT CHỈ SỐ TÓM TẮT CHO BẢNG test_sessions
ALTER TABLE test_sessions
    ADD COLUMN IF NOT EXISTS duration_seconds INT DEFAULT 0 CHECK (duration_seconds >= 0),
    ADD COLUMN IF NOT EXISTS correct_count INT DEFAULT 0 CHECK (correct_count >= 0),
    ADD COLUMN IF NOT EXISTS total_questions INT DEFAULT 0 CHECK (total_questions >= 0),
    ADD COLUMN IF NOT EXISTS is_passed BOOLEAN DEFAULT FALSE;

-- 2. ĐỊNH NGHĨA CÁC INDEX TỐI ƯU TRUY VẤN DỮ LIỆU LỊCH SỬ
-- Tối ưu lọc & phân trang danh sách lịch sử thi của học viên theo thời gian nộp
CREATE INDEX IF NOT EXISTS idx_test_sessions_user_history
    ON test_sessions(user_id, status, created_at DESC);

-- Tối ưu sắp xếp theo điểm số và tìm kiếm bài thi đạt điểm cao nhất
CREATE INDEX IF NOT EXISTS idx_test_sessions_user_score
    ON test_sessions(user_id, total_score DESC);

-- Tối ưu tìm kiếm và tính toán số câu đúng từ bảng đáp án
CREATE INDEX IF NOT EXISTS idx_test_session_answers_lookup
    ON test_session_answers(session_id, is_correct);

-- 3. HÀM & TRIGGER TỰ ĐỘNG TÍNH THỜI GIAN VÀ KẾT QUẢ KHI NỘP BÀI
CREATE OR REPLACE FUNCTION update_test_session_summary()
RETURNS TRIGGER AS $$
BEGIN
    -- Chỉ tính toán tự động khi phiên thi chuyển trạng thái sang COMPLETED hoặc TIMEOUT
    IF (NEW.status IN ('COMPLETED', 'TIMEOUT') AND OLD.status = 'IN_PROGRESS') THEN
        -- a. Tính thời gian làm bài thực tế (giây)
        IF NEW.end_time IS NOT NULL AND NEW.start_time IS NOT NULL THEN
            NEW.duration_seconds := GREATEST(0, EXTRACT(EPOCH FROM (NEW.end_time - NEW.start_time))::INT);
END IF;

        -- b. Đánh dấu trạng thái Đạt (Điểm >= 60%)
        IF NEW.total_score >= 60 THEN
            NEW.is_passed := TRUE;
ELSE
            NEW.is_passed := FALSE;
END IF;

        -- c. Tự động đếm tổng số câu và số câu làm đúng từ test_session_answers
SELECT
    COUNT(*),
    COUNT(CASE WHEN is_correct = TRUE THEN 1 END)
INTO
    NEW.total_questions,
    NEW.correct_count
FROM test_session_answers
WHERE session_id = NEW.id;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_update_test_session_summary
    BEFORE UPDATE ON test_sessions
    FOR EACH ROW
    EXECUTE FUNCTION update_test_session_summary();

-- 4. DATABASE VIEW TRUY VẤN BÁO CÁO THỐNG KÊ NGHỆU DÙNG (TOP METRIC CARDS)
-- Hỗ trợ API trả về nhanh 4 chỉ số: Tổng bài thi, Điểm trung bình, Điểm cao nhất, Tỷ lệ Đạt
CREATE OR REPLACE VIEW vw_user_mock_test_summary AS
SELECT
    user_id,
    COUNT(*) AS total_tests_taken,
    ROUND(AVG(total_score), 2) AS average_score,
    MAX(total_score) AS highest_score,
    COUNT(CASE WHEN is_passed = TRUE THEN 1 END) AS passed_tests_count,
    ROUND(
            (COUNT(CASE WHEN is_passed = TRUE THEN 1 END)::NUMERIC / NULLIF(COUNT(*), 0)::NUMERIC) * 100,
            2
    ) AS pass_rate_percentage
FROM test_sessions
WHERE status IN ('COMPLETED', 'TIMEOUT')
GROUP BY user_id;