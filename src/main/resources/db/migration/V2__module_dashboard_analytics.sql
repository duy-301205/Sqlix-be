-- =========================================================
-- MIGRATION MODULE 2: DASHBOARD & ANALYTICS
-- (Lưu ý: Yêu cầu đã chạy Migration Module 1 để có bảng users và hàm set_updated_at)
-- =========================================================

-- 1. ĐỊNH NGHĨA ENUM CHO CÁC CHỦ ĐỀ SQL VÀ LOẠI HOẠT ĐỘNG
CREATE TYPE sql_topic_code AS ENUM (
    'BASIC_SELECT', 'AGGREGATION', 'JOINS',
    'GROUP_BY', 'SUBQUERIES', 'WINDOW_FUNCTIONS', 'INDEXING'
);

CREATE TYPE activity_type AS ENUM (
    'LESSON_COMPLETED', 'EXERCISE_SOLVED', 'MOCK_TEST_TAKEN'
);

-- 2. BẢNG TỔNG HỢP CHỈ SỐ THEO NGÀY (Phục vụ biểu đồ 30 ngày & Mục tiêu ngày)
CREATE TABLE user_daily_metrics (
                                    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                    record_date DATE NOT NULL DEFAULT CURRENT_DATE,
                                    study_time_seconds INT NOT NULL DEFAULT 0 CHECK (study_time_seconds >= 0),
                                    lessons_completed INT NOT NULL DEFAULT 0 CHECK (lessons_completed >= 0),
                                    exercises_solved INT NOT NULL DEFAULT 0 CHECK (exercises_solved >= 0),
                                    total_score INT NOT NULL DEFAULT 0 CHECK (total_score >= 0), -- Dùng để chia trung bình
                                    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                    PRIMARY KEY (user_id, record_date)
);

-- Trigger cập nhật updated_at cho user_daily_metrics
CREATE TRIGGER tr_daily_metrics_updated_at
    BEFORE UPDATE ON user_daily_metrics
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

-- 3. BẢNG ĐỘ THÔNG THẠO THEO CHỦ ĐỀ (Phục vụ Skills Mastery Progress)
CREATE TABLE user_topic_mastery (
                                    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                    topic_code sql_topic_code NOT NULL,
                                    mastery_percentage NUMERIC(5,2) NOT NULL DEFAULT 0.00 CHECK (mastery_percentage BETWEEN 0 AND 100),
                                    exercises_solved_in_topic INT NOT NULL DEFAULT 0 CHECK (exercises_solved_in_topic >= 0),
                                    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                    PRIMARY KEY (user_id, topic_code)
);

CREATE TRIGGER tr_topic_mastery_updated_at
    BEFORE UPDATE ON user_topic_mastery
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

-- 4. BẢNG NHẬT KÝ HOẠT ĐỘNG GẦN ĐÂY (Phục vụ Action Cards)
-- Bảng này chỉ lưu dạng Log (Append-only) để truy vấn nhanh danh sách "Recent"
CREATE TABLE user_recent_activities (
                                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                        user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                        type activity_type NOT NULL,
                                        reference_id UUID NOT NULL, -- ID của bài học, bài tập hoặc bài thi (sẽ có ở các module sau)
                                        meta_data JSONB NULL,       -- Chứa JSON tóm tắt (vd: {"score": 90, "title": "Join 3 tables"}) để render nhanh không cần JOIN
                                        created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tối ưu Index cho việc truy vấn hoạt động gần nhất của một user
CREATE INDEX idx_user_activities_user_id ON user_recent_activities(user_id, created_at DESC);