-- Chạy thủ công một lần; không tự tạo lại dữ liệu khi khởi động dự án.
BEGIN;
INSERT INTO practice_databases (name, slug, description, difficulty, schema_data, init_sql_script, tables_count, exercises_count)
VALUES ('Quản lý Sách & Tác giả', 'quan-ly-sach-va-tac-gia',
'Dữ liệu mẫu quản lý sách gồm 5 bảng, 50 bản ghi: tác giả, nhà xuất bản, thể loại, sách và tác giả của sách. Hỗ trợ luyện SELECT, JOIN, GROUP BY và quan hệ nhiều-nhiều. Tên sách và đơn vị là dữ liệu giả lập.',
'MEDIUM', $schema${"tables": [{"name": "authors", "vietnameseName": "Tác giả", "description": "Tác giả", "columns": [{"name": "author_id", "type": "INT", "description": "Mã tác giả", "isPk": true, "isFk": false}, {"name": "full_name", "type": "VARCHAR(100)", "description": "Họ tên tác giả", "isPk": false, "isFk": false}, {"name": "country", "type": "VARCHAR(60)", "description": "Quốc gia", "isPk": false, "isFk": false}, {"name": "birth_year", "type": "INT", "description": "Năm sinh", "isPk": false, "isFk": false}]}, {"name": "publishers", "vietnameseName": "Nhà xuất bản", "description": "Nhà xuất bản", "columns": [{"name": "publisher_id", "type": "INT", "description": "Mã nhà xuất bản", "isPk": true, "isFk": false}, {"name": "name", "type": "VARCHAR(100)", "description": "Tên nhà xuất bản", "isPk": false, "isFk": false}, {"name": "city", "type": "VARCHAR(60)", "description": "Thành phố", "isPk": false, "isFk": false}]}, {"name": "categories", "vietnameseName": "Thể loại", "description": "Thể loại", "columns": [{"name": "category_id", "type": "INT", "description": "Mã thể loại", "isPk": true, "isFk": false}, {"name": "name", "type": "VARCHAR(80)", "description": "Tên thể loại", "isPk": false, "isFk": false}]}, {"name": "books", "vietnameseName": "Sách", "description": "Sách", "columns": [{"name": "book_id", "type": "INT", "description": "Mã sách", "isPk": true, "isFk": false}, {"name": "title", "type": "VARCHAR(200)", "description": "Tên sách", "isPk": false, "isFk": false}, {"name": "isbn", "type": "VARCHAR(13)", "description": "Mã ISBN duy nhất", "isPk": false, "isFk": false}, {"name": "published_year", "type": "INT", "description": "Năm xuất bản", "isPk": false, "isFk": false}, {"name": "price", "type": "NUMERIC(12,2)", "description": "Giá bán (VND)", "isPk": false, "isFk": false}, {"name": "stock_quantity", "type": "INT", "description": "Số lượng tồn kho", "isPk": false, "isFk": false}, {"name": "publisher_id", "type": "INT", "description": "Nhà xuất bản", "isPk": false, "isFk": true, "references": "publishers.publisher_id"}, {"name": "category_id", "type": "INT", "description": "Thể loại", "isPk": false, "isFk": true, "references": "categories.category_id"}]}, {"name": "book_authors", "vietnameseName": "Tác giả của sách", "description": "Tác giả của sách", "columns": [{"name": "book_id", "type": "INT", "description": "Sách", "isPk": true, "isFk": true, "references": "books.book_id"}, {"name": "author_id", "type": "INT", "description": "Tác giả", "isPk": true, "isFk": true, "references": "authors.author_id"}, {"name": "author_order", "type": "INT", "description": "Thứ tự ghi tên tác giả", "isPk": false, "isFk": false}]}]}$schema$::jsonb, $seed$-- Dữ liệu giả lập về sách: 5 bảng, đúng 50 bản ghi. Tên sách và đơn vị là dữ liệu mẫu.

CREATE TABLE authors (
    author_id INT NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    country VARCHAR(60) NOT NULL,
    birth_year INT NOT NULL,
    PRIMARY KEY (author_id)
);

CREATE TABLE publishers (
    publisher_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    city VARCHAR(60) NOT NULL,
    PRIMARY KEY (publisher_id)
);

CREATE TABLE categories (
    category_id INT NOT NULL,
    name VARCHAR(80) NOT NULL,
    PRIMARY KEY (category_id)
);

CREATE TABLE books (
    book_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    isbn VARCHAR(13) NOT NULL UNIQUE,
    published_year INT NOT NULL,
    price NUMERIC(12,2) NOT NULL CHECK (price >= 0),
    stock_quantity INT NOT NULL CHECK (stock_quantity >= 0),
    publisher_id INT NOT NULL REFERENCES publishers(publisher_id),
    category_id INT NOT NULL REFERENCES categories(category_id),
    PRIMARY KEY (book_id)
);

CREATE TABLE book_authors (
    book_id INT NOT NULL REFERENCES books(book_id),
    author_id INT NOT NULL REFERENCES authors(author_id),
    author_order INT NOT NULL CHECK (author_order > 0),
    PRIMARY KEY (book_id, author_id)
);

INSERT INTO authors (author_id, full_name, country, birth_year) VALUES
(1, 'Nguyễn Minh An', 'Việt Nam', 1980),
(2, 'Trần Thu Hà', 'Việt Nam', 1987),
(3, 'Lê Hoàng Nam', 'Việt Nam', 1975),
(4, 'Phạm Ngọc Linh', 'Việt Nam', 1990),
(5, 'Đỗ Quốc Bảo', 'Việt Nam', 1983);

INSERT INTO publishers (publisher_id, name, city) VALUES
(1, 'Tri Thức Mới', 'Hà Nội'),
(2, 'Ánh Dương', 'TP. Hồ Chí Minh'),
(3, 'Hành Trang', 'Đà Nẵng'),
(4, 'Trang Sách Xanh', 'Hà Nội'),
(5, 'Cánh Diều', 'Huế');

INSERT INTO categories (category_id, name) VALUES
(1, 'Công nghệ'),
(2, 'Văn học'),
(3, 'Kinh doanh'),
(4, 'Khoa học'),
(5, 'Thiếu nhi');

INSERT INTO books (book_id, title, isbn, published_year, price, stock_quantity, publisher_id, category_id) VALUES
(1, 'SQL từ cơ bản đến thực hành', '9780000000001', 2019, 99000, 3, 1, 1),
(2, 'Thiết kế cơ sở dữ liệu', '9780000000002', 2020, 120000, 8, 2, 1),
(3, 'Python cho người mới bắt đầu', '9780000000003', 2021, 145000, 15, 3, 1),
(4, 'Mùa hạ bên ô cửa', '9780000000004', 2022, 180000, 24, 4, 2),
(5, 'Những con đường cũ', '9780000000005', 2023, 75000, 0, 1, 2),
(6, 'Thành phố trong mưa', '9780000000006', 2024, 99000, 3, 2, 2),
(7, 'Khởi nghiệp từ trang giấy', '9780000000007', 2025, 120000, 8, 3, 3),
(8, 'Quản lý tài chính cá nhân', '9780000000008', 2018, 145000, 15, 4, 3),
(9, 'Nghệ thuật làm việc nhóm', '9780000000009', 2019, 180000, 24, 1, 3),
(10, 'Khám phá bầu trời đêm', '9780000000010', 2020, 75000, 0, 2, 4),
(11, 'Thế giới của những hạt nhỏ', '9780000000011', 2021, 99000, 3, 3, 4),
(12, 'Hành tinh xanh của chúng ta', '9780000000012', 2022, 120000, 8, 4, 4),
(13, 'Chuyến phiêu lưu của Mít', '9780000000013', 2023, 145000, 15, 1, 5),
(14, 'Khu vườn biết nói', '9780000000014', 2024, 180000, 24, 2, 5),
(15, 'Bí mật thư viện nhỏ', '9780000000015', 2025, 75000, 0, 3, 5);

INSERT INTO book_authors (book_id, author_id, author_order) VALUES
(1, 1, 1),
(2, 2, 1),
(3, 3, 1),
(4, 4, 1),
(5, 5, 1),
(6, 1, 1),
(7, 2, 1),
(8, 3, 1),
(9, 4, 1),
(10, 5, 1),
(11, 1, 1),
(12, 2, 1),
(13, 3, 1),
(14, 4, 1),
(15, 5, 1),
(1, 2, 2),
(2, 3, 2),
(3, 4, 2),
(4, 5, 2),
(5, 1, 2);
$seed$, 5, 0)
ON CONFLICT (slug) DO NOTHING;
COMMIT;
