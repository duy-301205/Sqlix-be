-- =========================================================
-- MIGRATION MODULE 4: LUYỆN TẬP DB & VIẾT TRUY VẤN
-- =========================================================

-- 1. ENUMS
CREATE TYPE difficulty_level AS ENUM ('EASY', 'MEDIUM', 'HARD');
CREATE TYPE submission_status AS ENUM ('PASSED', 'FAILED', 'ERROR');

-- 2. BẢNG QUẢN LÝ CÁC CƠ SỞ DỮ LIỆU MẪU (Practice Databases)
CREATE TABLE practice_databases (
                                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                    name VARCHAR(255) NOT NULL,            -- VD: 'E-commerce DB'
                                    slug VARCHAR(255) UNIQUE NOT NULL,     -- VD: 'e-commerce-db'
                                    description TEXT,                      -- Mô tả ngắn
                                    difficulty difficulty_level NOT NULL DEFAULT 'MEDIUM',
                                    icon_url TEXT,                         -- URL icon hiển thị ở trang chủ

    -- Dữ liệu hiển thị Sơ đồ ERD & Cấu trúc (Lưu dạng JSON cho linh hoạt và truy vấn nhanh)
                                    schema_data JSONB NOT NULL,

    -- Script SQL để backend tự động build database giả lập lúc chấm điểm
                                    init_sql_script TEXT NOT NULL,

    -- Các trường thống kê chuẩn hóa (Denormalized stats) hiển thị ở card
                                    tables_count INT NOT NULL DEFAULT 0,
                                    exercises_count INT NOT NULL DEFAULT 0,
                                    rating NUMERIC(3,2) NOT NULL DEFAULT 5.00,

                                    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER tr_practice_databases_updated_at
    BEFORE UPDATE ON practice_databases
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- 3. BẢNG BÀI TẬP TRUY VẤN (Practice Exercises)
CREATE TABLE practice_exercises (
                                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                    database_id UUID NOT NULL REFERENCES practice_databases(id) ON DELETE CASCADE,
                                    title VARCHAR(255) NOT NULL,           -- VD: 'Lọc danh sách sản phẩm giá từ 500,000 trở lên'
                                    description TEXT,                      -- Mô tả đề bài
                                    difficulty difficulty_level NOT NULL DEFAULT 'EASY',
                                    topics VARCHAR(255),                   -- VD: 'SELECT & WHERE' (Tags hiển thị)
                                    max_score INT NOT NULL DEFAULT 100,

    -- Các điều kiện kỳ vọng hiển thị cho user
                                    requirements JSONB,                    -- Mảng JSON: ["Truy vấn từ bảng products", "Mệnh đề WHERE..."]
                                    expected_columns JSONB,                -- Mảng JSON: ["product_name", "price", "stock_quantity"]

    -- Câu lệnh chuẩn để backend chạy đối chiếu output
                                    solution_query TEXT NOT NULL,

                                    sort_order INT NOT NULL DEFAULT 0,
                                    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER tr_practice_exercises_updated_at
    BEFORE UPDATE ON practice_exercises
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- 4. BẢNG TIẾN ĐỘ BÀI TẬP CỦA USER (Chỉ lưu kỷ lục/điểm cao nhất)
CREATE TABLE user_exercise_progress (
                                        user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                        exercise_id UUID NOT NULL REFERENCES practice_exercises(id) ON DELETE CASCADE,
                                        highest_score INT NOT NULL DEFAULT 0,
                                        is_passed BOOLEAN NOT NULL DEFAULT FALSE,
                                        last_attempt_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                        PRIMARY KEY (user_id, exercise_id)
);

-- 5. BẢNG LỊCH SỬ NỘP BÀI CỦA USER (Lưu mọi lần submit để xem lại)
CREATE TABLE user_exercise_submissions (
                                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                           user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                           exercise_id UUID NOT NULL REFERENCES practice_exercises(id) ON DELETE CASCADE,
                                           submitted_query TEXT NOT NULL,         -- Lệnh SQL user đã gõ
                                           score INT NOT NULL DEFAULT 0,
                                           status submission_status NOT NULL,     -- PASSED (Đúng), FAILED (Sai data), ERROR (Lỗi cú pháp SQL)
                                           feedback TEXT,                         -- Lời giải thích tự động (VD: "Output thiếu cột price")
                                           execution_time_ms INT,                 -- Thời gian chạy query
                                           created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tối ưu hóa truy vấn lịch sử làm bài
CREATE INDEX idx_user_submissions_lookup ON user_exercise_submissions(user_id, exercise_id, created_at DESC);