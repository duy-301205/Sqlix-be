-- =========================================================
-- MIGRATION MODULE 5: MOCK TEST (THI THỬ)
-- (Yêu cầu: Đã chạy Migration Module 4 trước đó)
-- =========================================================

-- 1. ENUMS MỚI
CREATE TYPE question_type AS ENUM ('SQL_QUERY', 'MULTIPLE_CHOICE');
CREATE TYPE test_session_status AS ENUM ('IN_PROGRESS', 'COMPLETED', 'TIMEOUT');

-- 2. BẢNG QUẢN LÝ ĐỀ THI (Mock Tests)
CREATE TABLE mock_tests (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            title VARCHAR(255) NOT NULL,           -- VD: 'E-commerce Database - Cơ bản'
                            slug VARCHAR(255) UNIQUE NOT NULL,
                            description TEXT,
                            difficulty difficulty_level NOT NULL DEFAULT 'MEDIUM', -- Tái sử dụng ENUM từ Mod 4
                            duration_minutes INT NOT NULL DEFAULT 30,              -- Thời gian làm bài tổng
                            total_questions INT NOT NULL DEFAULT 0,
                            cover_image_url TEXT,                                  -- Icon/Hình ảnh đề thi

    -- Thống kê hiển thị
                            total_participants INT NOT NULL DEFAULT 0,

                            created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER tr_mock_tests_updated_at
    BEFORE UPDATE ON mock_tests
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- 3. BẢNG CÂU HỎI TRONG ĐỀ THI (Dùng chung cho cả Trắc nghiệm & Thực hành)
CREATE TABLE mock_test_questions (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     test_id UUID NOT NULL REFERENCES mock_tests(id) ON DELETE CASCADE,
                                     question_type question_type NOT NULL,

    -- Liên kết đến CSDL mẫu (Chỉ dùng cho câu SQL, câu trắc nghiệm có thể để NULL)
                                     database_id UUID NULL REFERENCES practice_databases(id),

                                     content TEXT NOT NULL,                 -- Đề bài chung (Câu hỏi)
                                     points INT NOT NULL DEFAULT 100,       -- Điểm tối đa của câu này
                                     sort_order INT NOT NULL DEFAULT 0,     -- Thứ tự câu hỏi (Câu 1, Câu 2...)

    -- ---------------------------------------------------------
    -- CÁC TRƯỜNG DÀNH RIÊNG CHO CÂU HỎI SQL_QUERY
    -- ---------------------------------------------------------
                                     requirements JSONB NULL,               -- Các yêu cầu ("Truy vấn từ bảng...")
                                     expected_columns JSONB NULL,           -- Cột kỳ vọng
                                     solution_query TEXT NULL,              -- Đáp án query chuẩn
                                     max_retries INT NOT NULL DEFAULT 3,    -- Giới hạn số lần nộp bài (Admin set)

    -- ---------------------------------------------------------
    -- CÁC TRƯỜNG DÀNH RIÊNG CHO CÂU HỎI MULTIPLE_CHOICE
    -- ---------------------------------------------------------
                                     mc_options JSONB NULL,                 -- Mảng JSON: [{"id": "A", "text": "SELECT *"}, {"id": "B", ...}]
                                     mc_correct_option VARCHAR(50) NULL,    -- Lưu ID đáp án đúng (VD: 'A')
                                     explanation TEXT NULL,                 -- Lời giải thích khi xem lại bài

                                     created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER tr_mock_test_questions_updated_at
    BEFORE UPDATE ON mock_test_questions
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- 4. BẢNG PHIÊN LÀM BÀI (Test Sessions - Quản lý thời gian & Tổng điểm)
CREATE TABLE test_sessions (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               test_id UUID NOT NULL REFERENCES mock_tests(id) ON DELETE CASCADE,
                               status test_session_status NOT NULL DEFAULT 'IN_PROGRESS',

                               start_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               end_time TIMESTAMPTZ NULL,             -- Thời điểm nộp bài hoặc hệ thống tự thu bài
                               total_score INT NOT NULL DEFAULT 0,    -- Tổng điểm đạt được

                               created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER tr_test_sessions_updated_at
    BEFORE UPDATE ON test_sessions
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- 5. BẢNG LƯU ĐÁP ÁN TỪNG CÂU CỦA NGƯỜI DÙNG (Test Session Answers)
CREATE TABLE test_session_answers (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                      session_id UUID NOT NULL REFERENCES test_sessions(id) ON DELETE CASCADE,
                                      question_id UUID NOT NULL REFERENCES mock_test_questions(id) ON DELETE CASCADE,

                                      submitted_answer TEXT NULL,            -- Lưu chuỗi code SQL HOẶC ID đáp án trắc nghiệm (VD: 'B')
                                      is_correct BOOLEAN NOT NULL DEFAULT FALSE,
                                      score INT NOT NULL DEFAULT 0,          -- Điểm đạt được cho câu này
                                      retry_count INT NOT NULL DEFAULT 0,    -- Số lần đã nộp (Dành cho câu SQL)

                                      created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Đảm bảo mỗi phiên làm bài chỉ có 1 bản ghi đáp án duy nhất cho 1 câu hỏi (dùng Update đè lên)
                                      CONSTRAINT uq_session_question UNIQUE (session_id, question_id)
);

CREATE TRIGGER tr_test_session_answers_updated_at
    BEFORE UPDATE ON test_session_answers
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();