-- =========================================================
-- MIGRATION MODULE 3: LỘ TRÌNH & LÝ THUYẾT SQL
-- =========================================================

-- 1. BẢNG DANH MỤC LỘ TRÌNH (VD: SQL Cơ bản, SQL Nâng cao)
CREATE TABLE course_categories (
                                   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                   name VARCHAR(100) NOT NULL,
                                   description TEXT,
                                   sort_order INT NOT NULL DEFAULT 0, -- Dùng để sắp xếp (SQL cơ bản lên trước)
                                   created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER tr_course_categories_updated_at
    BEFORE UPDATE ON course_categories
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- 2. BẢNG BÀI HỌC (Lessons)
CREATE TABLE lessons (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         category_id UUID NOT NULL REFERENCES course_categories(id) ON DELETE CASCADE,
                         title VARCHAR(255) NOT NULL, -- VD: "01. SELECT"
                         slug VARCHAR(255) UNIQUE NOT NULL, -- Dùng cho URL: /learning/sql-co-ban/select
                         description TEXT, -- "Tìm hiểu câu lệnh SELECT..."
                         objectives JSONB, -- Mảng JSON chứa các mục tiêu: ["Hiểu cú pháp...", "Truy vấn toàn bộ..."]
                         practice_topic_code VARCHAR(50) NULL, -- Mã chủ đề bài tập để link sang nút "Luyện tập ngay" (VD: 'BASIC_SELECT')
                         sort_order INT NOT NULL DEFAULT 0,
                         created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER tr_lessons_updated_at
    BEFORE UPDATE ON lessons
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- 3. BẢNG CHI TIẾT NỘI DUNG BÀI HỌC (Sections - Phục vụ TOC ở cột phải và cuộn)
CREATE TABLE lesson_sections (
                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
                                 title VARCHAR(255) NOT NULL, -- VD: "1. SELECT là gì?"
                                 content TEXT NOT NULL,       -- Nội dung chữ/HTML/Markdown
                                 sample_sql TEXT NULL,        -- Khối code SQL mẫu (nếu có)
                                 note_box JSONB NULL,         -- Hộp cảnh báo/tip (VD: {"type": "tip", "text": "Mẹo..."})
                                 sort_order INT NOT NULL DEFAULT 0,
                                 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER tr_lesson_sections_updated_at
    BEFORE UPDATE ON lesson_sections
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- 4. BẢNG TRẠNG THÁI HỌC TẬP CỦA USER
CREATE TYPE lesson_status AS ENUM ('NOT_STARTED', 'COMPLETED');

CREATE TABLE user_lesson_progress (
                                      user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                      lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
                                      is_bookmarked BOOLEAN NOT NULL DEFAULT FALSE, -- Trạng thái thả tim "Yêu thích"
                                      status lesson_status NOT NULL DEFAULT 'NOT_STARTED', -- Đã bấm "Đánh dấu đã hiểu" hay chưa
                                      completed_at TIMESTAMPTZ NULL, -- Thời điểm bấm hoàn thành
                                      updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                      PRIMARY KEY (user_id, lesson_id)
);

CREATE TRIGGER tr_user_lesson_progress_updated_at
    BEFORE UPDATE ON user_lesson_progress
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();