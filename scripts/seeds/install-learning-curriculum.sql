-- Cài đặt thủ công nội dung Learning. Không chạy khi ứng dụng khởi động.
BEGIN;
CREATE TEMP TABLE _learning_progress_before ON COMMIT DROP AS SELECT * FROM user_lesson_progress;
CREATE TEMP TABLE _learning_ids_before ON COMMIT DROP AS SELECT id,slug FROM lessons;
INSERT INTO course_categories (id,name,description,sort_order) SELECT 'b50b9855-687c-5584-8692-d59299d340da','SQL cơ bản','Đọc SQL từ nền tảng: SELECT, lọc, sắp xếp, NULL, hàm, khóa và thao tác dữ liệu. Ví dụ thống nhất với database Quản lý Sách & Tác giả.',1 WHERE NOT EXISTS(SELECT 1 FROM course_categories WHERE lower(trim(name))=lower('SQL cơ bản'));
UPDATE course_categories SET description='Đọc SQL từ nền tảng: SELECT, lọc, sắp xếp, NULL, hàm, khóa và thao tác dữ liệu. Ví dụ thống nhất với database Quản lý Sách & Tác giả.',sort_order=1 WHERE lower(trim(name))=lower('SQL cơ bản');
INSERT INTO course_categories (id,name,description,sort_order) SELECT 'e8f9f73b-f3d2-5e01-9d8d-b7a95383a9b6','SQL nâng cao','Từ JOIN và GROUP BY đến truy vấn con, CTE, hàm cửa sổ, tối ưu và giao dịch. Chuẩn bị cho 30 bài thực hành sách.',2 WHERE NOT EXISTS(SELECT 1 FROM course_categories WHERE lower(trim(name))=lower('SQL nâng cao'));
UPDATE course_categories SET description='Từ JOIN và GROUP BY đến truy vấn con, CTE, hàm cửa sổ, tối ưu và giao dịch. Chuẩn bị cho 30 bài thực hành sách.',sort_order=2 WHERE lower(trim(name))=lower('SQL nâng cao');
INSERT INTO lessons (id,category_id,title,slug,description,objectives,practice_topic_code,sort_order) SELECT '9363e35a-135b-5fff-85a3-9423a05ba1ee',c.id,'01. SELECT và cách đọc một cơ sở dữ liệu','khai-niem-select-va-truy-van-toan-bo-bang','Phân biệt bảng, cột, dòng và tập kết quả; Viết SELECT FROM để đọc dữ liệu; Đọc đúng cấu trúc database sách trước khi truy vấn.','["Phân biệt bảng, cột, dòng và tập kết quả", "Viết SELECT FROM để đọc dữ liệu", "Đọc đúng cấu trúc database sách trước khi truy vấn"]'::jsonb,'SQL_BASIC',1 FROM course_categories c WHERE lower(trim(c.name))=lower('SQL cơ bản') ORDER BY c.created_at LIMIT 1 ON CONFLICT (slug) DO UPDATE SET category_id=EXCLUDED.category_id,title=EXCLUDED.title,description=EXCLUDED.description,objectives=EXCLUDED.objectives,practice_topic_code=EXCLUDED.practice_topic_code,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=1 ORDER BY s.created_at LIMIT 1),'51618127-da14-5e37-aa68-5fe44a7c0d77'::uuid),l.id,'1. Mục tiêu và chuẩn bị','### Sau bài này bạn có thể
- Phân biệt bảng, cột, dòng và tập kết quả
- Viết SELECT FROM để đọc dữ liệu
- Đọc đúng cấu trúc database sách trước khi truy vấn

### Kiến thức cần có
Không yêu cầu biết SQL trước; bắt đầu từ việc đọc bảng và bản ghi.

### Cách học
Đọc lý thuyết, dự đoán kết quả SQL trước khi chạy, sau đó đối chiếu giải thích. Các ví dụ truy vấn dùng bộ sách với 15 sách, 5 tác giả, 5 nhà xuất bản, 5 thể loại và 20 liên kết sách–tác giả. Một đầu sách có thể có nhiều tác giả; giá tính theo VND. Không cộng lẫn số đầu sách với số cuốn tồn kho.',NULL,'{"type": "info", "title": "Môi trường ví dụ", "content": "Cú pháp theo PostgreSQL 16. Ví dụ CREATE/UPDATE/DELETE và EXPLAIN được đánh dấu riêng, không phải đáp án cho phòng chỉ cho phép SELECT."}'::jsonb,1 FROM lessons l WHERE l.slug='khai-niem-select-va-truy-van-toan-bo-bang' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=2 ORDER BY s.created_at LIMIT 1),'7ca83595-30a8-50da-a709-6764674c7d5f'::uuid),l.id,'2. Khái niệm và cách suy luận','SQL là ngôn ngữ làm việc với dữ liệu quan hệ. Một bảng mô tả một loại đối tượng; cột mô tả thuộc tính; mỗi dòng là một bản ghi. Bảng `books` có một dòng cho mỗi đầu sách, còn `book_authors` có một dòng cho mỗi cặp sách–tác giả. Vì vậy, 20 dòng trong `book_authors` không có nghĩa là có 20 cuốn sách khác nhau.

`SELECT` xác định thông tin cần trả về; `FROM` xác định nguồn dữ liệu. Kết quả SELECT là một tập dòng dùng để đọc hoặc xử lý tiếp, không phải một bảng mới được lưu vĩnh viễn. SELECT thông thường không thay đổi dữ liệu gốc. Câu lệnh có thể xuống dòng và kết thúc bằng dấu chấm phẩy. Từ khóa thường viết hoa để dễ đọc; tên bảng/cột dùng chữ thường theo schema.

Trong database sách: `books` có 15 dòng; `authors`, `publishers`, `categories` mỗi bảng có 5 dòng; `book_authors` có 20 dòng. Có 50 bản ghi tổng cộng. Khi chưa dùng ORDER BY, hệ quản trị không cam kết thứ tự dòng. Hãy xác định mỗi dòng kết quả đại diện cho đối tượng gì trước khi chọn cột hoặc nối bảng.',NULL,NULL,2 FROM lessons l WHERE l.slug='khai-niem-select-va-truy-van-toan-bo-bang' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=3 ORDER BY s.created_at LIMIT 1),'84fbd028-f4de-5f91-aca7-3e62f4644515'::uuid),l.id,'3. Đọc toàn bộ một bảng','### Phân tích ví dụ
Có 15 dòng và 8 cột, từ book_id đến category_id. Dòng book_id = 1 là SQL từ cơ bản đến thực hành, giá 99000. Dấu * có nghĩa là tất cả cột, không có nghĩa là tất cả bảng.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **15**.

| book_id | title | isbn | published_year | price | stock_quantity | publisher_id | category_id |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | SQL từ cơ bản đến thực hành | 9780000000001 | 2019 | 99000.0 | 3 | 1 | 1 |
| 2 | Thiết kế cơ sở dữ liệu | 9780000000002 | 2020 | 120000.0 | 8 | 2 | 1 |
| 3 | Python cho người mới bắt đầu | 9780000000003 | 2021 | 145000.0 | 15 | 3 | 1 |
| 4 | Mùa hạ bên ô cửa | 9780000000004 | 2022 | 180000.0 | 24 | 4 | 2 |
| 5 | Những con đường cũ | 9780000000005 | 2023 | 75000.0 | 0 | 1 | 2 |

Bảng trên minh họa 5 dòng đầu; truy vấn trả đầy đủ 15 dòng.','SELECT *
FROM books
ORDER BY book_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,3 FROM lessons l WHERE l.slug='khai-niem-select-va-truy-van-toan-bo-bang' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=4 ORDER BY s.created_at LIMIT 1),'d08746da-a6fe-59c8-a266-a8e68e47dea3'::uuid),l.id,'4. Khám phá bảng tác giả','### Phân tích ví dụ
Có 5 dòng; tác giả đầu tiên là Nguyễn Minh An. Chỉ ba cột được chọn xuất hiện trong kết quả. Cần dùng đúng tên full_name, không tự đoán thành author_name khi đọc bảng gốc.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **5**.

| author_id | full_name | country |
| --- | --- | --- |
| 1 | Nguyễn Minh An | Việt Nam |
| 2 | Trần Thu Hà | Việt Nam |
| 3 | Lê Hoàng Nam | Việt Nam |
| 4 | Phạm Ngọc Linh | Việt Nam |
| 5 | Đỗ Quốc Bảo | Việt Nam |','SELECT author_id, full_name, country
FROM authors
ORDER BY author_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,4 FROM lessons l WHERE l.slug='khai-niem-select-va-truy-van-toan-bo-bang' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=5 ORDER BY s.created_at LIMIT 1),'42871926-9ce1-5a6b-8f56-137d6346a22f'::uuid),l.id,'5. Lỗi thường gặp và cách tránh','**1.** Tên bảng không đặt trong dấu nháy đơn: books là định danh, còn ''books'' là một chuỗi.

**2.** SELECT * tiện khi khám phá nhưng khi nộp bài cần liệt kê đúng các cột đề yêu cầu.

**3.** Không suy ra thứ tự dữ liệu từ thứ tự INSERT hoặc cách giao diện đang hiển thị.

### Quy trình kiểm tra
Đọc lại điều kiện tại đúng giá trị ranh giới; kiểm tra NULL, dòng không có đối tác và trường hợp đồng giá. Một câu SQL chạy được chưa chứng minh kết quả đã đúng với yêu cầu.',NULL,'{"type": "warning", "title": "Kiểm tra ý nghĩa trước cú pháp", "content": "Tên bảng không đặt trong dấu nháy đơn: books là định danh, còn ''books'' là một chuỗi."}'::jsonb,5 FROM lessons l WHERE l.slug='khai-niem-select-va-truy-van-toan-bo-bang' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=6 ORDER BY s.created_at LIMIT 1),'8a54c42e-ff63-50ed-8743-f2311110051f'::uuid),l.id,'6. Tự kiểm tra và chuyển sang thực hành','**Câu 1: SELECT có làm thay đổi giá sách không?**

Tự trả lời trước khi đọc. **Giải thích:** Không. SELECT thông thường chỉ đọc; UPDATE mới sửa giá trị đã lưu.

**Câu 2: books và book_authors có cùng số dòng không?**

Tự trả lời trước khi đọc. **Giải thích:** Không: lần lượt 15 và 20, do một sách có thể có nhiều tác giả.

### Thực hành sau khi học
Ôn cách đọc books, publishers và categories trước các bài JOIN dễ.

[Mở database sách để thực hành](/practice/quan-ly-sach-va-tac-gia)

### Tài liệu tham khảo
- [Tài liệu PostgreSQL 16](https://www.postgresql.org/docs/16/queries-select-lists.html)',NULL,NULL,6 FROM lessons l WHERE l.slug='khai-niem-select-va-truy-van-toan-bo-bang' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lessons (id,category_id,title,slug,description,objectives,practice_topic_code,sort_order) SELECT '79c202a2-b82e-520a-b0d8-f7dfd04baab4',c.id,'02. Chọn cột, biểu thức và bí danh AS','truy-van-cot-cu-the-va-dat-bi-danh-as','Chọn đúng cột và thứ tự đầu ra; Đặt bí danh cho cột và bảng; Tính giá trị mới mà không sửa dữ liệu gốc.','["Chọn đúng cột và thứ tự đầu ra", "Đặt bí danh cho cột và bảng", "Tính giá trị mới mà không sửa dữ liệu gốc"]'::jsonb,'SQL_BASIC',2 FROM course_categories c WHERE lower(trim(c.name))=lower('SQL cơ bản') ORDER BY c.created_at LIMIT 1 ON CONFLICT (slug) DO UPDATE SET category_id=EXCLUDED.category_id,title=EXCLUDED.title,description=EXCLUDED.description,objectives=EXCLUDED.objectives,practice_topic_code=EXCLUDED.practice_topic_code,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=1 ORDER BY s.created_at LIMIT 1),'1db12250-7ea4-5152-9263-744ee07dd7d1'::uuid),l.id,'1. Mục tiêu và chuẩn bị','### Sau bài này bạn có thể
- Chọn đúng cột và thứ tự đầu ra
- Đặt bí danh cho cột và bảng
- Tính giá trị mới mà không sửa dữ liệu gốc

### Kiến thức cần có
Đã học các bài đứng trước trong mục SQL cơ bản; đọc ví dụ theo đúng tên bảng/cột của bộ sách.

### Cách học
Đọc lý thuyết, dự đoán kết quả SQL trước khi chạy, sau đó đối chiếu giải thích. Các ví dụ truy vấn dùng bộ sách với 15 sách, 5 tác giả, 5 nhà xuất bản, 5 thể loại và 20 liên kết sách–tác giả. Một đầu sách có thể có nhiều tác giả; giá tính theo VND. Không cộng lẫn số đầu sách với số cuốn tồn kho.',NULL,'{"type": "info", "title": "Môi trường ví dụ", "content": "Cú pháp theo PostgreSQL 16. Ví dụ CREATE/UPDATE/DELETE và EXPLAIN được đánh dấu riêng, không phải đáp án cho phòng chỉ cho phép SELECT."}'::jsonb,1 FROM lessons l WHERE l.slug='truy-van-cot-cu-the-va-dat-bi-danh-as' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=2 ORDER BY s.created_at LIMIT 1),'ff67c18f-11bb-546e-98b5-f801dd9cbda4'::uuid),l.id,'2. Khái niệm và cách suy luận','Danh sách sau SELECT quyết định số cột, thứ tự và tên cột đầu ra. Chọn cột cụ thể giúp kết quả ổn định khi schema thay đổi và giảm dữ liệu cần truyền về ứng dụng. Điều này có thể hỗ trợ hiệu năng, nhưng không bảo đảm mọi truy vấn sẽ nhanh hơn: còn phụ thuộc kế hoạch thực thi, chỉ mục và lượng dữ liệu.

`AS` đặt tên cho một biểu thức hoặc đổi tên cột trong kết quả của truy vấn hiện tại. Nó không đổi cấu trúc bảng. `books AS b` tạo bí danh bảng để viết `b.title`, đặc biệt hữu ích khi JOIN. Khi đã đặt bí danh, nên dùng bí danh nhất quán. Tên `publisher_name` ở đề bài thường là bí danh cho `publishers.name`, không phải cột có sẵn trong bảng.

Có thể dùng phép nhân, cộng hoặc hàm trong SELECT. Ví dụ giá trị tồn kho là giá một cuốn nhân số cuốn còn lại, không phải chỉ giá bán. Chuỗi dùng nháy đơn; bí danh chứa khoảng trắng dùng nháy kép trong PostgreSQL. Với bài chấm tự động, ưu tiên bí danh tiếng Anh chữ thường đúng đề. Bí danh SELECT thường chưa dùng được trong WHERE cùng cấp, vì điều kiện lọc được xử lý trước danh sách SELECT.',NULL,NULL,2 FROM lessons l WHERE l.slug='truy-van-cot-cu-the-va-dat-bi-danh-as' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=3 ORDER BY s.created_at LIMIT 1),'253a3a55-69d9-54c1-b480-dc9398dfa7fd'::uuid),l.id,'3. Tạo cột báo cáo rõ nghĩa','### Phân tích ví dụ
Sách số 1 có inventory_value = 297000 vì 99000 × 3. Giá và tồn kho trong bảng không bị thay đổi.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **15**.

| book_id | book_title | inventory_value |
| --- | --- | --- |
| 1 | SQL từ cơ bản đến thực hành | 297000.0 |
| 2 | Thiết kế cơ sở dữ liệu | 960000.0 |
| 3 | Python cho người mới bắt đầu | 2175000.0 |
| 4 | Mùa hạ bên ô cửa | 4320000.0 |
| 5 | Những con đường cũ | 0.0 |

Bảng trên minh họa 5 dòng đầu; truy vấn trả đầy đủ 15 dòng.','SELECT b.book_id, b.title AS book_title,
       b.price * b.stock_quantity AS inventory_value
FROM books AS b
ORDER BY b.book_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,3 FROM lessons l WHERE l.slug='truy-van-cot-cu-the-va-dat-bi-danh-as' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=4 ORDER BY s.created_at LIMIT 1),'83eb79ce-6593-5575-b376-69d2db816e6a'::uuid),l.id,'4. Đổi tên cột ở đầu ra','### Phân tích ví dụ
Tên cột thứ hai là publisher_name. Kết quả có 5 nhà xuất bản, bao gồm Cánh Diều dù đơn vị này chưa có sách.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **5**.

| publisher_id | publisher_name | city |
| --- | --- | --- |
| 1 | Tri Thức Mới | Hà Nội |
| 2 | Ánh Dương | TP. Hồ Chí Minh |
| 3 | Hành Trang | Đà Nẵng |
| 4 | Trang Sách Xanh | Hà Nội |
| 5 | Cánh Diều | Huế |','SELECT publisher_id, name AS publisher_name, city
FROM publishers
ORDER BY publisher_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,4 FROM lessons l WHERE l.slug='truy-van-cot-cu-the-va-dat-bi-danh-as' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=5 ORDER BY s.created_at LIMIT 1),'d37759e9-a7b8-5d36-96e6-9ce42ec71280'::uuid),l.id,'5. Lỗi thường gặp và cách tránh','**1.** Không dùng bí danh ở WHERE cùng cấp; hãy dùng biểu thức gốc hoặc bọc bằng truy vấn con.

**2.** Viết AS không tạo cột mới trong bảng và không thay thế ALTER TABLE.

**3.** Tên cột và thứ tự cột là một phần của yêu cầu bài làm, không chỉ là hình thức trình bày.

### Quy trình kiểm tra
Đọc lại điều kiện tại đúng giá trị ranh giới; kiểm tra NULL, dòng không có đối tác và trường hợp đồng giá. Một câu SQL chạy được chưa chứng minh kết quả đã đúng với yêu cầu.',NULL,'{"type": "warning", "title": "Kiểm tra ý nghĩa trước cú pháp", "content": "Không dùng bí danh ở WHERE cùng cấp; hãy dùng biểu thức gốc hoặc bọc bằng truy vấn con."}'::jsonb,5 FROM lessons l WHERE l.slug='truy-van-cot-cu-the-va-dat-bi-danh-as' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=6 ORDER BY s.created_at LIMIT 1),'a17b91fe-d98d-52c3-8b5b-1eedc06ab572'::uuid),l.id,'6. Tự kiểm tra và chuyển sang thực hành','**Câu 1: Có thể viết price * stock_quantity AS value không?**

Tự trả lời trước khi đọc. **Giải thích:** Có, nhưng nếu đề yêu cầu inventory_value thì phải dùng đúng tên đó.

**Câu 2: AS có bắt buộc với bí danh cột không?**

Tự trả lời trước khi đọc. **Giải thích:** PostgreSQL cho phép bỏ AS trong nhiều trường hợp, nhưng viết rõ AS giúp tránh nhầm lẫn.

### Thực hành sau khi học
Chuẩn bị các bí danh publisher_name, category_name, author_name cho bài JOIN.

[Mở database sách để thực hành](/practice/quan-ly-sach-va-tac-gia)

### Tài liệu tham khảo
- [Tài liệu PostgreSQL 16](https://www.postgresql.org/docs/16/queries-select-lists.html)',NULL,NULL,6 FROM lessons l WHERE l.slug='truy-van-cot-cu-the-va-dat-bi-danh-as' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lessons (id,category_id,title,slug,description,objectives,practice_topic_code,sort_order) SELECT '2f4ce4fd-45fb-5da0-a20c-4444f303fc65',c.id,'03. DISTINCT và cách hiểu dữ liệu trùng','loc-gia-tri-duy-nhat-voi-distinct','Loại bản sao ở tập kết quả; Phân biệt DISTINCT một cột và nhiều cột; Không dùng DISTINCT để che lỗi JOIN.','["Loại bản sao ở tập kết quả", "Phân biệt DISTINCT một cột và nhiều cột", "Không dùng DISTINCT để che lỗi JOIN"]'::jsonb,'SQL_BASIC',3 FROM course_categories c WHERE lower(trim(c.name))=lower('SQL cơ bản') ORDER BY c.created_at LIMIT 1 ON CONFLICT (slug) DO UPDATE SET category_id=EXCLUDED.category_id,title=EXCLUDED.title,description=EXCLUDED.description,objectives=EXCLUDED.objectives,practice_topic_code=EXCLUDED.practice_topic_code,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=1 ORDER BY s.created_at LIMIT 1),'38f796d6-81c4-53fb-8ce0-326c64b9c755'::uuid),l.id,'1. Mục tiêu và chuẩn bị','### Sau bài này bạn có thể
- Loại bản sao ở tập kết quả
- Phân biệt DISTINCT một cột và nhiều cột
- Không dùng DISTINCT để che lỗi JOIN

### Kiến thức cần có
Đã học các bài đứng trước trong mục SQL cơ bản; đọc ví dụ theo đúng tên bảng/cột của bộ sách.

### Cách học
Đọc lý thuyết, dự đoán kết quả SQL trước khi chạy, sau đó đối chiếu giải thích. Các ví dụ truy vấn dùng bộ sách với 15 sách, 5 tác giả, 5 nhà xuất bản, 5 thể loại và 20 liên kết sách–tác giả. Một đầu sách có thể có nhiều tác giả; giá tính theo VND. Không cộng lẫn số đầu sách với số cuốn tồn kho.',NULL,'{"type": "info", "title": "Môi trường ví dụ", "content": "Cú pháp theo PostgreSQL 16. Ví dụ CREATE/UPDATE/DELETE và EXPLAIN được đánh dấu riêng, không phải đáp án cho phòng chỉ cho phép SELECT."}'::jsonb,1 FROM lessons l WHERE l.slug='loc-gia-tri-duy-nhat-voi-distinct' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=2 ORDER BY s.created_at LIMIT 1),'ab344c5c-d480-5801-837c-ad0b944e8d06'::uuid),l.id,'2. Khái niệm và cách suy luận','SELECT mặc định giữ các dòng trùng nhau ở đầu ra. `DISTINCT` loại những dòng có toàn bộ giá trị cột đã chọn giống nhau. Nếu chỉ chọn publisher_id, nhiều sách cùng nhà xuất bản được gộp thành một mã. Nếu chọn thêm book_id, các dòng lại khác nhau vì mỗi sách có mã riêng.

DISTINCT không xóa dữ liệu trong bảng và không cam kết sắp xếp. Nó được áp dụng trên toàn bộ danh sách SELECT, không chỉ cột đứng ngay sau từ khóa. Với DISTINCT, các giá trị NULL trong cùng tổ hợp cột được xem là trùng cho mục đích loại bản sao, dù phép so sánh NULL = NULL không trả về TRUE.

Trước khi thêm DISTINCT, hãy hỏi mỗi dòng mong muốn đại diện cho sách, tác giả hay một cặp sách–tác giả. Truy vấn sách JOIN tác giả trả về nhiều dòng cho sách có đồng tác giả là đúng mô hình. Nếu bài yêu cầu chỉ kiểm tra có tác giả phù hợp, EXISTS thường diễn đạt tốt hơn JOIN rồi loại bản sao. DISTINCT chỉ giải quyết trùng ở đầu ra; nó không sửa nguyên nhân một phép nối sai làm nhân bản dữ liệu.',NULL,NULL,2 FROM lessons l WHERE l.slug='loc-gia-tri-duy-nhat-voi-distinct' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=3 ORDER BY s.created_at LIMIT 1),'9e7a75b2-1824-5b40-b8ef-11edd35a9ddc'::uuid),l.id,'3. Nhà xuất bản đang có sách','### Phân tích ví dụ
Có 4 mã: 1, 2, 3, 4. Nhà xuất bản số 5 vẫn tồn tại trong publishers nhưng chưa xuất hiện ở books.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **4**.

| publisher_id |
| --- |
| 1 |
| 2 |
| 3 |
| 4 |','SELECT DISTINCT publisher_id
FROM books
ORDER BY publisher_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,3 FROM lessons l WHERE l.slug='loc-gia-tri-duy-nhat-voi-distinct' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=4 ORDER BY s.created_at LIMIT 1),'d9384189-2638-51be-b04d-571fd0c34079'::uuid),l.id,'4. Các cặp nhà xuất bản và thể loại','### Phân tích ví dụ
Có 15 cặp khác nhau trên bộ dữ liệu hiện tại. DISTINCT xét cả cặp chứ không loại trùng publisher_id riêng lẻ.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **15**.

| publisher_id | category_id |
| --- | --- |
| 1 | 1 |
| 1 | 2 |
| 1 | 3 |
| 1 | 5 |
| 2 | 1 |

Bảng trên minh họa 5 dòng đầu; truy vấn trả đầy đủ 15 dòng.','SELECT DISTINCT publisher_id, category_id
FROM books
ORDER BY publisher_id, category_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,4 FROM lessons l WHERE l.slug='loc-gia-tri-duy-nhat-voi-distinct' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=5 ORDER BY s.created_at LIMIT 1),'6c50612f-3db8-501f-a5f3-f8861623f459'::uuid),l.id,'5. Lỗi thường gặp và cách tránh','**1.** Không viết SELECT DISTINCT publisher_id, title nếu mục tiêu chỉ là danh sách mã nhà xuất bản.

**2.** Không mặc định DISTINCT làm kết quả có thứ tự; hãy dùng ORDER BY.

**3.** Nếu DISTINCT bất ngờ giảm nhiều dòng sau JOIN, kiểm tra lại khóa nối trước.

### Quy trình kiểm tra
Đọc lại điều kiện tại đúng giá trị ranh giới; kiểm tra NULL, dòng không có đối tác và trường hợp đồng giá. Một câu SQL chạy được chưa chứng minh kết quả đã đúng với yêu cầu.',NULL,'{"type": "warning", "title": "Kiểm tra ý nghĩa trước cú pháp", "content": "Không viết SELECT DISTINCT publisher_id, title nếu mục tiêu chỉ là danh sách mã nhà xuất bản."}'::jsonb,5 FROM lessons l WHERE l.slug='loc-gia-tri-duy-nhat-voi-distinct' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=6 ORDER BY s.created_at LIMIT 1),'004ed785-7482-5e52-8bd4-b3a535e66a46'::uuid),l.id,'6. Tự kiểm tra và chuyển sang thực hành','**Câu 1: DISTINCT có xóa sách khỏi books không?**

Tự trả lời trước khi đọc. **Giải thích:** Không, nó chỉ loại dòng lặp trong kết quả truy vấn.

**Câu 2: Vì sao thêm book_id làm DISTINCT không còn giảm dòng?**

Tự trả lời trước khi đọc. **Giải thích:** Vì book_id phân biệt từng sách nên mỗi tổ hợp giá trị đã duy nhất.

### Thực hành sau khi học
Hữu ích khi một tác giả có nhiều sách hoặc một phép JOIN tạo nhiều cặp hợp lệ.

[Mở database sách để thực hành](/practice/quan-ly-sach-va-tac-gia)

### Tài liệu tham khảo
- [Tài liệu PostgreSQL 16](https://www.postgresql.org/docs/16/queries-select-lists.html)',NULL,NULL,6 FROM lessons l WHERE l.slug='loc-gia-tri-duy-nhat-voi-distinct' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lessons (id,category_id,title,slug,description,objectives,practice_topic_code,sort_order) SELECT 'c667bb68-d95c-59a6-b7e1-88b8e344ba07',c.id,'04. WHERE và các phép so sánh','loc-du-lieu-dieu-kien-voi-mende-where','Lọc bản ghi trước khi tổng hợp; Dùng phép so sánh số và chuỗi; Kết hợp điều kiện đúng với yêu cầu nghiệp vụ.','["Lọc bản ghi trước khi tổng hợp", "Dùng phép so sánh số và chuỗi", "Kết hợp điều kiện đúng với yêu cầu nghiệp vụ"]'::jsonb,'SQL_BASIC',4 FROM course_categories c WHERE lower(trim(c.name))=lower('SQL cơ bản') ORDER BY c.created_at LIMIT 1 ON CONFLICT (slug) DO UPDATE SET category_id=EXCLUDED.category_id,title=EXCLUDED.title,description=EXCLUDED.description,objectives=EXCLUDED.objectives,practice_topic_code=EXCLUDED.practice_topic_code,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=1 ORDER BY s.created_at LIMIT 1),'2366f79c-b905-5435-809a-dbf7c6b71502'::uuid),l.id,'1. Mục tiêu và chuẩn bị','### Sau bài này bạn có thể
- Lọc bản ghi trước khi tổng hợp
- Dùng phép so sánh số và chuỗi
- Kết hợp điều kiện đúng với yêu cầu nghiệp vụ

### Kiến thức cần có
Đã học các bài đứng trước trong mục SQL cơ bản; đọc ví dụ theo đúng tên bảng/cột của bộ sách.

### Cách học
Đọc lý thuyết, dự đoán kết quả SQL trước khi chạy, sau đó đối chiếu giải thích. Các ví dụ truy vấn dùng bộ sách với 15 sách, 5 tác giả, 5 nhà xuất bản, 5 thể loại và 20 liên kết sách–tác giả. Một đầu sách có thể có nhiều tác giả; giá tính theo VND. Không cộng lẫn số đầu sách với số cuốn tồn kho.',NULL,'{"type": "info", "title": "Môi trường ví dụ", "content": "Cú pháp theo PostgreSQL 16. Ví dụ CREATE/UPDATE/DELETE và EXPLAIN được đánh dấu riêng, không phải đáp án cho phòng chỉ cho phép SELECT."}'::jsonb,1 FROM lessons l WHERE l.slug='loc-du-lieu-dieu-kien-voi-mende-where' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=2 ORDER BY s.created_at LIMIT 1),'0443f669-bb13-5a8d-906a-10e2ccf9fdac'::uuid),l.id,'2. Khái niệm và cách suy luận','WHERE chọn những dòng làm điều kiện trả về TRUE. Dòng có điều kiện FALSE hoặc UNKNOWN bị loại. Đây là bộ lọc theo bản ghi, không phải bộ lọc theo nhóm. Ví dụ price >= 120000 xét giá của từng sách; còn AVG(price) > 120000 là điều kiện cho kết quả tổng hợp và cần HAVING hoặc một cấp truy vấn khác.

Các phép so sánh thường gặp là =, <>, >, >=, <, <=. So sánh bằng dùng một dấu =, khác với == trong một số ngôn ngữ lập trình. Giá là NUMERIC nên viết số không có dấu phân tách hàng nghìn: 120000, không viết 120.000 nếu ý định là một trăm hai mươi nghìn. Chuỗi tiếng Việt đặt trong nháy đơn và phải đúng dữ liệu lưu trong bảng.

WHERE không thay đổi giá trị gốc. Có thể chọn ít cột hơn số cột được dùng làm điều kiện. Thứ tự viết phổ biến là SELECT, FROM, WHERE, ORDER BY; hãy xuống dòng để dễ đọc. Cần kiểm tra cả giá trị đúng bằng ngưỡng: lớn hơn 120000 khác với từ 120000 trở lên. Khi có NULL, dùng IS NULL thay cho phép bằng; phần NULL sẽ giải thích riêng.',NULL,NULL,2 FROM lessons l WHERE l.slug='loc-du-lieu-dieu-kien-voi-mende-where' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=3 ORDER BY s.created_at LIMIT 1),'b1031ea4-0c6e-5fe4-8a45-b2785e3710d2'::uuid),l.id,'3. Lọc giá từ một ngưỡng','### Phân tích ví dụ
Có 9 sách. Các sách giá đúng 120000 vẫn được lấy vì dùng >=, ví dụ Thiết kế cơ sở dữ liệu.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **9**.

| book_id | title | price |
| --- | --- | --- |
| 2 | Thiết kế cơ sở dữ liệu | 120000.0 |
| 3 | Python cho người mới bắt đầu | 145000.0 |
| 4 | Mùa hạ bên ô cửa | 180000.0 |
| 7 | Khởi nghiệp từ trang giấy | 120000.0 |
| 8 | Quản lý tài chính cá nhân | 145000.0 |

Bảng trên minh họa 5 dòng đầu; truy vấn trả đầy đủ 9 dòng.','SELECT book_id, title, price
FROM books
WHERE price >= 120000
ORDER BY book_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,3 FROM lessons l WHERE l.slug='loc-du-lieu-dieu-kien-voi-mende-where' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=4 ORDER BY s.created_at LIMIT 1),'983e9dc3-1363-5d7b-8569-93cf062dac8d'::uuid),l.id,'4. Lọc tên nhà xuất bản theo thành phố','### Phân tích ví dụ
Có 2 nhà xuất bản: Tri Thức Mới và Trang Sách Xanh. WHERE làm việc với cột city dù tên đầu ra vẫn giữ nguyên.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **2**.

| publisher_id | name | city |
| --- | --- | --- |
| 1 | Tri Thức Mới | Hà Nội |
| 4 | Trang Sách Xanh | Hà Nội |','SELECT publisher_id, name, city
FROM publishers
WHERE city = ''Hà Nội''
ORDER BY publisher_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,4 FROM lessons l WHERE l.slug='loc-du-lieu-dieu-kien-voi-mende-where' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=5 ORDER BY s.created_at LIMIT 1),'6f2708d8-8856-50dd-a941-69497ef8800f'::uuid),l.id,'5. Lỗi thường gặp và cách tránh','**1.** Không dùng WHERE SUM(...) trong cùng cấp truy vấn; hãy chuyển điều kiện tổng hợp sang HAVING.

**2.** Chuỗi dùng dấu nháy đơn, không dùng nháy kép như dấu bao chuỗi.

**3.** Cần phân biệt > và >=, đặc biệt ở các bài giới hạn giá hoặc số lượng.

### Quy trình kiểm tra
Đọc lại điều kiện tại đúng giá trị ranh giới; kiểm tra NULL, dòng không có đối tác và trường hợp đồng giá. Một câu SQL chạy được chưa chứng minh kết quả đã đúng với yêu cầu.',NULL,'{"type": "warning", "title": "Kiểm tra ý nghĩa trước cú pháp", "content": "Không dùng WHERE SUM(...) trong cùng cấp truy vấn; hãy chuyển điều kiện tổng hợp sang HAVING."}'::jsonb,5 FROM lessons l WHERE l.slug='loc-du-lieu-dieu-kien-voi-mende-where' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=6 ORDER BY s.created_at LIMIT 1),'fbb684d8-3bad-5b98-a189-fdbd0293255b'::uuid),l.id,'6. Tự kiểm tra và chuyển sang thực hành','**Câu 1: Lấy sách hết hàng viết thế nào?**

Tự trả lời trước khi đọc. **Giải thích:** Điều kiện là stock_quantity = 0, không phải stock_quantity IS NULL.

**Câu 2: Có cần SELECT stock_quantity khi dùng nó ở WHERE không?**

Tự trả lời trước khi đọc. **Giải thích:** Không; điều kiện lọc có thể dùng cột không xuất hiện ở đầu ra.

### Thực hành sau khi học
Áp dụng ở các bài JOIN có lọc thể loại, thành phố và giá sách.

[Mở database sách để thực hành](/practice/quan-ly-sach-va-tac-gia)

### Tài liệu tham khảo
- [Tài liệu PostgreSQL 16](https://www.postgresql.org/docs/16/queries-select-lists.html)',NULL,NULL,6 FROM lessons l WHERE l.slug='loc-du-lieu-dieu-kien-voi-mende-where' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lessons (id,category_id,title,slug,description,objectives,practice_topic_code,sort_order) SELECT '9c0ae00f-31a2-525d-9e30-feb2e89eb8ab',c.id,'05. ORDER BY, LIMIT và phân trang','sap-xep-ket-qua-truy-van-voi-order-by','Sắp xếp theo một hoặc nhiều tiêu chí; Giới hạn số dòng mà vẫn có kết quả ổn định; Phân biệt lấy N dòng và lấy N mức giá.','["Sắp xếp theo một hoặc nhiều tiêu chí", "Giới hạn số dòng mà vẫn có kết quả ổn định", "Phân biệt lấy N dòng và lấy N mức giá"]'::jsonb,'SQL_BASIC',5 FROM course_categories c WHERE lower(trim(c.name))=lower('SQL cơ bản') ORDER BY c.created_at LIMIT 1 ON CONFLICT (slug) DO UPDATE SET category_id=EXCLUDED.category_id,title=EXCLUDED.title,description=EXCLUDED.description,objectives=EXCLUDED.objectives,practice_topic_code=EXCLUDED.practice_topic_code,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=1 ORDER BY s.created_at LIMIT 1),'6d2eefa4-2ca9-5a96-ad36-9253d6692b96'::uuid),l.id,'1. Mục tiêu và chuẩn bị','### Sau bài này bạn có thể
- Sắp xếp theo một hoặc nhiều tiêu chí
- Giới hạn số dòng mà vẫn có kết quả ổn định
- Phân biệt lấy N dòng và lấy N mức giá

### Kiến thức cần có
Đã học các bài đứng trước trong mục SQL cơ bản; đọc ví dụ theo đúng tên bảng/cột của bộ sách.

### Cách học
Đọc lý thuyết, dự đoán kết quả SQL trước khi chạy, sau đó đối chiếu giải thích. Các ví dụ truy vấn dùng bộ sách với 15 sách, 5 tác giả, 5 nhà xuất bản, 5 thể loại và 20 liên kết sách–tác giả. Một đầu sách có thể có nhiều tác giả; giá tính theo VND. Không cộng lẫn số đầu sách với số cuốn tồn kho.',NULL,'{"type": "info", "title": "Môi trường ví dụ", "content": "Cú pháp theo PostgreSQL 16. Ví dụ CREATE/UPDATE/DELETE và EXPLAIN được đánh dấu riêng, không phải đáp án cho phòng chỉ cho phép SELECT."}'::jsonb,1 FROM lessons l WHERE l.slug='sap-xep-ket-qua-truy-van-voi-order-by' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=2 ORDER BY s.created_at LIMIT 1),'d2bfcdae-5f0b-592e-8705-15443b53dbdf'::uuid),l.id,'2. Khái niệm và cách suy luận','ORDER BY điều khiển thứ tự kết quả cuối cùng. ASC là tăng dần và là mặc định; DESC là giảm dần. Với nhiều khóa sắp xếp, hệ quản trị so sánh khóa thứ nhất, nếu bằng nhau mới xét khóa tiếp theo. Vì nhiều sách cùng giá, ORDER BY price DESC nên thêm book_id ASC để kết quả ổn định giữa các lần chạy.

LIMIT lấy tối đa một số dòng sau khi sắp xếp; OFFSET bỏ qua một số dòng đầu. LIMIT 3 không có ORDER BY chỉ lấy ba dòng bất kỳ theo kế hoạch thực thi, không có nghĩa là ba sách đầu tiên hoặc ba sách đắt nhất. Khi dữ liệu thay đổi liên tục, phân trang OFFSET có thể thấy dòng lặp hoặc bị bỏ qua; đây là lý do ứng dụng lớn cân nhắc phân trang theo khóa.

Lấy ba sách đắt nhất là một yêu cầu khác với lấy tất cả sách thuộc ba mức giá cao nhất. LIMIT có thể cắt ngang nhóm đồng giá. Với yêu cầu giữ mọi trường hợp đồng hạng, cần truy vấn con hoặc DENSE_RANK ở phần nâng cao. PostgreSQL còn cho phép NULLS FIRST/LAST để chỉ định vị trí NULL thay vì dựa vào mặc định.',NULL,NULL,2 FROM lessons l WHERE l.slug='sap-xep-ket-qua-truy-van-voi-order-by' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=3 ORDER BY s.created_at LIMIT 1),'e2e8f8ab-6e02-5c5c-aeae-63606e7f75ee'::uuid),l.id,'3. Ba sách đắt nhất theo quy tắc rõ ràng','### Phân tích ví dụ
Trả về book_id 4, 9, 14, đều giá 180000. Khóa book_id quyết định thứ tự khi giá bằng nhau.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **3**.

| book_id | title | price |
| --- | --- | --- |
| 4 | Mùa hạ bên ô cửa | 180000.0 |
| 9 | Nghệ thuật làm việc nhóm | 180000.0 |
| 14 | Khu vườn biết nói | 180000.0 |','SELECT book_id, title, price
FROM books
ORDER BY price DESC, book_id ASC
LIMIT 3;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,3 FROM lessons l WHERE l.slug='sap-xep-ket-qua-truy-van-voi-order-by' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=4 ORDER BY s.created_at LIMIT 1),'8945fafb-bff3-5cb4-a7c4-8c8a4b8953de'::uuid),l.id,'4. Trang thứ hai, mỗi trang năm sách','### Phân tích ví dụ
Bỏ qua 5 sách đầu và trả về book_id từ 6 đến 10. OFFSET là số dòng bỏ qua, không phải số trang.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **5**.

| book_id | title |
| --- | --- |
| 6 | Thành phố trong mưa |
| 7 | Khởi nghiệp từ trang giấy |
| 8 | Quản lý tài chính cá nhân |
| 9 | Nghệ thuật làm việc nhóm |
| 10 | Khám phá bầu trời đêm |','SELECT book_id, title
FROM books
ORDER BY book_id
LIMIT 5 OFFSET 5;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,4 FROM lessons l WHERE l.slug='sap-xep-ket-qua-truy-van-voi-order-by' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=5 ORDER BY s.created_at LIMIT 1),'e849b47e-3f31-5458-b4f0-528ed71c47a5'::uuid),l.id,'5. Lỗi thường gặp và cách tránh','**1.** Không đặt LIMIT trước ORDER BY trong cú pháp truy vấn PostgreSQL.

**2.** Mỗi cột có hướng sắp xếp riêng: ORDER BY price DESC, book_id là giá giảm nhưng mã tăng.

**3.** Không dùng LIMIT 1 khi đề yêu cầu tất cả sách đồng giá cao nhất.

### Quy trình kiểm tra
Đọc lại điều kiện tại đúng giá trị ranh giới; kiểm tra NULL, dòng không có đối tác và trường hợp đồng giá. Một câu SQL chạy được chưa chứng minh kết quả đã đúng với yêu cầu.',NULL,'{"type": "warning", "title": "Kiểm tra ý nghĩa trước cú pháp", "content": "Không đặt LIMIT trước ORDER BY trong cú pháp truy vấn PostgreSQL."}'::jsonb,5 FROM lessons l WHERE l.slug='sap-xep-ket-qua-truy-van-voi-order-by' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=6 ORDER BY s.created_at LIMIT 1),'2e4e0894-3664-53fa-a956-c20e1bf75bf1'::uuid),l.id,'6. Tự kiểm tra và chuyển sang thực hành','**Câu 1: ORDER BY price DESC LIMIT 1 có luôn chọn cùng một sách không?**

Tự trả lời trước khi đọc. **Giải thích:** Không nếu nhiều sách đồng giá và thiếu khóa phụ duy nhất.

**Câu 2: Trang thứ ba, kích thước 5 có OFFSET bao nhiêu?**

Tự trả lời trước khi đọc. **Giải thích:** OFFSET 10, vì bỏ qua (3 − 1) × 5 dòng.

### Thực hành sau khi học
Tất cả bài tập đều có yêu cầu ORDER BY; hãy đọc đúng cả hướng sắp xếp và khóa phụ.

[Mở database sách để thực hành](/practice/quan-ly-sach-va-tac-gia)

### Tài liệu tham khảo
- [Tài liệu PostgreSQL 16](https://www.postgresql.org/docs/16/queries-select-lists.html)',NULL,NULL,6 FROM lessons l WHERE l.slug='sap-xep-ket-qua-truy-van-voi-order-by' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lessons (id,category_id,title,slug,description,objectives,practice_topic_code,sort_order) SELECT 'ca334b83-f0ac-5fca-b8d5-76ed43781cc7',c.id,'06. AND, OR, NOT, IN và BETWEEN','dieu-kien-and-or-not-in-between','Ghép nhiều điều kiện chính xác; Đặt ngoặc để diễn đạt đúng nghiệp vụ; Dùng IN và BETWEEN thay chuỗi so sánh dài.','["Ghép nhiều điều kiện chính xác", "Đặt ngoặc để diễn đạt đúng nghiệp vụ", "Dùng IN và BETWEEN thay chuỗi so sánh dài"]'::jsonb,'SQL_BASIC',6 FROM course_categories c WHERE lower(trim(c.name))=lower('SQL cơ bản') ORDER BY c.created_at LIMIT 1 ON CONFLICT (slug) DO UPDATE SET category_id=EXCLUDED.category_id,title=EXCLUDED.title,description=EXCLUDED.description,objectives=EXCLUDED.objectives,practice_topic_code=EXCLUDED.practice_topic_code,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=1 ORDER BY s.created_at LIMIT 1),'23311d15-8897-5feb-9ef7-ad6b496c1831'::uuid),l.id,'1. Mục tiêu và chuẩn bị','### Sau bài này bạn có thể
- Ghép nhiều điều kiện chính xác
- Đặt ngoặc để diễn đạt đúng nghiệp vụ
- Dùng IN và BETWEEN thay chuỗi so sánh dài

### Kiến thức cần có
Đã học các bài đứng trước trong mục SQL cơ bản; đọc ví dụ theo đúng tên bảng/cột của bộ sách.

### Cách học
Đọc lý thuyết, dự đoán kết quả SQL trước khi chạy, sau đó đối chiếu giải thích. Các ví dụ truy vấn dùng bộ sách với 15 sách, 5 tác giả, 5 nhà xuất bản, 5 thể loại và 20 liên kết sách–tác giả. Một đầu sách có thể có nhiều tác giả; giá tính theo VND. Không cộng lẫn số đầu sách với số cuốn tồn kho.',NULL,'{"type": "info", "title": "Môi trường ví dụ", "content": "Cú pháp theo PostgreSQL 16. Ví dụ CREATE/UPDATE/DELETE và EXPLAIN được đánh dấu riêng, không phải đáp án cho phòng chỉ cho phép SELECT."}'::jsonb,1 FROM lessons l WHERE l.slug='dieu-kien-and-or-not-in-between' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=2 ORDER BY s.created_at LIMIT 1),'0fee9739-b071-5a3d-b13e-2ccb607f8306'::uuid),l.id,'2. Khái niệm và cách suy luận','AND đòi hỏi các điều kiện đều đúng; OR chỉ cần một điều kiện đúng; NOT đảo giá trị logic, với UNKNOWN vẫn là UNKNOWN. PostgreSQL ưu tiên NOT trước AND, rồi OR. Vì thế A OR B AND C được hiểu là A OR (B AND C), không phải (A OR B) AND C. Dù nhớ thứ tự ưu tiên, vẫn nên đặt ngoặc cho điều kiện phức tạp.

IN kiểm tra giá trị có thuộc một danh sách hay không, ví dụ category_id IN (1, 2). BETWEEN gồm cả hai đầu: price BETWEEN 99000 AND 145000 tương đương price >= 99000 AND price <= 145000. Với thời gian có phần giờ, cách lọc từ đầu ngày đến trước đầu ngày kế tiếp thường rõ hơn so với cố chọn mốc 23:59:59.

Đừng viết category_id = 1 OR 2; mỗi vế OR cần là một biểu thức logic hoàn chỉnh. Hãy bắt đầu từ câu nghiệp vụ: lấy sách thuộc hai thể loại và còn hàng. Tách thành nhóm điều kiện về thể loại và điều kiện tồn kho, sau đó nối chúng bằng AND. Với danh sách chứa NULL, IN và đặc biệt NOT IN có quy tắc ba giá trị cần được học ở bài NULL và truy vấn con.',NULL,NULL,2 FROM lessons l WHERE l.slug='dieu-kien-and-or-not-in-between' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=3 ORDER BY s.created_at LIMIT 1),'048db1e7-9dab-54ca-a497-f5397175e53e'::uuid),l.id,'3. Hai thể loại và còn hàng','### Phân tích ví dụ
Có 5 sách: 1, 2, 3, 4, 6. Sách số 5 thuộc Văn học nhưng hết hàng nên bị loại.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **5**.

| book_id | title | category_id | stock_quantity |
| --- | --- | --- | --- |
| 1 | SQL từ cơ bản đến thực hành | 1 | 3 |
| 2 | Thiết kế cơ sở dữ liệu | 1 | 8 |
| 3 | Python cho người mới bắt đầu | 1 | 15 |
| 4 | Mùa hạ bên ô cửa | 2 | 24 |
| 6 | Thành phố trong mưa | 2 | 3 |','SELECT book_id, title, category_id, stock_quantity
FROM books
WHERE category_id IN (1, 2) AND stock_quantity > 0
ORDER BY book_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,3 FROM lessons l WHERE l.slug='dieu-kien-and-or-not-in-between' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=4 ORDER BY s.created_at LIMIT 1),'d8cad583-cb0a-5259-af18-66b053f74d03'::uuid),l.id,'4. Giá trong khoảng đóng','### Phân tích ví dụ
Có 9 sách, bao gồm cả hai mức 99000 và 145000. Hai cận đều được tính.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **9**.

| book_id | title | price |
| --- | --- | --- |
| 1 | SQL từ cơ bản đến thực hành | 99000.0 |
| 6 | Thành phố trong mưa | 99000.0 |
| 11 | Thế giới của những hạt nhỏ | 99000.0 |
| 2 | Thiết kế cơ sở dữ liệu | 120000.0 |
| 7 | Khởi nghiệp từ trang giấy | 120000.0 |

Bảng trên minh họa 5 dòng đầu; truy vấn trả đầy đủ 9 dòng.','SELECT book_id, title, price
FROM books
WHERE price BETWEEN 99000 AND 145000
ORDER BY price, book_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,4 FROM lessons l WHERE l.slug='dieu-kien-and-or-not-in-between' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=5 ORDER BY s.created_at LIMIT 1),'35167b62-108e-5b7b-bb96-30b77d234fc1'::uuid),l.id,'5. Lỗi thường gặp và cách tránh','**1.** Dùng ngoặc quanh nhóm OR khi ghép thêm AND.

**2.** Không nhầm BETWEEN là khoảng mở; nó bao gồm cả hai cận.

**3.** NOT IN có NULL trong danh sách có thể trả UNKNOWN thay vì TRUE.

### Quy trình kiểm tra
Đọc lại điều kiện tại đúng giá trị ranh giới; kiểm tra NULL, dòng không có đối tác và trường hợp đồng giá. Một câu SQL chạy được chưa chứng minh kết quả đã đúng với yêu cầu.',NULL,'{"type": "warning", "title": "Kiểm tra ý nghĩa trước cú pháp", "content": "Dùng ngoặc quanh nhóm OR khi ghép thêm AND."}'::jsonb,5 FROM lessons l WHERE l.slug='dieu-kien-and-or-not-in-between' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=6 ORDER BY s.created_at LIMIT 1),'23301232-df12-5e60-b132-f6825afb3080'::uuid),l.id,'6. Tự kiểm tra và chuyển sang thực hành','**Câu 1: Lấy sách thể loại 1 hoặc 2 có giá trên 100000 viết điều kiện nào?**

Tự trả lời trước khi đọc. **Giải thích:** category_id IN (1, 2) AND price > 100000.

**Câu 2: NOT (price > 120000) có luôn giống price <= 120000 không?**

Tự trả lời trước khi đọc. **Giải thích:** Với price không NULL thì có; nếu price NULL, cả hai điều kiện đều UNKNOWN.

### Thực hành sau khi học
Rèn cách phân tách yêu cầu nhiều điều kiện trước các bài JOIN có WHERE.

[Mở database sách để thực hành](/practice/quan-ly-sach-va-tac-gia)

### Tài liệu tham khảo
- [Tài liệu PostgreSQL 16](https://www.postgresql.org/docs/16/queries-select-lists.html)',NULL,NULL,6 FROM lessons l WHERE l.slug='dieu-kien-and-or-not-in-between' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lessons (id,category_id,title,slug,description,objectives,practice_topic_code,sort_order) SELECT '06bf9965-1961-5632-bf2a-06ccac10424f',c.id,'07. NULL, IS NULL, COALESCE và NULLIF','null-is-null-va-coalesce','Hiểu NULL khác 0 và chuỗi rỗng; Dùng IS NULL để kiểm tra thiếu dữ liệu; Dùng COALESCE và NULLIF đúng mục đích.','["Hiểu NULL khác 0 và chuỗi rỗng", "Dùng IS NULL để kiểm tra thiếu dữ liệu", "Dùng COALESCE và NULLIF đúng mục đích"]'::jsonb,'SQL_BASIC',7 FROM course_categories c WHERE lower(trim(c.name))=lower('SQL cơ bản') ORDER BY c.created_at LIMIT 1 ON CONFLICT (slug) DO UPDATE SET category_id=EXCLUDED.category_id,title=EXCLUDED.title,description=EXCLUDED.description,objectives=EXCLUDED.objectives,practice_topic_code=EXCLUDED.practice_topic_code,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=1 ORDER BY s.created_at LIMIT 1),'33c27934-f3bc-5dd7-86ed-49e22200dfbe'::uuid),l.id,'1. Mục tiêu và chuẩn bị','### Sau bài này bạn có thể
- Hiểu NULL khác 0 và chuỗi rỗng
- Dùng IS NULL để kiểm tra thiếu dữ liệu
- Dùng COALESCE và NULLIF đúng mục đích

### Kiến thức cần có
Đã học các bài đứng trước trong mục SQL cơ bản; đọc ví dụ theo đúng tên bảng/cột của bộ sách.

### Cách học
Đọc lý thuyết, dự đoán kết quả SQL trước khi chạy, sau đó đối chiếu giải thích. Các ví dụ truy vấn dùng bộ sách với 15 sách, 5 tác giả, 5 nhà xuất bản, 5 thể loại và 20 liên kết sách–tác giả. Một đầu sách có thể có nhiều tác giả; giá tính theo VND. Không cộng lẫn số đầu sách với số cuốn tồn kho.',NULL,'{"type": "info", "title": "Môi trường ví dụ", "content": "Cú pháp theo PostgreSQL 16. Ví dụ CREATE/UPDATE/DELETE và EXPLAIN được đánh dấu riêng, không phải đáp án cho phòng chỉ cho phép SELECT."}'::jsonb,1 FROM lessons l WHERE l.slug='null-is-null-va-coalesce' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=2 ORDER BY s.created_at LIMIT 1),'b71022b7-6aea-5b85-9295-b81cb03e070a'::uuid),l.id,'2. Khái niệm và cách suy luận','NULL biểu thị giá trị chưa biết, không áp dụng hoặc thiếu thông tin; nó không phải số 0 và không phải chuỗi rỗng. Phép so sánh thông thường với NULL tạo UNKNOWN. Vì WHERE chỉ giữ TRUE nên WHERE value = NULL không tìm được các dòng thiếu giá trị. Dùng IS NULL hoặc IS NOT NULL để kiểm tra.

COALESCE trả đối số đầu tiên không NULL. Ví dụ COALESCE(SUM(...), 0) làm báo cáo không có dữ liệu hiển thị 0 thay vì NULL, nhưng cần xác nhận 0 đúng ý nghĩa nghiệp vụ. Hàm này không coi chuỗi rỗng là NULL. NULLIF(a, b) trả NULL khi a bằng b, ngược lại trả a; mẫu chia cho NULLIF(mẫu_số, 0) tránh lỗi chia cho 0 và cho kết quả NULL nếu không thể tính tỷ lệ.

Các cột mẫu books đều NOT NULL nhưng LEFT JOIN có thể tạo NULL ở phía không khớp. Nhà xuất bản số 5 chưa có sách sẽ có b.book_id bằng NULL trong phép nối. COUNT(*) vẫn đếm dòng do LEFT JOIN tạo, trong khi COUNT(b.book_id) bỏ qua NULL và đếm đúng 0 sách. Ngoài COUNT, nhiều hàm tổng hợp bỏ qua giá trị NULL; SUM trên tập đầu vào không có giá trị thường trả NULL.',NULL,NULL,2 FROM lessons l WHERE l.slug='null-is-null-va-coalesce' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=3 ORDER BY s.created_at LIMIT 1),'321ebd9f-ab5c-5ae0-97b4-929a28c9abfe'::uuid),l.id,'3. Kiểm tra logic NULL bằng dữ liệu nhỏ','### Phân tích ví dụ
wrong_check luôn NULL. correct_check chỉ TRUE ở dòng x bị thiếu. display_value là 1, 0, 0; do đó việc thay NULL bằng 0 làm mất phân biệt giữa thiếu và có giá trị 0.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **3**.

| x | wrong_check | correct_check | display_value |
| --- | --- | --- | --- |
| 1 | NULL | FALSE | 1 |
| NULL | NULL | TRUE | 0 |
| 0 | NULL | FALSE | 0 |','SELECT x, x = NULL AS wrong_check,
       x IS NULL AS correct_check,
       COALESCE(x, 0) AS display_value
FROM (VALUES (1), (NULL::integer), (0)) AS v(x);','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,3 FROM lessons l WHERE l.slug='null-is-null-va-coalesce' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=4 ORDER BY s.created_at LIMIT 1),'d4bfd5e6-52da-5ba1-89eb-52a5c5850e33'::uuid),l.id,'4. Tránh phép chia cho 0','### Phân tích ví dụ
Sách 5, 10, 15 có kết quả NULL thay vì lỗi chia cho 0. Đây là minh họa kỹ thuật NULLIF, không phải công thức giá bán mới.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **15**.

| book_id | title | price_per_stock_unit |
| --- | --- | --- |
| 1 | SQL từ cơ bản đến thực hành | 33000.0 |
| 2 | Thiết kế cơ sở dữ liệu | 15000.0 |
| 3 | Python cho người mới bắt đầu | 9666.67 |
| 4 | Mùa hạ bên ô cửa | 7500.0 |
| 5 | Những con đường cũ | NULL |

Bảng trên minh họa 5 dòng đầu; truy vấn trả đầy đủ 15 dòng.','SELECT book_id, title,
       ROUND(price / NULLIF(stock_quantity, 0), 2) AS price_per_stock_unit
FROM books
ORDER BY book_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,4 FROM lessons l WHERE l.slug='null-is-null-va-coalesce' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=5 ORDER BY s.created_at LIMIT 1),'1000fe19-e6ef-5519-b11d-0e2f664bb70b'::uuid),l.id,'5. Lỗi thường gặp và cách tránh','**1.** Không viết = NULL hoặc <> NULL.

**2.** COALESCE không tự sửa dữ liệu gốc; nó chỉ tạo giá trị cho biểu thức.

**3.** Đừng thay mọi NULL bằng 0 nếu thiếu dữ liệu cần được phân biệt với giá trị bằng 0.

### Quy trình kiểm tra
Đọc lại điều kiện tại đúng giá trị ranh giới; kiểm tra NULL, dòng không có đối tác và trường hợp đồng giá. Một câu SQL chạy được chưa chứng minh kết quả đã đúng với yêu cầu.',NULL,'{"type": "warning", "title": "Kiểm tra ý nghĩa trước cú pháp", "content": "Không viết = NULL hoặc <> NULL."}'::jsonb,5 FROM lessons l WHERE l.slug='null-is-null-va-coalesce' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=6 ORDER BY s.created_at LIMIT 1),'a156f107-f800-5e7a-a6bf-51a9e229071b'::uuid),l.id,'6. Tự kiểm tra và chuyển sang thực hành','**Câu 1: COUNT(*) và COUNT(book_id) khác nhau lúc nào?**

Tự trả lời trước khi đọc. **Giải thích:** Khi book_id có NULL trong đầu vào, ví dụ phía phải của LEFT JOIN không khớp.

**Câu 2: COALESCE('''', ''Chưa có'') trả gì?**

Tự trả lời trước khi đọc. **Giải thích:** Chuỗi rỗng, vì nó không phải NULL.

### Thực hành sau khi học
Quan trọng cho bài LEFT JOIN và đếm cả nhà xuất bản có 0 sách.

[Mở database sách để thực hành](/practice/quan-ly-sach-va-tac-gia)

### Tài liệu tham khảo
- [Tài liệu PostgreSQL 16](https://www.postgresql.org/docs/16/queries-select-lists.html)',NULL,NULL,6 FROM lessons l WHERE l.slug='null-is-null-va-coalesce' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lessons (id,category_id,title,slug,description,objectives,practice_topic_code,sort_order) SELECT '39231e47-829f-5a53-885c-9329df08de43',c.id,'08. Tìm chuỗi với LIKE và ILIKE','tim-kiem-chuoi-like-ilike','Dùng ký tự đại diện % và _; Phân biệt LIKE với ILIKE trong PostgreSQL; Lọc chuỗi đúng vị trí bắt đầu, kết thúc hoặc chứa.','["Dùng ký tự đại diện % và _", "Phân biệt LIKE với ILIKE trong PostgreSQL", "Lọc chuỗi đúng vị trí bắt đầu, kết thúc hoặc chứa"]'::jsonb,'SQL_BASIC',8 FROM course_categories c WHERE lower(trim(c.name))=lower('SQL cơ bản') ORDER BY c.created_at LIMIT 1 ON CONFLICT (slug) DO UPDATE SET category_id=EXCLUDED.category_id,title=EXCLUDED.title,description=EXCLUDED.description,objectives=EXCLUDED.objectives,practice_topic_code=EXCLUDED.practice_topic_code,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=1 ORDER BY s.created_at LIMIT 1),'b010a290-86d7-5e6c-8d38-0314187ff456'::uuid),l.id,'1. Mục tiêu và chuẩn bị','### Sau bài này bạn có thể
- Dùng ký tự đại diện % và _
- Phân biệt LIKE với ILIKE trong PostgreSQL
- Lọc chuỗi đúng vị trí bắt đầu, kết thúc hoặc chứa

### Kiến thức cần có
Đã học các bài đứng trước trong mục SQL cơ bản; đọc ví dụ theo đúng tên bảng/cột của bộ sách.

### Cách học
Đọc lý thuyết, dự đoán kết quả SQL trước khi chạy, sau đó đối chiếu giải thích. Các ví dụ truy vấn dùng bộ sách với 15 sách, 5 tác giả, 5 nhà xuất bản, 5 thể loại và 20 liên kết sách–tác giả. Một đầu sách có thể có nhiều tác giả; giá tính theo VND. Không cộng lẫn số đầu sách với số cuốn tồn kho.',NULL,'{"type": "info", "title": "Môi trường ví dụ", "content": "Cú pháp theo PostgreSQL 16. Ví dụ CREATE/UPDATE/DELETE và EXPLAIN được đánh dấu riêng, không phải đáp án cho phòng chỉ cho phép SELECT."}'::jsonb,1 FROM lessons l WHERE l.slug='tim-kiem-chuoi-like-ilike' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=2 ORDER BY s.created_at LIMIT 1),'83caf609-4e90-5ce0-b832-731f95087440'::uuid),l.id,'2. Khái niệm và cách suy luận','LIKE kiểm tra một chuỗi có khớp mẫu hay không. Dấu % đại diện cho không hoặc nhiều ký tự, còn _ đại diện đúng một ký tự. Mẫu SQL% tìm chuỗi bắt đầu bằng SQL; %SQL% tìm chuỗi chứa SQL ở bất kỳ vị trí nào. LIKE không phải phép bằng: bỏ ký tự đại diện làm nó kiểm tra một mẫu toàn chuỗi.

Trong cấu hình thông thường, LIKE phân biệt hoa/thường; PostgreSQL có ILIKE để tìm không phân biệt hoa/thường theo locale. ILIKE không đồng nghĩa bỏ dấu tiếng Việt: sach không tự khớp sách. Nếu cần tìm kiếm bỏ dấu, hệ thống phải có bước chuẩn hóa hoặc tiện ích phù hợp. Bài mẫu chỉ dùng dữ liệu và tính năng sẵn có của PostgreSQL.

Nếu cần tìm chính dấu % hay _, có thể chọn một ký tự escape và khai báo ESCAPE. Đừng ghép chuỗi đầu vào người dùng trực tiếp thành SQL; ứng dụng nên dùng tham số truy vấn. Tìm chứa với % ở đầu mẫu có thể khó dùng chỉ mục B-tree thông thường; đó là vấn đề khác với tính đúng đắn của điều kiện. Chỉ tối ưu sau khi đã xác định rõ người dùng muốn tìm bắt đầu bằng hay tìm chứa.',NULL,NULL,2 FROM lessons l WHERE l.slug='tim-kiem-chuoi-like-ilike' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=3 ORDER BY s.created_at LIMIT 1),'516546a3-1148-5256-9751-ba9664a4af9f'::uuid),l.id,'3. Tên sách chứa một từ','### Phân tích ví dụ
Có book_id 15: Bí mật thư viện nhỏ không chứa từ sách, nên không được lấy; trên dữ liệu hiện tại truy vấn này không có dòng nào. Tập kết quả rỗng có thể là kết quả đúng, không phải lỗi SQL.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **0**. Không có dòng nào thỏa điều kiện; đây là tập kết quả rỗng hợp lệ.','SELECT book_id, title
FROM books
WHERE title ILIKE ''%sách%''
ORDER BY book_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,3 FROM lessons l WHERE l.slug='tim-kiem-chuoi-like-ilike' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=4 ORDER BY s.created_at LIMIT 1),'110d313e-0cbd-5065-97ae-cd559d3a3fac'::uuid),l.id,'4. Tên sách bắt đầu bằng SQL','### Phân tích ví dụ
Có một sách: SQL từ cơ bản đến thực hành. Mẫu không yêu cầu khoảng trắng sau SQL nên bất kỳ tiêu đề bắt đầu bằng ba ký tự đó đều phù hợp.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **1**.

| book_id | title |
| --- | --- |
| 1 | SQL từ cơ bản đến thực hành |','SELECT book_id, title
FROM books
WHERE title LIKE ''SQL%''
ORDER BY book_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,4 FROM lessons l WHERE l.slug='tim-kiem-chuoi-like-ilike' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=5 ORDER BY s.created_at LIMIT 1),'8a0dfa4a-02bd-5dad-bee5-d7ea1b8b1d76'::uuid),l.id,'5. Lỗi thường gặp và cách tránh','**1.** % khớp cả chuỗi rỗng; _ luôn yêu cầu đúng một ký tự.

**2.** ILIKE xử lý hoa/thường, không tự động bỏ dấu tiếng Việt.

**3.** Tìm chứa khác tìm bắt đầu bằng; hai mẫu có thể cho kết quả rất khác nhau.

### Quy trình kiểm tra
Đọc lại điều kiện tại đúng giá trị ranh giới; kiểm tra NULL, dòng không có đối tác và trường hợp đồng giá. Một câu SQL chạy được chưa chứng minh kết quả đã đúng với yêu cầu.',NULL,'{"type": "warning", "title": "Kiểm tra ý nghĩa trước cú pháp", "content": "% khớp cả chuỗi rỗng; _ luôn yêu cầu đúng một ký tự."}'::jsonb,5 FROM lessons l WHERE l.slug='tim-kiem-chuoi-like-ilike' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=6 ORDER BY s.created_at LIMIT 1),'f3b85c06-2a24-515e-a6d2-feaad5493955'::uuid),l.id,'6. Tự kiểm tra và chuyển sang thực hành','**Câu 1: Mẫu _SQL% yêu cầu điều gì?**

Tự trả lời trước khi đọc. **Giải thích:** Có đúng một ký tự trước SQL rồi đến phần đuôi tùy ý.

**Câu 2: Vì sao một truy vấn không trả dòng vẫn có thể đúng?**

Tự trả lời trước khi đọc. **Giải thích:** Vì không có giá trị nào trong dữ liệu thỏa mẫu; cần kiểm tra dữ liệu thay vì đổi điều kiện tùy tiện.

### Thực hành sau khi học
Có thể thử tìm title ILIKE ''%SQL%'' hoặc ''%cơ sở%'' trong bảng books.

[Mở database sách để thực hành](/practice/quan-ly-sach-va-tac-gia)

### Tài liệu tham khảo
- [Tài liệu PostgreSQL 16](https://www.postgresql.org/docs/16/queries-select-lists.html)',NULL,NULL,6 FROM lessons l WHERE l.slug='tim-kiem-chuoi-like-ilike' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lessons (id,category_id,title,slug,description,objectives,practice_topic_code,sort_order) SELECT '8076575f-7e85-5ceb-b148-f5c1b750f7eb',c.id,'09. Hàm chuỗi, số, ngày tháng và ép kiểu','ham-chuoi-so-ngay-va-ep-kieu','Biến đổi giá trị trong SELECT; Tránh chia nguyên và làm tròn sai; Hiểu sự khác nhau giữa năm số nguyên và kiểu ngày.','["Biến đổi giá trị trong SELECT", "Tránh chia nguyên và làm tròn sai", "Hiểu sự khác nhau giữa năm số nguyên và kiểu ngày"]'::jsonb,'SQL_BASIC',9 FROM course_categories c WHERE lower(trim(c.name))=lower('SQL cơ bản') ORDER BY c.created_at LIMIT 1 ON CONFLICT (slug) DO UPDATE SET category_id=EXCLUDED.category_id,title=EXCLUDED.title,description=EXCLUDED.description,objectives=EXCLUDED.objectives,practice_topic_code=EXCLUDED.practice_topic_code,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=1 ORDER BY s.created_at LIMIT 1),'533b6960-59fd-5e6b-8a9a-fb05cefd4671'::uuid),l.id,'1. Mục tiêu và chuẩn bị','### Sau bài này bạn có thể
- Biến đổi giá trị trong SELECT
- Tránh chia nguyên và làm tròn sai
- Hiểu sự khác nhau giữa năm số nguyên và kiểu ngày

### Kiến thức cần có
Đã học các bài đứng trước trong mục SQL cơ bản; đọc ví dụ theo đúng tên bảng/cột của bộ sách.

### Cách học
Đọc lý thuyết, dự đoán kết quả SQL trước khi chạy, sau đó đối chiếu giải thích. Các ví dụ truy vấn dùng bộ sách với 15 sách, 5 tác giả, 5 nhà xuất bản, 5 thể loại và 20 liên kết sách–tác giả. Một đầu sách có thể có nhiều tác giả; giá tính theo VND. Không cộng lẫn số đầu sách với số cuốn tồn kho.',NULL,'{"type": "info", "title": "Môi trường ví dụ", "content": "Cú pháp theo PostgreSQL 16. Ví dụ CREATE/UPDATE/DELETE và EXPLAIN được đánh dấu riêng, không phải đáp án cho phòng chỉ cho phép SELECT."}'::jsonb,1 FROM lessons l WHERE l.slug='ham-chuoi-so-ngay-va-ep-kieu' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=2 ORDER BY s.created_at LIMIT 1),'7184d67d-0e24-578f-b84a-720c50e7bf73'::uuid),l.id,'2. Khái niệm và cách suy luận','Hàm xử lý dữ liệu giúp chuyển từ giá trị gốc sang thông tin báo cáo. LOWER/UPPER đổi hoa thường; TRIM bỏ khoảng trắng ở hai đầu; LENGTH đếm số ký tự. ROUND dùng để làm tròn số, nhưng việc làm tròn quá sớm trước tổng hợp có thể làm sai kết quả cuối. Nên tính đủ chính xác trước rồi làm tròn khi xuất báo cáo.

Trong PostgreSQL, phép chia hai số nguyên cho kết quả nguyên bị cắt phần lẻ. Dùng 100.0 hoặc ép kiểu ::numeric trước phép chia khi tính tỷ lệ. Giá books.price đã là NUMERIC; các mã, năm và tồn kho là INT. Ép kiểu đúng ở đầu biểu thức giúp tránh mất độ chính xác trước khi ROUND được gọi.

published_year của books chỉ là năm, không phải một DATE hoàn chỉnh. Không thể tự suy ra ngày phát hành từ một số như 2020. Với dữ liệu ngày thực sự, dùng DATE, INTERVAL hoặc EXTRACT, và hiểu rõ đơn vị mong muốn. Ví dụ dùng DATE cố định sẽ tái lập được kết quả; CURRENT_DATE thay đổi theo ngày chạy. Với timestamp, cần tính đến múi giờ. Hãy kiểm tra kiểu đầu vào và kiểu đầu ra của hàm thay vì chỉ nhìn tên.',NULL,NULL,2 FROM lessons l WHERE l.slug='ham-chuoi-so-ngay-va-ep-kieu' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=3 ORDER BY s.created_at LIMIT 1),'a3b7d580-218c-5abd-b4ce-2fca7b1a8621'::uuid),l.id,'3. Chuẩn hóa chuỗi để báo cáo','### Phân tích ví dụ
Có 5 dòng; tên được đổi thành chữ hoa và đếm ký tự, dữ liệu gốc không đổi. Kết quả LENGTH là số ký tự chứ không phải dung lượng byte.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **5**.

| author_id | upper_name | name_length |
| --- | --- | --- |
| 1 | NGUYỄN MINH AN | 14 |
| 2 | TRẦN THU HÀ | 11 |
| 3 | LÊ HOÀNG NAM | 12 |
| 4 | PHẠM NGỌC LINH | 14 |
| 5 | ĐỖ QUỐC BẢO | 11 |','SELECT author_id, UPPER(full_name) AS upper_name,
       LENGTH(full_name) AS name_length
FROM authors
ORDER BY author_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,3 FROM lessons l WHERE l.slug='ham-chuoi-so-ngay-va-ep-kieu' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=4 ORDER BY s.created_at LIMIT 1),'bb425ad3-5141-58a5-a993-ff268ae57d03'::uuid),l.id,'4. Chia số và tính ngày minh họa','### Phân tích ví dụ
Kết quả lần lượt là 2, 2.50 và ngày 2025-01-08. Việc ép numeric được thực hiện trước phép chia để giữ phần lẻ.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **1**.

| integer_division | decimal_division | one_week_later |
| --- | --- | --- |
| 2 | 2.5 | 2025-01-08 |','SELECT 5 / 2 AS integer_division,
       ROUND(5::numeric / 2, 2) AS decimal_division,
       DATE ''2025-01-01'' + 7 AS one_week_later;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,4 FROM lessons l WHERE l.slug='ham-chuoi-so-ngay-va-ep-kieu' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=5 ORDER BY s.created_at LIMIT 1),'b6f88c05-1806-5fcf-a6ac-432af9637565'::uuid),l.id,'5. Lỗi thường gặp và cách tránh','**1.** ROUND sau phép chia nguyên không khôi phục được phần thập phân đã mất.

**2.** Không cộng số năm trực tiếp vào kiểu DATE nếu ý định là cộng năm: cộng số nguyên vào DATE là cộng ngày.

**3.** Không mặc định năm xuất bản INT có đủ thông tin ngày tháng.

### Quy trình kiểm tra
Đọc lại điều kiện tại đúng giá trị ranh giới; kiểm tra NULL, dòng không có đối tác và trường hợp đồng giá. Một câu SQL chạy được chưa chứng minh kết quả đã đúng với yêu cầu.',NULL,'{"type": "warning", "title": "Kiểm tra ý nghĩa trước cú pháp", "content": "ROUND sau phép chia nguyên không khôi phục được phần thập phân đã mất."}'::jsonb,5 FROM lessons l WHERE l.slug='ham-chuoi-so-ngay-va-ep-kieu' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=6 ORDER BY s.created_at LIMIT 1),'157de929-2d6b-5789-804b-830d561d4208'::uuid),l.id,'6. Tự kiểm tra và chuyển sang thực hành','**Câu 1: Vì sao 1 / 4 * 100 bằng 0?**

Tự trả lời trước khi đọc. **Giải thích:** Phép chia nguyên 1 / 4 đã cho 0; dùng 100.0 * 1 / 4 hoặc ép numeric.

**Câu 2: UPPER có sửa tên tác giả đã lưu không?**

Tự trả lời trước khi đọc. **Giải thích:** Không; nó chỉ thay đổi giá trị biểu thức ở kết quả SELECT.

### Thực hành sau khi học
Dùng ROUND(AVG(price), 2) ở bài trung bình và ép numeric khi tính tỷ lệ.

[Mở database sách để thực hành](/practice/quan-ly-sach-va-tac-gia)

### Tài liệu tham khảo
- [Tài liệu PostgreSQL 16](https://www.postgresql.org/docs/16/queries-select-lists.html)',NULL,NULL,6 FROM lessons l WHERE l.slug='ham-chuoi-so-ngay-va-ep-kieu' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lessons (id,category_id,title,slug,description,objectives,practice_topic_code,sort_order) SELECT 'd9491dc8-0d0f-532e-bbf3-6cffcb4cd3a2',c.id,'10. CASE WHEN và phân loại dữ liệu','case-when-va-phan-loai-du-lieu','Viết biểu thức CASE trả giá trị; Hiểu thứ tự xét các nhánh; Kết hợp CASE với điều kiện nghiệp vụ.','["Viết biểu thức CASE trả giá trị", "Hiểu thứ tự xét các nhánh", "Kết hợp CASE với điều kiện nghiệp vụ"]'::jsonb,'SQL_BASIC',10 FROM course_categories c WHERE lower(trim(c.name))=lower('SQL cơ bản') ORDER BY c.created_at LIMIT 1 ON CONFLICT (slug) DO UPDATE SET category_id=EXCLUDED.category_id,title=EXCLUDED.title,description=EXCLUDED.description,objectives=EXCLUDED.objectives,practice_topic_code=EXCLUDED.practice_topic_code,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=1 ORDER BY s.created_at LIMIT 1),'f1255da9-90b0-547a-be17-218640583bda'::uuid),l.id,'1. Mục tiêu và chuẩn bị','### Sau bài này bạn có thể
- Viết biểu thức CASE trả giá trị
- Hiểu thứ tự xét các nhánh
- Kết hợp CASE với điều kiện nghiệp vụ

### Kiến thức cần có
Đã học các bài đứng trước trong mục SQL cơ bản; đọc ví dụ theo đúng tên bảng/cột của bộ sách.

### Cách học
Đọc lý thuyết, dự đoán kết quả SQL trước khi chạy, sau đó đối chiếu giải thích. Các ví dụ truy vấn dùng bộ sách với 15 sách, 5 tác giả, 5 nhà xuất bản, 5 thể loại và 20 liên kết sách–tác giả. Một đầu sách có thể có nhiều tác giả; giá tính theo VND. Không cộng lẫn số đầu sách với số cuốn tồn kho.',NULL,'{"type": "info", "title": "Môi trường ví dụ", "content": "Cú pháp theo PostgreSQL 16. Ví dụ CREATE/UPDATE/DELETE và EXPLAIN được đánh dấu riêng, không phải đáp án cho phòng chỉ cho phép SELECT."}'::jsonb,1 FROM lessons l WHERE l.slug='case-when-va-phan-loai-du-lieu' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=2 ORDER BY s.created_at LIMIT 1),'6b8815cf-3f28-5c6f-a9e6-dffb83b3413d'::uuid),l.id,'2. Khái niệm và cách suy luận','CASE là một biểu thức chọn giá trị, không phải một lệnh chuyển điều khiển toàn bộ truy vấn. Dạng phổ biến là CASE WHEN điều_kiện THEN giá_trị ... ELSE giá_trị END. Nó có thể xuất hiện ở SELECT, ORDER BY hoặc bên trong hàm tổng hợp. Mỗi dòng được kiểm tra từ trên xuống và dừng ở WHEN đầu tiên đúng.

Khi các điều kiện chồng lấn, đặt trường hợp cụ thể trước. Ví dụ stock_quantity = 0 phải được xét trước stock_quantity < 5 nếu muốn phân biệt hết hàng và sắp hết hàng. Nếu không có nhánh đúng và bỏ ELSE, kết quả là NULL. Các nhánh THEN/ELSE cần có kiểu dữ liệu tương thích; không trộn số với một chuỗi tùy ý trong cùng cột báo cáo.

Dùng CASE giúp đưa quy tắc kinh doanh vào biểu thức minh bạch. Tuy nhiên, đừng đổi ý nghĩa điều kiện khi phân loại: từ 120000 trở lên cần nhánh >= 120000; cao hơn cần >. Với bài tổng hợp, SUM(CASE WHEN điều_kiện THEN 1 ELSE 0 END) đếm dòng thỏa điều kiện. Hãy kiểm tra các giá trị ở đúng ranh giới và xem một dòng có thể được gán nhiều nhãn hay chỉ một nhãn.',NULL,NULL,2 FROM lessons l WHERE l.slug='case-when-va-phan-loai-du-lieu' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=3 ORDER BY s.created_at LIMIT 1),'c4e56d28-ab87-53a7-9600-d5b54c0dd6c4'::uuid),l.id,'3. Phân loại tồn kho','### Phân tích ví dụ
Sách 5, 10, 15 là Hết hàng; 1, 6, 11 là Sắp hết. Các sách còn lại Còn hàng. Điều kiện được xét theo thứ tự.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **15**.

| book_id | title | stock_status |
| --- | --- | --- |
| 1 | SQL từ cơ bản đến thực hành | Sắp hết |
| 2 | Thiết kế cơ sở dữ liệu | Còn hàng |
| 3 | Python cho người mới bắt đầu | Còn hàng |
| 4 | Mùa hạ bên ô cửa | Còn hàng |
| 5 | Những con đường cũ | Hết hàng |

Bảng trên minh họa 5 dòng đầu; truy vấn trả đầy đủ 15 dòng.','SELECT book_id, title,
       CASE WHEN stock_quantity = 0 THEN ''Hết hàng''
            WHEN stock_quantity < 5 THEN ''Sắp hết''
            ELSE ''Còn hàng'' END AS stock_status
FROM books
ORDER BY book_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,3 FROM lessons l WHERE l.slug='case-when-va-phan-loai-du-lieu' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=4 ORDER BY s.created_at LIMIT 1),'e25de1ea-b073-5280-b743-a7148514a569'::uuid),l.id,'4. Chia nhóm giá','### Phân tích ví dụ
Giá 145000 thuộc Cao; 99000 và 120000 thuộc Vừa; 75000 thuộc Thấp. Đây là quy tắc minh họa, không phải thuộc tính có sẵn trong schema.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **15**.

| book_id | price | price_group |
| --- | --- | --- |
| 1 | 99000.0 | Vừa |
| 2 | 120000.0 | Vừa |
| 3 | 145000.0 | Cao |
| 4 | 180000.0 | Cao |
| 5 | 75000.0 | Thấp |

Bảng trên minh họa 5 dòng đầu; truy vấn trả đầy đủ 15 dòng.','SELECT book_id, price,
       CASE WHEN price >= 145000 THEN ''Cao''
            WHEN price >= 99000 THEN ''Vừa''
            ELSE ''Thấp'' END AS price_group
FROM books
ORDER BY book_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,4 FROM lessons l WHERE l.slug='case-when-va-phan-loai-du-lieu' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=5 ORDER BY s.created_at LIMIT 1),'20f8152b-9898-5d0e-86bd-90606ef877c3'::uuid),l.id,'5. Lỗi thường gặp và cách tránh','**1.** Không quên END ở cuối CASE.

**2.** Bỏ ELSE có thể tạo NULL khiến báo cáo khó đọc hoặc COUNT bỏ qua dòng.

**3.** Không đặt một điều kiện rộng trước điều kiện hẹp nếu muốn nhánh hẹp có cơ hội chạy.

### Quy trình kiểm tra
Đọc lại điều kiện tại đúng giá trị ranh giới; kiểm tra NULL, dòng không có đối tác và trường hợp đồng giá. Một câu SQL chạy được chưa chứng minh kết quả đã đúng với yêu cầu.',NULL,'{"type": "warning", "title": "Kiểm tra ý nghĩa trước cú pháp", "content": "Không quên END ở cuối CASE."}'::jsonb,5 FROM lessons l WHERE l.slug='case-when-va-phan-loai-du-lieu' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=6 ORDER BY s.created_at LIMIT 1),'f6e281bc-d955-5e23-af25-3c6fde766770'::uuid),l.id,'6. Tự kiểm tra và chuyển sang thực hành','**Câu 1: Nếu đặt stock_quantity < 5 trước = 0 thì sao?**

Tự trả lời trước khi đọc. **Giải thích:** Sách hết hàng sẽ bị gán Sắp hết, vì nhánh đầu tiên đã đúng.

**Câu 2: Có thể dùng CASE trong SUM không?**

Tự trả lời trước khi đọc. **Giải thích:** Có; đó là cách tổng hợp có điều kiện, sẽ được dùng ở bài GROUP BY.

### Thực hành sau khi học
Chuẩn bị cho bài thống kê sách còn hàng và hết hàng theo thể loại.

[Mở database sách để thực hành](/practice/quan-ly-sach-va-tac-gia)

### Tài liệu tham khảo
- [Tài liệu PostgreSQL 16](https://www.postgresql.org/docs/16/queries-select-lists.html)',NULL,NULL,6 FROM lessons l WHERE l.slug='case-when-va-phan-loai-du-lieu' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lessons (id,category_id,title,slug,description,objectives,practice_topic_code,sort_order) SELECT 'be446d35-1adc-5db9-931f-55fb5774fe10',c.id,'11. Khóa chính, khóa ngoại và cách đọc ERD','khoa-chinh-khoa-ngoai-va-doc-erd','Xác định khóa chính và khóa ngoại; Đọc quan hệ một–nhiều và nhiều–nhiều; Nhận biết khóa ghép trước khi JOIN.','["Xác định khóa chính và khóa ngoại", "Đọc quan hệ một–nhiều và nhiều–nhiều", "Nhận biết khóa ghép trước khi JOIN"]'::jsonb,'SQL_BASIC',11 FROM course_categories c WHERE lower(trim(c.name))=lower('SQL cơ bản') ORDER BY c.created_at LIMIT 1 ON CONFLICT (slug) DO UPDATE SET category_id=EXCLUDED.category_id,title=EXCLUDED.title,description=EXCLUDED.description,objectives=EXCLUDED.objectives,practice_topic_code=EXCLUDED.practice_topic_code,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=1 ORDER BY s.created_at LIMIT 1),'a75768a1-e6a3-547a-8da2-7b0b4be26ece'::uuid),l.id,'1. Mục tiêu và chuẩn bị','### Sau bài này bạn có thể
- Xác định khóa chính và khóa ngoại
- Đọc quan hệ một–nhiều và nhiều–nhiều
- Nhận biết khóa ghép trước khi JOIN

### Kiến thức cần có
Đã học các bài đứng trước trong mục SQL cơ bản; đọc ví dụ theo đúng tên bảng/cột của bộ sách.

### Cách học
Đọc lý thuyết, dự đoán kết quả SQL trước khi chạy, sau đó đối chiếu giải thích. Các ví dụ truy vấn dùng bộ sách với 15 sách, 5 tác giả, 5 nhà xuất bản, 5 thể loại và 20 liên kết sách–tác giả. Một đầu sách có thể có nhiều tác giả; giá tính theo VND. Không cộng lẫn số đầu sách với số cuốn tồn kho.',NULL,'{"type": "info", "title": "Môi trường ví dụ", "content": "Cú pháp theo PostgreSQL 16. Ví dụ CREATE/UPDATE/DELETE và EXPLAIN được đánh dấu riêng, không phải đáp án cho phòng chỉ cho phép SELECT."}'::jsonb,1 FROM lessons l WHERE l.slug='khoa-chinh-khoa-ngoai-va-doc-erd' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=2 ORDER BY s.created_at LIMIT 1),'71b3d80f-7b6c-5ba3-9aba-3ac4c0c5d380'::uuid),l.id,'2. Khái niệm và cách suy luận','Khóa chính xác định duy nhất một dòng và không nhận NULL. Khóa ngoại ràng buộc giá trị ở bảng con phải tham chiếu giá trị hợp lệ ở bảng được tham chiếu, hoặc có thể NULL nếu cột cho phép. Một bảng có thể có nhiều khóa ngoại. Trong database sách, books.publisher_id và books.category_id là hai liên kết riêng, không phải một khóa ghép.

Quan hệ publishers → books là một–nhiều: một nhà xuất bản có nhiều sách, một sách mẫu thuộc một nhà xuất bản. Quan hệ books ↔ authors là nhiều–nhiều, được biểu diễn bằng book_authors. Khóa chính của book_authors gồm cả book_id và author_id nên một cặp không thể lặp, nhưng từng book_id hoặc author_id riêng lẻ có thể lặp. author_order chỉ là thứ tự đứng tên, không phải mã tác giả.

Khi đọc ERD, theo đường từ cột khóa ngoại đến cột được tham chiếu, rồi xác định mức dữ liệu ở đầu ra. Có FK không có nghĩa mọi bản ghi bảng cha đều có bản ghi con: nhà xuất bản Cánh Diều hợp lệ dù chưa có sách. Với một khóa ngoại ghép ở database khác, điều kiện nối phải dùng đầy đủ các thành phần cùng cặp, không chỉ một cột.',NULL,NULL,2 FROM lessons l WHERE l.slug='khoa-chinh-khoa-ngoai-va-doc-erd' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=3 ORDER BY s.created_at LIMIT 1),'32e8dabf-3f7b-52fc-95de-bd39a59200f2'::uuid),l.id,'3. Đọc một cạnh một–nhiều','### Phân tích ví dụ
Có 15 dòng vì mỗi sách có một nhà xuất bản hợp lệ. Khóa ngoại nối với publisher_id, không nối theo tên hoặc theo book_id.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **15**.

| book_id | publisher_id | publisher_name |
| --- | --- | --- |
| 1 | 1 | Tri Thức Mới |
| 2 | 2 | Ánh Dương |
| 3 | 3 | Hành Trang |
| 4 | 4 | Trang Sách Xanh |
| 5 | 1 | Tri Thức Mới |

Bảng trên minh họa 5 dòng đầu; truy vấn trả đầy đủ 15 dòng.','SELECT b.book_id, b.publisher_id, p.name AS publisher_name
FROM books b
JOIN publishers p ON p.publisher_id = b.publisher_id
ORDER BY b.book_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,3 FROM lessons l WHERE l.slug='khoa-chinh-khoa-ngoai-va-doc-erd' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=4 ORDER BY s.created_at LIMIT 1),'ed159046-2f97-53c8-b419-767e3129cc27'::uuid),l.id,'4. Đọc bảng liên kết nhiều–nhiều','### Phân tích ví dụ
Có hai dòng: (1, 1, 1) và (1, 2, 2). Sách 1 có Nguyễn Minh An và Trần Thu Hà; hai dòng là dữ liệu hợp lệ, không phải bản ghi trùng.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **2**.

| book_id | author_id | author_order |
| --- | --- | --- |
| 1 | 1 | 1 |
| 1 | 2 | 2 |','SELECT book_id, author_id, author_order
FROM book_authors
WHERE book_id = 1
ORDER BY author_order;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,4 FROM lessons l WHERE l.slug='khoa-chinh-khoa-ngoai-va-doc-erd' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=5 ORDER BY s.created_at LIMIT 1),'a67188c7-b80d-552c-a89c-ef5d36fb71ff'::uuid),l.id,'5. Lỗi thường gặp và cách tránh','**1.** Không nối cột chỉ vì cùng kiểu INT; phải đúng ý nghĩa khóa.

**2.** Không mặc định mọi cột tên id đều có thể nối với nhau.

**3.** Khóa chính ghép duy nhất theo cả bộ giá trị, không theo từng cột riêng lẻ.

### Quy trình kiểm tra
Đọc lại điều kiện tại đúng giá trị ranh giới; kiểm tra NULL, dòng không có đối tác và trường hợp đồng giá. Một câu SQL chạy được chưa chứng minh kết quả đã đúng với yêu cầu.',NULL,'{"type": "warning", "title": "Kiểm tra ý nghĩa trước cú pháp", "content": "Không nối cột chỉ vì cùng kiểu INT; phải đúng ý nghĩa khóa."}'::jsonb,5 FROM lessons l WHERE l.slug='khoa-chinh-khoa-ngoai-va-doc-erd' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=6 ORDER BY s.created_at LIMIT 1),'8c815e3a-abd6-522d-954a-8148c189ccfe'::uuid),l.id,'6. Tự kiểm tra và chuyển sang thực hành','**Câu 1: Vì sao book_id lặp trong book_authors không vi phạm khóa chính?**

Tự trả lời trước khi đọc. **Giải thích:** Vì khóa chính là cặp (book_id, author_id), các cặp vẫn khác nhau.

**Câu 2: Nhà xuất bản không có sách có làm sai FK không?**

Tự trả lời trước khi đọc. **Giải thích:** Không. FK kiểm tra bảng con tham chiếu bảng cha, không bắt bảng cha phải có con.

### Thực hành sau khi học
Mở tab ERD của Quản lý Sách & Tác giả và lần theo đủ bốn khóa ngoại trước khi làm JOIN.

[Mở database sách để thực hành](/practice/quan-ly-sach-va-tac-gia)

### Tài liệu tham khảo
- [Tài liệu PostgreSQL 16](https://www.postgresql.org/docs/16/queries-select-lists.html)',NULL,NULL,6 FROM lessons l WHERE l.slug='khoa-chinh-khoa-ngoai-va-doc-erd' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lessons (id,category_id,title,slug,description,objectives,practice_topic_code,sort_order) SELECT '73fd108c-24cb-5cdc-8c7c-dfadc06e6646',c.id,'12. CREATE TABLE, INSERT, UPDATE, DELETE và ràng buộc','tao-bang-them-sua-xoa-va-rang-buoc','Phân biệt định nghĩa cấu trúc và thao tác dữ liệu; Đọc NOT NULL, UNIQUE, CHECK và REFERENCES; Biết cách thử thay đổi trong transaction rồi rollback.','["Phân biệt định nghĩa cấu trúc và thao tác dữ liệu", "Đọc NOT NULL, UNIQUE, CHECK và REFERENCES", "Biết cách thử thay đổi trong transaction rồi rollback"]'::jsonb,'SQL_BASIC',12 FROM course_categories c WHERE lower(trim(c.name))=lower('SQL cơ bản') ORDER BY c.created_at LIMIT 1 ON CONFLICT (slug) DO UPDATE SET category_id=EXCLUDED.category_id,title=EXCLUDED.title,description=EXCLUDED.description,objectives=EXCLUDED.objectives,practice_topic_code=EXCLUDED.practice_topic_code,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=1 ORDER BY s.created_at LIMIT 1),'326e1d30-74a7-550f-90a1-3094633ec64f'::uuid),l.id,'1. Mục tiêu và chuẩn bị','### Sau bài này bạn có thể
- Phân biệt định nghĩa cấu trúc và thao tác dữ liệu
- Đọc NOT NULL, UNIQUE, CHECK và REFERENCES
- Biết cách thử thay đổi trong transaction rồi rollback

### Kiến thức cần có
Đã học các bài đứng trước trong mục SQL cơ bản; đọc ví dụ theo đúng tên bảng/cột của bộ sách.

### Cách học
Đọc lý thuyết, dự đoán kết quả SQL trước khi chạy, sau đó đối chiếu giải thích. Các ví dụ truy vấn dùng bộ sách với 15 sách, 5 tác giả, 5 nhà xuất bản, 5 thể loại và 20 liên kết sách–tác giả. Một đầu sách có thể có nhiều tác giả; giá tính theo VND. Không cộng lẫn số đầu sách với số cuốn tồn kho.',NULL,'{"type": "info", "title": "Môi trường ví dụ", "content": "Cú pháp theo PostgreSQL 16. Ví dụ CREATE/UPDATE/DELETE và EXPLAIN được đánh dấu riêng, không phải đáp án cho phòng chỉ cho phép SELECT."}'::jsonb,1 FROM lessons l WHERE l.slug='tao-bang-them-sua-xoa-va-rang-buoc' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=2 ORDER BY s.created_at LIMIT 1),'feec9f86-ce5b-5be6-884b-b23ec09ca2ce'::uuid),l.id,'2. Khái niệm và cách suy luận','CREATE TABLE định nghĩa cấu trúc; INSERT thêm dòng; UPDATE sửa dòng; DELETE xóa dòng. Với INSERT nên viết danh sách cột để không phụ thuộc thứ tự vật lý. Với UPDATE và DELETE, WHERE là ranh giới thao tác: bỏ WHERE có thể tác động mọi dòng. SELECT kiểm tra phạm vi trước rồi mới thay đổi dữ liệu là một thói quen quan trọng.

Các ràng buộc ngăn trạng thái không hợp lệ: NOT NULL bắt buộc có giá trị; UNIQUE chống giá trị trùng theo quy tắc của khóa; CHECK kiểm tra biểu thức; FOREIGN KEY/REFERENCES kiểm tra liên kết. Với PostgreSQL, CHECK chỉ từ chối FALSE, còn NULL thường không bị từ chối nên cần NOT NULL khi muốn bắt buộc nhập. Trong books, price và stock_quantity đều NOT NULL và không âm.

BEGIN mở transaction, COMMIT chấp nhận thay đổi và ROLLBACK hủy các thay đổi chưa commit. Ví dụ bên dưới dùng bảng tạm và luôn rollback, không sửa catalog hay bộ dữ liệu Practice. Phòng truy vấn của ứng dụng có thể chỉ cho phép SELECT; các ví dụ quản trị này dùng để học trên PostgreSQL riêng, không phải để nộp thay câu trả lời SELECT. Xóa bảng DROP TABLE khác với xóa các dòng bằng DELETE.',NULL,NULL,2 FROM lessons l WHERE l.slug='tao-bang-them-sua-xoa-va-rang-buoc' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=3 ORDER BY s.created_at LIMIT 1),'8baa00f9-ad68-5b38-acf5-3f301ff962b4'::uuid),l.id,'3. Tạo bảng tạm và thử các thao tác','### Phân tích ví dụ
SELECT cho một dòng giá 120000 trong transaction. Sau ROLLBACK, bảng tạm vừa tạo và dữ liệu trong đó đều không còn.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.','BEGIN;
CREATE TEMP TABLE demo_books (
    id INT PRIMARY KEY,
    title TEXT NOT NULL,
    price NUMERIC(12,2) NOT NULL CHECK (price >= 0)
);
INSERT INTO demo_books (id, title, price) VALUES (1, ''Sách thử'', 100000);
UPDATE demo_books SET price = 120000 WHERE id = 1;
SELECT id, title, price FROM demo_books;
ROLLBACK;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,3 FROM lessons l WHERE l.slug='tao-bang-them-sua-xoa-va-rang-buoc' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=4 ORDER BY s.created_at LIMIT 1),'332f9760-e960-5e57-9465-358f3138bfc4'::uuid),l.id,'4. Xóa đúng phạm vi ở bảng tạm','### Phân tích ví dụ
Sau DELETE chỉ còn (2, 5) trong transaction. Câu lệnh không đụng tới books hoặc dữ liệu bài tập.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.','BEGIN;
CREATE TEMP TABLE demo_stock (id INT PRIMARY KEY, stock INT NOT NULL);
INSERT INTO demo_stock VALUES (1, 0), (2, 5);
DELETE FROM demo_stock WHERE stock = 0;
SELECT id, stock FROM demo_stock ORDER BY id;
ROLLBACK;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,4 FROM lessons l WHERE l.slug='tao-bang-them-sua-xoa-va-rang-buoc' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=5 ORDER BY s.created_at LIMIT 1),'1f5cb9a6-0512-5afe-9027-4aca57d1c0ce'::uuid),l.id,'5. Lỗi thường gặp và cách tránh','**1.** Không bỏ WHERE ở UPDATE/DELETE nếu chỉ muốn xử lý một phần dữ liệu.

**2.** Không thử DDL/DML trực tiếp trên dữ liệu quan trọng khi chưa xác định đúng môi trường.

**3.** CHECK(price >= 0) một mình không thay thế NOT NULL.

### Quy trình kiểm tra
Đọc lại điều kiện tại đúng giá trị ranh giới; kiểm tra NULL, dòng không có đối tác và trường hợp đồng giá. Một câu SQL chạy được chưa chứng minh kết quả đã đúng với yêu cầu.',NULL,'{"type": "warning", "title": "Kiểm tra ý nghĩa trước cú pháp", "content": "Không bỏ WHERE ở UPDATE/DELETE nếu chỉ muốn xử lý một phần dữ liệu."}'::jsonb,5 FROM lessons l WHERE l.slug='tao-bang-them-sua-xoa-va-rang-buoc' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=6 ORDER BY s.created_at LIMIT 1),'c1690b6e-1d07-5051-bc60-a70ca7fe1083'::uuid),l.id,'6. Tự kiểm tra và chuyển sang thực hành','**Câu 1: DELETE FROM demo_stock khác DROP TABLE demo_stock thế nào?**

Tự trả lời trước khi đọc. **Giải thích:** DELETE xóa dòng nhưng giữ cấu trúc; DROP xóa đối tượng bảng.

**Câu 2: Sau một lệnh lỗi trong transaction PostgreSQL cần làm gì?**

Tự trả lời trước khi đọc. **Giải thích:** Thông thường cần ROLLBACK, hoặc ROLLBACK TO SAVEPOINT nếu đã tạo savepoint trước lỗi.

### Thực hành sau khi học
Học cấu trúc và ràng buộc để hiểu vì sao các phép JOIN trong bài tập có kết quả hợp lệ.

[Mở database sách để thực hành](/practice/quan-ly-sach-va-tac-gia)

### Tài liệu tham khảo
- [Tài liệu PostgreSQL 16](https://www.postgresql.org/docs/16/queries-select-lists.html)',NULL,NULL,6 FROM lessons l WHERE l.slug='tao-bang-them-sua-xoa-va-rang-buoc' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lessons (id,category_id,title,slug,description,objectives,practice_topic_code,sort_order) SELECT 'bb43c7c3-48a7-59b4-95ad-f4a168e9c1b5',c.id,'01. INNER JOIN và thứ tự xử lý truy vấn','inner-join-va-thu-tu-xu-ly-truy-van','Chọn đúng cặp khóa để nối bảng; Dự đoán số dòng sau INNER JOIN; Hiểu thứ tự logic FROM đến ORDER BY.','["Chọn đúng cặp khóa để nối bảng", "Dự đoán số dòng sau INNER JOIN", "Hiểu thứ tự logic FROM đến ORDER BY"]'::jsonb,'SQL_ADVANCED',1 FROM course_categories c WHERE lower(trim(c.name))=lower('SQL nâng cao') ORDER BY c.created_at LIMIT 1 ON CONFLICT (slug) DO UPDATE SET category_id=EXCLUDED.category_id,title=EXCLUDED.title,description=EXCLUDED.description,objectives=EXCLUDED.objectives,practice_topic_code=EXCLUDED.practice_topic_code,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=1 ORDER BY s.created_at LIMIT 1),'d8a81e00-3c4e-5be7-bebd-138f95a50ae9'::uuid),l.id,'1. Mục tiêu và chuẩn bị','### Sau bài này bạn có thể
- Chọn đúng cặp khóa để nối bảng
- Dự đoán số dòng sau INNER JOIN
- Hiểu thứ tự logic FROM đến ORDER BY

### Kiến thức cần có
Đã học các bài SQL cơ bản và hiểu books, publishers, categories, authors, book_authors.

### Cách học
Đọc lý thuyết, dự đoán kết quả SQL trước khi chạy, sau đó đối chiếu giải thích. Các ví dụ truy vấn dùng bộ sách với 15 sách, 5 tác giả, 5 nhà xuất bản, 5 thể loại và 20 liên kết sách–tác giả. Một đầu sách có thể có nhiều tác giả; giá tính theo VND. Không cộng lẫn số đầu sách với số cuốn tồn kho.',NULL,'{"type": "info", "title": "Môi trường ví dụ", "content": "Cú pháp theo PostgreSQL 16. Ví dụ CREATE/UPDATE/DELETE và EXPLAIN được đánh dấu riêng, không phải đáp án cho phòng chỉ cho phép SELECT."}'::jsonb,1 FROM lessons l WHERE l.slug='inner-join-va-thu-tu-xu-ly-truy-van' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=2 ORDER BY s.created_at LIMIT 1),'fd2b97aa-68bd-5d77-ad45-20d12b80da90'::uuid),l.id,'2. Khái niệm và cách suy luận','INNER JOIN kết hợp hai bảng và chỉ giữ các cặp dòng thỏa điều kiện ON. Mỗi cặp khớp tạo một dòng kết quả. Nếu một dòng bên trái khớp ba dòng bên phải, kết quả có ba dòng cho đối tượng đó. Đây là đặc điểm của phép nối, không phải lỗi hoặc tác dụng của SELECT.

Bắt đầu bằng câu hỏi: một dòng kết quả đại diện cho cuốn sách hay cho một tác giả của cuốn sách? Với books nối publishers bằng publisher_id, mỗi sách mẫu có đúng một nhà xuất bản hợp lệ nên có 15 dòng. Với books nối book_authors, 15 sách tạo 20 dòng do có đồng tác giả. Điều kiện nối phải dựa vào quan hệ, không dựa vào việc hai cột có giá trị tình cờ trùng nhau.

Thứ tự xử lý logic hữu ích để đọc truy vấn là FROM/JOIN → WHERE → GROUP BY → HAVING → SELECT → DISTINCT → ORDER BY → LIMIT. Đây là cách hiểu ngữ nghĩa, không phải cam kết thứ tự thao tác vật lý của bộ tối ưu. WHERE lọc dòng sau khi hình thành nguồn dữ liệu; vì vậy có thể lọc p.city dù chỉ trả title. Bí danh SELECT chưa có sẵn ở WHERE cùng cấp. Khi nối nhiều bảng, đặt bí danh ngắn nhưng nhất quán và ghi tiền tố cho cột dễ mơ hồ.',NULL,NULL,2 FROM lessons l WHERE l.slug='inner-join-va-thu-tu-xu-ly-truy-van' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=3 ORDER BY s.created_at LIMIT 1),'1350ef38-0fe9-50f2-a194-6352fd2c1013'::uuid),l.id,'3. Nối sách với nhà xuất bản','### Phân tích ví dụ
Có 15 dòng, mỗi dòng là một sách. Cánh Diều không xuất hiện vì INNER JOIN chỉ giữ cặp đã có sách.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **15**.

| book_id | title | publisher_name |
| --- | --- | --- |
| 1 | SQL từ cơ bản đến thực hành | Tri Thức Mới |
| 2 | Thiết kế cơ sở dữ liệu | Ánh Dương |
| 3 | Python cho người mới bắt đầu | Hành Trang |
| 4 | Mùa hạ bên ô cửa | Trang Sách Xanh |
| 5 | Những con đường cũ | Tri Thức Mới |

Bảng trên minh họa 5 dòng đầu; truy vấn trả đầy đủ 15 dòng.','SELECT b.book_id, b.title, p.name AS publisher_name
FROM books AS b
INNER JOIN publishers AS p
  ON p.publisher_id = b.publisher_id
ORDER BY b.book_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,3 FROM lessons l WHERE l.slug='inner-join-va-thu-tu-xu-ly-truy-van' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=4 ORDER BY s.created_at LIMIT 1),'3326d70c-78b9-5520-9850-00c62a9d446f'::uuid),l.id,'4. JOIN và lọc theo bảng được nối','### Phân tích ví dụ
Có 6 sách: 1, 4, 8, 9, 12, 13. Sách 5 bị loại vì hết hàng dù nhà xuất bản ở Hà Nội.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **6**.

| book_id | title | publisher_name |
| --- | --- | --- |
| 1 | SQL từ cơ bản đến thực hành | Tri Thức Mới |
| 4 | Mùa hạ bên ô cửa | Trang Sách Xanh |
| 8 | Quản lý tài chính cá nhân | Trang Sách Xanh |
| 9 | Nghệ thuật làm việc nhóm | Tri Thức Mới |
| 12 | Hành tinh xanh của chúng ta | Trang Sách Xanh |

Bảng trên minh họa 5 dòng đầu; truy vấn trả đầy đủ 6 dòng.','SELECT b.book_id, b.title, p.name AS publisher_name
FROM books b
JOIN publishers p ON p.publisher_id = b.publisher_id
WHERE p.city = ''Hà Nội'' AND b.stock_quantity > 0
ORDER BY b.book_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,4 FROM lessons l WHERE l.slug='inner-join-va-thu-tu-xu-ly-truy-van' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=5 ORDER BY s.created_at LIMIT 1),'6b54a91c-6b8e-5d66-8d25-3f9669640f1c'::uuid),l.id,'5. Lỗi thường gặp và cách tránh','**1.** Nếu cùng tên cột ở nhiều bảng, dùng tiền tố như b.publisher_id.

**2.** Không thêm DISTINCT trước khi hiểu vì sao phép nối tạo nhiều dòng.

**3.** Thứ tự logic không có nghĩa cơ sở dữ liệu luôn chạy vòng lặp JOIN rồi mới lọc.

### Quy trình kiểm tra
Đọc lại điều kiện tại đúng giá trị ranh giới; kiểm tra NULL, dòng không có đối tác và trường hợp đồng giá. Một câu SQL chạy được chưa chứng minh kết quả đã đúng với yêu cầu.',NULL,'{"type": "warning", "title": "Kiểm tra ý nghĩa trước cú pháp", "content": "Nếu cùng tên cột ở nhiều bảng, dùng tiền tố như b.publisher_id."}'::jsonb,5 FROM lessons l WHERE l.slug='inner-join-va-thu-tu-xu-ly-truy-van' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=6 ORDER BY s.created_at LIMIT 1),'775b263a-fd72-5fed-8f95-a9761f6a05f8'::uuid),l.id,'6. Tự kiểm tra và chuyển sang thực hành','**Câu 1: INNER JOIN có giữ nhà xuất bản chưa có sách không?**

Tự trả lời trước khi đọc. **Giải thích:** Không; cần LEFT JOIN từ publishers.

**Câu 2: Nối books.book_id = publishers.publisher_id có đúng không?**

Tự trả lời trước khi đọc. **Giải thích:** Không. Khóa nối đúng là books.publisher_id = publishers.publisher_id.

### Thực hành sau khi học
Làm các bài dễ 1–3, 5–9 trong database sách.

[Mở database sách để thực hành](/practice/quan-ly-sach-va-tac-gia)

### Tài liệu tham khảo
- [Tài liệu PostgreSQL 16](https://www.postgresql.org/docs/16/queries-table-expressions.html)',NULL,NULL,6 FROM lessons l WHERE l.slug='inner-join-va-thu-tu-xu-ly-truy-van' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lessons (id,category_id,title,slug,description,objectives,practice_topic_code,sort_order) SELECT '8b62acaa-10ea-5e8f-82c3-8775a60f7c1f',c.id,'02. LEFT JOIN, NULL và điều kiện ON/WHERE','left-join-va-dieu-kien-on-where','Giữ bản ghi phía trái dù không có đối tác; Phân biệt điều kiện trong ON và WHERE; Đếm đúng bản ghi con khi LEFT JOIN.','["Giữ bản ghi phía trái dù không có đối tác", "Phân biệt điều kiện trong ON và WHERE", "Đếm đúng bản ghi con khi LEFT JOIN"]'::jsonb,'SQL_ADVANCED',2 FROM course_categories c WHERE lower(trim(c.name))=lower('SQL nâng cao') ORDER BY c.created_at LIMIT 1 ON CONFLICT (slug) DO UPDATE SET category_id=EXCLUDED.category_id,title=EXCLUDED.title,description=EXCLUDED.description,objectives=EXCLUDED.objectives,practice_topic_code=EXCLUDED.practice_topic_code,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=1 ORDER BY s.created_at LIMIT 1),'c5a99d20-eb76-5cef-b8a2-7b46a33f0449'::uuid),l.id,'1. Mục tiêu và chuẩn bị','### Sau bài này bạn có thể
- Giữ bản ghi phía trái dù không có đối tác
- Phân biệt điều kiện trong ON và WHERE
- Đếm đúng bản ghi con khi LEFT JOIN

### Kiến thức cần có
Đã học các bài SQL cơ bản và hiểu books, publishers, categories, authors, book_authors.

### Cách học
Đọc lý thuyết, dự đoán kết quả SQL trước khi chạy, sau đó đối chiếu giải thích. Các ví dụ truy vấn dùng bộ sách với 15 sách, 5 tác giả, 5 nhà xuất bản, 5 thể loại và 20 liên kết sách–tác giả. Một đầu sách có thể có nhiều tác giả; giá tính theo VND. Không cộng lẫn số đầu sách với số cuốn tồn kho.',NULL,'{"type": "info", "title": "Môi trường ví dụ", "content": "Cú pháp theo PostgreSQL 16. Ví dụ CREATE/UPDATE/DELETE và EXPLAIN được đánh dấu riêng, không phải đáp án cho phòng chỉ cho phép SELECT."}'::jsonb,1 FROM lessons l WHERE l.slug='left-join-va-dieu-kien-on-where' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=2 ORDER BY s.created_at LIMIT 1),'4420b438-1a7c-59ab-82d7-d8d5c4ba466a'::uuid),l.id,'2. Khái niệm và cách suy luận','LEFT JOIN giữ mọi dòng ở bảng bên trái. Với dòng không tìm được đối tác thỏa ON, các cột của bảng bên phải được điền NULL. Chọn bảng nào đứng bên trái dựa vào đối tượng cần giữ đầy đủ: muốn xem mọi nhà xuất bản thì FROM publishers, không phải FROM books.

Điều kiện ON quyết định dòng nào được xem là một cặp khớp. WHERE lọc kết quả sau đó. Nếu dùng LEFT JOIN rồi WHERE b.stock_quantity > 0, dòng nhà xuất bản không có sách bị loại vì NULL > 0 là UNKNOWN. Đưa điều kiện đó vào ON giữ nhà xuất bản nhưng chỉ gắn sách còn hàng. Hai câu hỏi nghiệp vụ khác nhau nên không tùy tiện đổi chỗ điều kiện.

Khi gom nhóm kết quả LEFT JOIN, COUNT(*) đếm cả dòng đệm cho nhà xuất bản không khớp; COUNT(b.book_id) đếm 0 nếu không có sách thật. Muốn tìm bản ghi cha không có con, kiểm tra b.book_id IS NULL khi book_id là khóa chính không NULL, hoặc dùng NOT EXISTS. Không kiểm tra một cột bên phải có thể NULL ngay cả khi đã khớp, vì sẽ nhầm giữa thiếu đối tác và đối tác có giá trị trống.',NULL,NULL,2 FROM lessons l WHERE l.slug='left-join-va-dieu-kien-on-where' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=3 ORDER BY s.created_at LIMIT 1),'0e699f28-2971-5185-94d0-8b9ab7e108f2'::uuid),l.id,'3. Giữ mọi nhà xuất bản','### Phân tích ví dụ
Có 16 dòng: 15 sách và một dòng Cánh Diều với book_id/title là NULL. LEFT JOIN không giới hạn một dòng đầu ra cho mỗi nhà xuất bản.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **16**.

| publisher_id | publisher_name | book_id | title |
| --- | --- | --- | --- |
| 1 | Tri Thức Mới | 1 | SQL từ cơ bản đến thực hành |
| 1 | Tri Thức Mới | 5 | Những con đường cũ |
| 1 | Tri Thức Mới | 9 | Nghệ thuật làm việc nhóm |
| 1 | Tri Thức Mới | 13 | Chuyến phiêu lưu của Mít |
| 2 | Ánh Dương | 2 | Thiết kế cơ sở dữ liệu |

Bảng trên minh họa 5 dòng đầu; truy vấn trả đầy đủ 16 dòng.','SELECT p.publisher_id, p.name AS publisher_name, b.book_id, b.title
FROM publishers p
LEFT JOIN books b ON b.publisher_id = p.publisher_id
ORDER BY p.publisher_id, b.book_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,3 FROM lessons l WHERE l.slug='left-join-va-dieu-kien-on-where' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=4 ORDER BY s.created_at LIMIT 1),'f9d53fd4-36e2-5ae9-a987-ebab957addb0'::uuid),l.id,'4. Chỉ gắn sách còn hàng','### Phân tích ví dụ
Có 13 dòng: 12 sách còn hàng và dòng Cánh Diều không có sách phù hợp. Chuyển điều kiện tồn kho sang WHERE sẽ làm mất Cánh Diều.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **13**.

| publisher_id | name | book_id | stock_quantity |
| --- | --- | --- | --- |
| 1 | Tri Thức Mới | 1 | 3 |
| 1 | Tri Thức Mới | 9 | 24 |
| 1 | Tri Thức Mới | 13 | 15 |
| 2 | Ánh Dương | 2 | 8 |
| 2 | Ánh Dương | 6 | 3 |

Bảng trên minh họa 5 dòng đầu; truy vấn trả đầy đủ 13 dòng.','SELECT p.publisher_id, p.name, b.book_id, b.stock_quantity
FROM publishers p
LEFT JOIN books b
  ON b.publisher_id = p.publisher_id AND b.stock_quantity > 0
ORDER BY p.publisher_id, b.book_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,4 FROM lessons l WHERE l.slug='left-join-va-dieu-kien-on-where' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=5 ORDER BY s.created_at LIMIT 1),'8f0f052a-b6a4-59f5-afb2-08ea5be51417'::uuid),l.id,'5. Lỗi thường gặp và cách tránh','**1.** WHERE trên cột bên phải có thể làm mất ý nghĩa giữ dòng của LEFT JOIN.

**2.** COUNT(*) không phải số sách trong nhóm có dòng đệm NULL.

**3.** Tìm không có đối tác bằng khóa chính không NULL của phía phải.

### Quy trình kiểm tra
Đọc lại điều kiện tại đúng giá trị ranh giới; kiểm tra NULL, dòng không có đối tác và trường hợp đồng giá. Một câu SQL chạy được chưa chứng minh kết quả đã đúng với yêu cầu.',NULL,'{"type": "warning", "title": "Kiểm tra ý nghĩa trước cú pháp", "content": "WHERE trên cột bên phải có thể làm mất ý nghĩa giữ dòng của LEFT JOIN."}'::jsonb,5 FROM lessons l WHERE l.slug='left-join-va-dieu-kien-on-where' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=6 ORDER BY s.created_at LIMIT 1),'68709f2f-eb36-5d8a-a122-a3e4c4b06df2'::uuid),l.id,'6. Tự kiểm tra và chuyển sang thực hành','**Câu 1: NULL ở dòng Cánh Diều có phải dữ liệu gốc thiếu bắt buộc không?**

Tự trả lời trước khi đọc. **Giải thích:** Không; NULL được sinh bởi LEFT JOIN không khớp.

**Câu 2: Giữ mọi tác giả dù chưa có sách thì bảng nào bên trái?**

Tự trả lời trước khi đọc. **Giải thích:** authors, rồi LEFT JOIN sang book_authors và books khi cần.

### Thực hành sau khi học
Làm bài dễ 10 và trung bình 2, 6 để giữ đối tượng có 0 sách.

[Mở database sách để thực hành](/practice/quan-ly-sach-va-tac-gia)

### Tài liệu tham khảo
- [Tài liệu PostgreSQL 16](https://www.postgresql.org/docs/16/queries-table-expressions.html)',NULL,NULL,6 FROM lessons l WHERE l.slug='left-join-va-dieu-kien-on-where' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lessons (id,category_id,title,slug,description,objectives,practice_topic_code,sort_order) SELECT 'c90bd6bf-0aef-5966-a494-fa33f706418c',c.id,'03. JOIN nhiều–nhiều, SELF JOIN và CROSS JOIN','join-nhieu-nhieu-self-join-va-cross-join','Đi qua bảng liên kết nhiều–nhiều; Tự nối một bảng với hai bí danh; Tránh nhân bản dữ liệu ngoài ý muốn.','["Đi qua bảng liên kết nhiều–nhiều", "Tự nối một bảng với hai bí danh", "Tránh nhân bản dữ liệu ngoài ý muốn"]'::jsonb,'SQL_ADVANCED',3 FROM course_categories c WHERE lower(trim(c.name))=lower('SQL nâng cao') ORDER BY c.created_at LIMIT 1 ON CONFLICT (slug) DO UPDATE SET category_id=EXCLUDED.category_id,title=EXCLUDED.title,description=EXCLUDED.description,objectives=EXCLUDED.objectives,practice_topic_code=EXCLUDED.practice_topic_code,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=1 ORDER BY s.created_at LIMIT 1),'f01598ec-83a3-58a0-b61a-5f90ebf4d598'::uuid),l.id,'1. Mục tiêu và chuẩn bị','### Sau bài này bạn có thể
- Đi qua bảng liên kết nhiều–nhiều
- Tự nối một bảng với hai bí danh
- Tránh nhân bản dữ liệu ngoài ý muốn

### Kiến thức cần có
Đã học các bài SQL cơ bản và hiểu books, publishers, categories, authors, book_authors.

### Cách học
Đọc lý thuyết, dự đoán kết quả SQL trước khi chạy, sau đó đối chiếu giải thích. Các ví dụ truy vấn dùng bộ sách với 15 sách, 5 tác giả, 5 nhà xuất bản, 5 thể loại và 20 liên kết sách–tác giả. Một đầu sách có thể có nhiều tác giả; giá tính theo VND. Không cộng lẫn số đầu sách với số cuốn tồn kho.',NULL,'{"type": "info", "title": "Môi trường ví dụ", "content": "Cú pháp theo PostgreSQL 16. Ví dụ CREATE/UPDATE/DELETE và EXPLAIN được đánh dấu riêng, không phải đáp án cho phòng chỉ cho phép SELECT."}'::jsonb,1 FROM lessons l WHERE l.slug='join-nhieu-nhieu-self-join-va-cross-join' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=2 ORDER BY s.created_at LIMIT 1),'be9c4f10-9bed-5d14-b885-cfbff6b4ebc5'::uuid),l.id,'2. Khái niệm và cách suy luận','Quan hệ nhiều–nhiều cần một bảng trung gian. Đường nối là books.book_id → book_authors.book_id và book_authors.author_id → authors.author_id. Bỏ bảng trung gian hoặc nối trực tiếp hai khóa chính cho quan hệ sai. Một sách có nhiều tác giả nên thông tin sách xuất hiện nhiều lần ở mức dữ liệu cặp sách–tác giả.

SELF JOIN là dùng một bảng ở hai vai trò với hai bí danh, không phải loại cú pháp riêng. Tìm các cặp tác giả cùng viết một sách bằng tự nối book_authors theo book_id. Thêm x.author_id < y.author_id để loại trường hợp tự ghép và tránh hai cặp đối xứng (1,2)/(2,1). Nếu muốn danh sách cặp duy nhất qua nhiều sách, xem xét DISTINCT trên đúng các cột mong muốn.

CROSS JOIN tạo tích Descartes: m dòng nhân n dòng cho m × n cặp. Nó hợp lý khi cần mọi tổ hợp, ví dụ nhà xuất bản–thể loại để lập ma trận báo cáo, nhưng nguy hiểm khi vô tình bỏ ON. Trước khi SUM giá hoặc tồn kho sau một phép nối nhiều–nhiều, phải xét cấp dữ liệu: sách xuất hiện hai lần có thể làm giá trị tồn kho bị cộng hai lần.',NULL,NULL,2 FROM lessons l WHERE l.slug='join-nhieu-nhieu-self-join-va-cross-join' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=3 ORDER BY s.created_at LIMIT 1),'17e79b81-0ea6-574f-98d6-a7e5d28cd7f6'::uuid),l.id,'3. Nối đủ đường sách–tác giả','### Phân tích ví dụ
Có 20 dòng cho 15 sách. Đếm dòng truy vấn này không phải đếm đầu sách.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **20**.

| book_id | title | author_name | author_order |
| --- | --- | --- | --- |
| 1 | SQL từ cơ bản đến thực hành | Nguyễn Minh An | 1 |
| 1 | SQL từ cơ bản đến thực hành | Trần Thu Hà | 2 |
| 2 | Thiết kế cơ sở dữ liệu | Trần Thu Hà | 1 |
| 2 | Thiết kế cơ sở dữ liệu | Lê Hoàng Nam | 2 |
| 3 | Python cho người mới bắt đầu | Lê Hoàng Nam | 1 |

Bảng trên minh họa 5 dòng đầu; truy vấn trả đầy đủ 20 dòng.','SELECT b.book_id, b.title, a.full_name AS author_name, ba.author_order
FROM books b
JOIN book_authors ba ON ba.book_id = b.book_id
JOIN authors a ON a.author_id = ba.author_id
ORDER BY b.book_id, ba.author_order;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,3 FROM lessons l WHERE l.slug='join-nhieu-nhieu-self-join-va-cross-join' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=4 ORDER BY s.created_at LIMIT 1),'b5d1baa9-8d21-5567-80e1-cea59faa07aa'::uuid),l.id,'4. Tìm các cặp đồng tác giả','### Phân tích ví dụ
Có 5 cặp tương ứng sách 1–5. Mỗi cặp xuất hiện một chiều và không ghép cùng tác giả.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **5**.

| book_id | author_one | author_two |
| --- | --- | --- |
| 1 | Nguyễn Minh An | Trần Thu Hà |
| 2 | Trần Thu Hà | Lê Hoàng Nam |
| 3 | Lê Hoàng Nam | Phạm Ngọc Linh |
| 4 | Phạm Ngọc Linh | Đỗ Quốc Bảo |
| 5 | Nguyễn Minh An | Đỗ Quốc Bảo |','SELECT x.book_id, a.full_name AS author_one, b.full_name AS author_two
FROM book_authors x
JOIN book_authors y ON y.book_id = x.book_id AND x.author_id < y.author_id
JOIN authors a ON a.author_id = x.author_id
JOIN authors b ON b.author_id = y.author_id
ORDER BY x.book_id, x.author_id, y.author_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,4 FROM lessons l WHERE l.slug='join-nhieu-nhieu-self-join-va-cross-join' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=5 ORDER BY s.created_at LIMIT 1),'6b830b1b-cd1f-5c69-8fa3-6b52c10dc06b'::uuid),l.id,'5. Lỗi thường gặp và cách tránh','**1.** Không cộng giá trị sách sau JOIN tác giả rồi coi đó là tổng kho đầu sách.

**2.** SELF JOIN cần bí danh riêng cho từng vai trò.

**3.** CROSS JOIN 15 sách và 5 tác giả cho 75 cặp, không phải 20 liên kết thực tế.

### Quy trình kiểm tra
Đọc lại điều kiện tại đúng giá trị ranh giới; kiểm tra NULL, dòng không có đối tác và trường hợp đồng giá. Một câu SQL chạy được chưa chứng minh kết quả đã đúng với yêu cầu.',NULL,'{"type": "warning", "title": "Kiểm tra ý nghĩa trước cú pháp", "content": "Không cộng giá trị sách sau JOIN tác giả rồi coi đó là tổng kho đầu sách."}'::jsonb,5 FROM lessons l WHERE l.slug='join-nhieu-nhieu-self-join-va-cross-join' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=6 ORDER BY s.created_at LIMIT 1),'840acb20-3baf-5d66-8937-9c6a1d2c6a9a'::uuid),l.id,'6. Tự kiểm tra và chuyển sang thực hành','**Câu 1: Vì sao x.author_id <> y.author_id chưa tránh lặp cặp?**

Tự trả lời trước khi đọc. **Giải thích:** Vẫn có cả (1,2) và (2,1); dùng < để giữ một chiều.

**Câu 2: Đếm số sách sau JOIN bảng tác giả dùng gì?**

Tự trả lời trước khi đọc. **Giải thích:** COUNT(DISTINCT b.book_id), hoặc tổng hợp books trước tùy yêu cầu.

### Thực hành sau khi học
Làm bài dễ 4, 7, 8 và đối chiếu 20 dòng book_authors.

[Mở database sách để thực hành](/practice/quan-ly-sach-va-tac-gia)

### Tài liệu tham khảo
- [Tài liệu PostgreSQL 16](https://www.postgresql.org/docs/16/queries-table-expressions.html)',NULL,NULL,6 FROM lessons l WHERE l.slug='join-nhieu-nhieu-self-join-va-cross-join' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lessons (id,category_id,title,slug,description,objectives,practice_topic_code,sort_order) SELECT '1d56c239-e29b-5f26-b7c3-f4ff4c421d74',c.id,'04. COUNT, SUM, AVG, MIN, MAX và mức dữ liệu','ham-tong-hop-count-sum-avg-min-max','Phân biệt COUNT(*) với COUNT(cột); Tính tổng và trung bình đúng cấp; Hiểu ảnh hưởng của NULL và JOIN nhiều–nhiều.','["Phân biệt COUNT(*) với COUNT(cột)", "Tính tổng và trung bình đúng cấp", "Hiểu ảnh hưởng của NULL và JOIN nhiều–nhiều"]'::jsonb,'SQL_ADVANCED',4 FROM course_categories c WHERE lower(trim(c.name))=lower('SQL nâng cao') ORDER BY c.created_at LIMIT 1 ON CONFLICT (slug) DO UPDATE SET category_id=EXCLUDED.category_id,title=EXCLUDED.title,description=EXCLUDED.description,objectives=EXCLUDED.objectives,practice_topic_code=EXCLUDED.practice_topic_code,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=1 ORDER BY s.created_at LIMIT 1),'ac4bdb00-cc5b-574f-ac15-24a6102e203f'::uuid),l.id,'1. Mục tiêu và chuẩn bị','### Sau bài này bạn có thể
- Phân biệt COUNT(*) với COUNT(cột)
- Tính tổng và trung bình đúng cấp
- Hiểu ảnh hưởng của NULL và JOIN nhiều–nhiều

### Kiến thức cần có
Đã học các bài SQL cơ bản và hiểu books, publishers, categories, authors, book_authors.

### Cách học
Đọc lý thuyết, dự đoán kết quả SQL trước khi chạy, sau đó đối chiếu giải thích. Các ví dụ truy vấn dùng bộ sách với 15 sách, 5 tác giả, 5 nhà xuất bản, 5 thể loại và 20 liên kết sách–tác giả. Một đầu sách có thể có nhiều tác giả; giá tính theo VND. Không cộng lẫn số đầu sách với số cuốn tồn kho.',NULL,'{"type": "info", "title": "Môi trường ví dụ", "content": "Cú pháp theo PostgreSQL 16. Ví dụ CREATE/UPDATE/DELETE và EXPLAIN được đánh dấu riêng, không phải đáp án cho phòng chỉ cho phép SELECT."}'::jsonb,1 FROM lessons l WHERE l.slug='ham-tong-hop-count-sum-avg-min-max' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=2 ORDER BY s.created_at LIMIT 1),'db7d34a4-c87a-546b-ab7b-3424e31f5274'::uuid),l.id,'2. Khái niệm và cách suy luận','Hàm tổng hợp gộp nhiều giá trị thành một giá trị. COUNT(*) đếm dòng; COUNT(cột) đếm giá trị không NULL; COUNT(DISTINCT cột) đếm giá trị khác nhau và không NULL. SUM cộng giá trị; AVG lấy trung bình các giá trị có mặt; MIN/MAX tìm cực trị. Không có GROUP BY thì tổng hợp trên toàn bộ đầu vào.

Trước khi tổng hợp, xác định cấp dữ liệu. books có một dòng mỗi đầu sách nên SUM(stock_quantity) là số cuốn tồn kho. Sau JOIN với book_authors, một sách đồng tác giả xuất hiện hai lần và làm SUM bị nhân bản. SUM(DISTINCT price) không sửa được vấn đề vì hai sách khác nhau có cùng giá sẽ bị gộp nhầm. Cần tổng hợp từ nguồn đúng hoặc gom lại theo book_id trước.

AVG(price) là trung bình giá giữa các đầu sách, không tính trọng số tồn kho. Muốn giá bình quân theo số cuốn đang có, tính SUM(price * stock_quantity) / SUM(stock_quantity), bảo vệ mẫu số bằng NULLIF. ROUND chỉ định số chữ số thập phân cuối. COUNT trả 0 khi không có dòng; SUM/AVG thường trả NULL trên đầu vào rỗng, nên COALESCE chỉ thêm khi giá trị thay thế phù hợp.',NULL,NULL,2 FROM lessons l WHERE l.slug='ham-tong-hop-count-sum-avg-min-max' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=3 ORDER BY s.created_at LIMIT 1),'d9a89e1b-a27c-515b-81ea-f4ce888952ae'::uuid),l.id,'3. Thống kê toàn bộ đầu sách','### Phân tích ví dụ
Một dòng: book_count = 15, total_stock = 150, average_price = 123800.00, min_price = 75000, max_price = 180000.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **1**.

| book_count | total_stock | average_price | min_price | max_price |
| --- | --- | --- | --- | --- |
| 15 | 150 | 123800.0 | 75000.0 | 180000.0 |','SELECT COUNT(*) AS book_count, SUM(stock_quantity) AS total_stock,
       ROUND(AVG(price), 2) AS average_price,
       MIN(price) AS min_price, MAX(price) AS max_price
FROM books;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,3 FROM lessons l WHERE l.slug='ham-tong-hop-count-sum-avg-min-max' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=4 ORDER BY s.created_at LIMIT 1),'51eef515-b2db-52c3-8f74-b3b18734aef7'::uuid),l.id,'4. Trung bình có trọng số tồn kho','### Phân tích ví dụ
weighted_price = 155040.00, khác trung bình giá 123800.00 vì sách đắt đang có nhiều cuốn hơn. Hai thước đo trả lời hai câu hỏi khác nhau.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **1**.

| weighted_price |
| --- |
| 155040.0 |','SELECT ROUND(SUM(price * stock_quantity) /
             NULLIF(SUM(stock_quantity), 0), 2) AS weighted_price
FROM books;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,4 FROM lessons l WHERE l.slug='ham-tong-hop-count-sum-avg-min-max' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=5 ORDER BY s.created_at LIMIT 1),'446aa497-7be4-571e-bd10-9ac745070fc0'::uuid),l.id,'5. Lỗi thường gặp và cách tránh','**1.** Không dùng SUM(DISTINCT price) để sửa nhân bản do JOIN sai cấp.

**2.** COUNT(price) bỏ qua NULL, COUNT(*) không bỏ dòng.

**3.** Làm tròn từng dòng trước AVG có thể khác làm tròn kết quả AVG cuối.

### Quy trình kiểm tra
Đọc lại điều kiện tại đúng giá trị ranh giới; kiểm tra NULL, dòng không có đối tác và trường hợp đồng giá. Một câu SQL chạy được chưa chứng minh kết quả đã đúng với yêu cầu.',NULL,'{"type": "warning", "title": "Kiểm tra ý nghĩa trước cú pháp", "content": "Không dùng SUM(DISTINCT price) để sửa nhân bản do JOIN sai cấp."}'::jsonb,5 FROM lessons l WHERE l.slug='ham-tong-hop-count-sum-avg-min-max' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=6 ORDER BY s.created_at LIMIT 1),'b827a0fe-a799-5d25-bf75-e66c4824fbc2'::uuid),l.id,'6. Tự kiểm tra và chuyển sang thực hành','**Câu 1: COUNT(DISTINCT publisher_id) ở books bằng bao nhiêu?**

Tự trả lời trước khi đọc. **Giải thích:** 4, trong khi publishers có 5 dòng.

**Câu 2: Vì sao đếm 20 dòng sau JOIN tác giả không phải 20 sách?**

Tự trả lời trước khi đọc. **Giải thích:** Vì đầu vào là cặp sách–tác giả, không còn là đầu sách.

### Thực hành sau khi học
Chuẩn bị cho toàn bộ 10 bài trung bình; phân biệt đầu sách và số cuốn.

[Mở database sách để thực hành](/practice/quan-ly-sach-va-tac-gia)

### Tài liệu tham khảo
- [Tài liệu PostgreSQL 16](https://www.postgresql.org/docs/16/functions-aggregate.html)',NULL,NULL,6 FROM lessons l WHERE l.slug='ham-tong-hop-count-sum-avg-min-max' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lessons (id,category_id,title,slug,description,objectives,practice_topic_code,sort_order) SELECT '58b495c3-ca1a-51b5-b950-75bcbce739f1',c.id,'05. GROUP BY, HAVING và tổng hợp có điều kiện','group-by-having-va-tong-hop-co-dieu-kien','Gom dữ liệu theo khóa nghiệp vụ; Phân biệt WHERE với HAVING; Đếm nhiều trạng thái bằng SUM CASE.','["Gom dữ liệu theo khóa nghiệp vụ", "Phân biệt WHERE với HAVING", "Đếm nhiều trạng thái bằng SUM CASE"]'::jsonb,'SQL_ADVANCED',5 FROM course_categories c WHERE lower(trim(c.name))=lower('SQL nâng cao') ORDER BY c.created_at LIMIT 1 ON CONFLICT (slug) DO UPDATE SET category_id=EXCLUDED.category_id,title=EXCLUDED.title,description=EXCLUDED.description,objectives=EXCLUDED.objectives,practice_topic_code=EXCLUDED.practice_topic_code,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=1 ORDER BY s.created_at LIMIT 1),'fa931ffc-8435-5282-b0c6-8d3fbb9489fb'::uuid),l.id,'1. Mục tiêu và chuẩn bị','### Sau bài này bạn có thể
- Gom dữ liệu theo khóa nghiệp vụ
- Phân biệt WHERE với HAVING
- Đếm nhiều trạng thái bằng SUM CASE

### Kiến thức cần có
Đã học các bài SQL cơ bản và hiểu books, publishers, categories, authors, book_authors.

### Cách học
Đọc lý thuyết, dự đoán kết quả SQL trước khi chạy, sau đó đối chiếu giải thích. Các ví dụ truy vấn dùng bộ sách với 15 sách, 5 tác giả, 5 nhà xuất bản, 5 thể loại và 20 liên kết sách–tác giả. Một đầu sách có thể có nhiều tác giả; giá tính theo VND. Không cộng lẫn số đầu sách với số cuốn tồn kho.',NULL,'{"type": "info", "title": "Môi trường ví dụ", "content": "Cú pháp theo PostgreSQL 16. Ví dụ CREATE/UPDATE/DELETE và EXPLAIN được đánh dấu riêng, không phải đáp án cho phòng chỉ cho phép SELECT."}'::jsonb,1 FROM lessons l WHERE l.slug='group-by-having-va-tong-hop-co-dieu-kien' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=2 ORDER BY s.created_at LIMIT 1),'4c478b28-4223-54cd-8e0f-576e58a50977'::uuid),l.id,'2. Khái niệm và cách suy luận','GROUP BY chia đầu vào thành các nhóm theo giá trị khóa. Kết quả thường có một dòng mỗi nhóm, với hàm tổng hợp tính trên từng nhóm. Nên nhóm theo mã ổn định và tên mô tả, ví dụ category_id, name, thay vì chỉ nhóm theo tên có thể trùng. Viết rõ các khóa và tên giúp hiểu ý nghĩa, ít phụ thuộc quy tắc suy diễn khóa của từng hệ quản trị.

WHERE loại dòng trước gom nhóm; HAVING loại nhóm sau khi đã có giá trị tổng hợp. Đếm sách còn hàng theo thể loại thì WHERE stock_quantity > 0 trước GROUP BY. Chỉ giữ thể loại có ít nhất hai sách còn hàng thì thêm HAVING COUNT(*) >= 2. Hai điều kiện có nhiệm vụ khác nhau và có thể cùng xuất hiện.

Tổng hợp có điều kiện dùng SUM(CASE WHEN ... THEN 1 ELSE 0 END) để tạo nhiều thống kê trên cùng một nhóm. Có thể tạo hai cột còn hàng/hết hàng mà không chạy hai truy vấn riêng. Với LEFT JOIN, chú ý dòng đệm không khớp để không đếm nhầm. SUM/COUNT dựa trên đầu vào sau JOIN nên vẫn sai nếu phép nối đã nhân bản sách. Một cột như title không có giá trị duy nhất khi nhóm theo category_id, nên không thể lấy một tiêu đề tùy ý đại diện cho cả nhóm.',NULL,NULL,2 FROM lessons l WHERE l.slug='group-by-having-va-tong-hop-co-dieu-kien' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=3 ORDER BY s.created_at LIMIT 1),'7926c8c1-c39f-591c-8f05-a20063767386'::uuid),l.id,'3. Gom nhóm và lọc nhóm','### Phân tích ví dụ
Có ba nhà xuất bản mã 1, 2, 3, mỗi đơn vị 4 sách. Mã 4 có 3 sách; mã 5 chưa có sách.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **3**.

| publisher_id | publisher_name | book_count |
| --- | --- | --- |
| 1 | Tri Thức Mới | 4 |
| 2 | Ánh Dương | 4 |
| 3 | Hành Trang | 4 |','SELECT p.publisher_id, p.name AS publisher_name, COUNT(b.book_id) AS book_count
FROM publishers p JOIN books b ON b.publisher_id = p.publisher_id
GROUP BY p.publisher_id, p.name
HAVING COUNT(b.book_id) >= 4
ORDER BY p.publisher_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,3 FROM lessons l WHERE l.slug='group-by-having-va-tong-hop-co-dieu-kien' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=4 ORDER BY s.created_at LIMIT 1),'53b54ed2-ab97-5b4a-a88d-334b8fe7c996'::uuid),l.id,'4. Hai thống kê trong một nhóm','### Phân tích ví dụ
Có 5 dòng. Công nghệ có 3 đầu sách còn hàng và 0 hết hàng; Văn học có 2 còn hàng, 1 hết hàng. Đây là đếm đầu sách, không phải số cuốn.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **5**.

| category_id | category_name | in_stock_count | out_of_stock_count |
| --- | --- | --- | --- |
| 1 | Công nghệ | 3 | 0 |
| 2 | Văn học | 2 | 1 |
| 3 | Kinh doanh | 3 | 0 |
| 4 | Khoa học | 2 | 1 |
| 5 | Thiếu nhi | 2 | 1 |','SELECT c.category_id, c.name AS category_name,
       SUM(CASE WHEN b.stock_quantity > 0 THEN 1 ELSE 0 END) AS in_stock_count,
       SUM(CASE WHEN b.stock_quantity = 0 THEN 1 ELSE 0 END) AS out_of_stock_count
FROM categories c JOIN books b ON b.category_id = c.category_id
GROUP BY c.category_id, c.name
ORDER BY c.category_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,4 FROM lessons l WHERE l.slug='group-by-having-va-tong-hop-co-dieu-kien' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=5 ORDER BY s.created_at LIMIT 1),'a5024dff-843e-5e57-99b4-66a4b168b793'::uuid),l.id,'5. Lỗi thường gặp và cách tránh','**1.** WHERE COUNT(*) > 1 không hợp lệ cùng cấp; dùng HAVING.

**2.** Không chọn title tùy ý trong nhóm chứa nhiều sách.

**3.** Giữ nhóm 0 sách thì bắt đầu từ bảng cha và LEFT JOIN.

### Quy trình kiểm tra
Đọc lại điều kiện tại đúng giá trị ranh giới; kiểm tra NULL, dòng không có đối tác và trường hợp đồng giá. Một câu SQL chạy được chưa chứng minh kết quả đã đúng với yêu cầu.',NULL,'{"type": "warning", "title": "Kiểm tra ý nghĩa trước cú pháp", "content": "WHERE COUNT(*) > 1 không hợp lệ cùng cấp; dùng HAVING."}'::jsonb,5 FROM lessons l WHERE l.slug='group-by-having-va-tong-hop-co-dieu-kien' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=6 ORDER BY s.created_at LIMIT 1),'18e8ca0e-29fc-5ebe-9f1b-330fbfae8597'::uuid),l.id,'6. Tự kiểm tra và chuyển sang thực hành','**Câu 1: Tính giá trung bình chỉ trên sách còn hàng thì lọc ở đâu?**

Tự trả lời trước khi đọc. **Giải thích:** WHERE stock_quantity > 0 trước GROUP BY.

**Câu 2: Chỉ giữ nhóm có giá trung bình trên 120000 thì lọc ở đâu?**

Tự trả lời trước khi đọc. **Giải thích:** HAVING AVG(price) > 120000 sau GROUP BY.

### Thực hành sau khi học
Làm các bài trung bình 1–10; xác định đúng cấp trước mỗi phép tổng hợp.

[Mở database sách để thực hành](/practice/quan-ly-sach-va-tac-gia)

### Tài liệu tham khảo
- [Tài liệu PostgreSQL 16](https://www.postgresql.org/docs/16/functions-aggregate.html)',NULL,NULL,6 FROM lessons l WHERE l.slug='group-by-having-va-tong-hop-co-dieu-kien' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lessons (id,category_id,title,slug,description,objectives,practice_topic_code,sort_order) SELECT '0d1cac3b-38bf-5cb5-a9f7-4d2b21182484',c.id,'06. Truy vấn con vô hướng và tương quan','truy-van-con-vo-huong-va-tuong-quan','Dùng kết quả một ô làm ngưỡng so sánh; Phân biệt tương quan với độc lập; Giữ các trường hợp đồng cực trị.','["Dùng kết quả một ô làm ngưỡng so sánh", "Phân biệt tương quan với độc lập", "Giữ các trường hợp đồng cực trị"]'::jsonb,'SQL_ADVANCED',6 FROM course_categories c WHERE lower(trim(c.name))=lower('SQL nâng cao') ORDER BY c.created_at LIMIT 1 ON CONFLICT (slug) DO UPDATE SET category_id=EXCLUDED.category_id,title=EXCLUDED.title,description=EXCLUDED.description,objectives=EXCLUDED.objectives,practice_topic_code=EXCLUDED.practice_topic_code,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=1 ORDER BY s.created_at LIMIT 1),'7b115f30-de44-5377-9c18-9db31707f66f'::uuid),l.id,'1. Mục tiêu và chuẩn bị','### Sau bài này bạn có thể
- Dùng kết quả một ô làm ngưỡng so sánh
- Phân biệt tương quan với độc lập
- Giữ các trường hợp đồng cực trị

### Kiến thức cần có
Đã học các bài SQL cơ bản và hiểu books, publishers, categories, authors, book_authors.

### Cách học
Đọc lý thuyết, dự đoán kết quả SQL trước khi chạy, sau đó đối chiếu giải thích. Các ví dụ truy vấn dùng bộ sách với 15 sách, 5 tác giả, 5 nhà xuất bản, 5 thể loại và 20 liên kết sách–tác giả. Một đầu sách có thể có nhiều tác giả; giá tính theo VND. Không cộng lẫn số đầu sách với số cuốn tồn kho.',NULL,'{"type": "info", "title": "Môi trường ví dụ", "content": "Cú pháp theo PostgreSQL 16. Ví dụ CREATE/UPDATE/DELETE và EXPLAIN được đánh dấu riêng, không phải đáp án cho phòng chỉ cho phép SELECT."}'::jsonb,1 FROM lessons l WHERE l.slug='truy-van-con-vo-huong-va-tuong-quan' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=2 ORDER BY s.created_at LIMIT 1),'cc14900f-3372-5c3e-9bb5-90e6c6dba470'::uuid),l.id,'2. Khái niệm và cách suy luận','Truy vấn con là một truy vấn nằm trong truy vấn khác. Truy vấn con vô hướng phải trả tối đa một dòng và một cột; không có dòng thì giá trị là NULL, nhiều hơn một dòng sẽ lỗi khi dùng như một giá trị đơn. AVG(price) không GROUP BY tạo một giá trị nên dùng được trong WHERE price > (...).

Truy vấn con tương quan tham chiếu bí danh ngoài. Trong bài so với trung bình cùng thể loại, điều kiện x.category_id = b.category_id làm nhóm dữ liệu bên trong phụ thuộc sách đang xét ở ngoài. Có thể hình dung mỗi dòng ngoài mang một khóa vào truy vấn con, nhưng bộ tối ưu có thể thực thi cách khác, không mặc định một lần chạy vật lý cho mỗi dòng.

Tìm sách đắt nhất mỗi thể loại bằng price = (SELECT MAX(...)) giữ tất cả sách đồng giá cực đại. LIMIT 1 trên toàn bảng không giải quyết cùng bài toán. Phân biệt trung bình toàn bộ và cùng nhóm; hiểu phạm vi bí danh: x ở trong, b ở ngoài. Viết x.category_id = x.category_id sẽ làm mất tương quan đúng và có thể cho kết quả sai mà không lỗi cú pháp.',NULL,NULL,2 FROM lessons l WHERE l.slug='truy-van-con-vo-huong-va-tuong-quan' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=3 ORDER BY s.created_at LIMIT 1),'088a1bfe-a2b1-5419-b436-d2ea284bbfb2'::uuid),l.id,'3. So với trung bình toàn bộ','### Phân tích ví dụ
Ngưỡng 123800.00 nên có 6 sách giá 145000 hoặc 180000.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **6**.

| book_id | title | price |
| --- | --- | --- |
| 3 | Python cho người mới bắt đầu | 145000.0 |
| 4 | Mùa hạ bên ô cửa | 180000.0 |
| 8 | Quản lý tài chính cá nhân | 145000.0 |
| 9 | Nghệ thuật làm việc nhóm | 180000.0 |
| 13 | Chuyến phiêu lưu của Mít | 145000.0 |

Bảng trên minh họa 5 dòng đầu; truy vấn trả đầy đủ 6 dòng.','SELECT b.book_id, b.title, b.price
FROM books b
WHERE b.price > (SELECT AVG(x.price) FROM books x)
ORDER BY b.book_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,3 FROM lessons l WHERE l.slug='truy-van-con-vo-huong-va-tuong-quan' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=4 ORDER BY s.created_at LIMIT 1),'1f4f79a4-fb8e-5292-b7b9-c2370a52cc7e'::uuid),l.id,'4. So với trung bình cùng thể loại','### Phân tích ví dụ
Có 7 sách: 3, 4, 8, 9, 12, 13, 14. Sách 12 giá 120000 vẫn cao hơn trung bình Khoa học là 98000, dù thấp hơn trung bình toàn bộ.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **7**.

| book_id | title | category_id | price |
| --- | --- | --- | --- |
| 3 | Python cho người mới bắt đầu | 1 | 145000.0 |
| 4 | Mùa hạ bên ô cửa | 2 | 180000.0 |
| 9 | Nghệ thuật làm việc nhóm | 3 | 180000.0 |
| 11 | Thế giới của những hạt nhỏ | 4 | 99000.0 |
| 12 | Hành tinh xanh của chúng ta | 4 | 120000.0 |

Bảng trên minh họa 5 dòng đầu; truy vấn trả đầy đủ 7 dòng.','SELECT b.book_id, b.title, b.category_id, b.price
FROM books b
WHERE b.price > (
    SELECT AVG(x.price) FROM books x WHERE x.category_id = b.category_id
)
ORDER BY b.book_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,4 FROM lessons l WHERE l.slug='truy-van-con-vo-huong-va-tuong-quan' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=5 ORDER BY s.created_at LIMIT 1),'a266d041-a2cc-5398-b382-63540feb4f0e'::uuid),l.id,'5. Lỗi thường gặp và cách tránh','**1.** Dùng = với truy vấn con nhiều dòng sẽ lỗi; cân nhắc IN hoặc sửa bài toán.

**2.** Không thêm LIMIT 1 để che lỗi nhiều dòng nếu thiếu quy tắc chọn rõ ràng.

**3.** Kiểm tra điều kiện tương quan có tham chiếu bí danh ngoài thật sự.

### Quy trình kiểm tra
Đọc lại điều kiện tại đúng giá trị ranh giới; kiểm tra NULL, dòng không có đối tác và trường hợp đồng giá. Một câu SQL chạy được chưa chứng minh kết quả đã đúng với yêu cầu.',NULL,'{"type": "warning", "title": "Kiểm tra ý nghĩa trước cú pháp", "content": "Dùng = với truy vấn con nhiều dòng sẽ lỗi; cân nhắc IN hoặc sửa bài toán."}'::jsonb,5 FROM lessons l WHERE l.slug='truy-van-con-vo-huong-va-tuong-quan' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=6 ORDER BY s.created_at LIMIT 1),'75a868ea-263e-5467-931d-94669df979e1'::uuid),l.id,'6. Tự kiểm tra và chuyển sang thực hành','**Câu 1: MAX không GROUP BY trên bảng rỗng trả gì?**

Tự trả lời trước khi đọc. **Giải thích:** Một dòng NULL; so sánh thông thường với NULL không giữ dòng nào.

**Câu 2: Giữ mọi sách cao nhất mỗi thể loại bằng cách nào?**

Tự trả lời trước khi đọc. **Giải thích:** So sánh với MAX theo category_id trong truy vấn con tương quan, hoặc dùng DENSE_RANK.

### Thực hành sau khi học
Làm bài khó 1–3 và 8; bài 8 cần thêm một tầng MAX.

[Mở database sách để thực hành](/practice/quan-ly-sach-va-tac-gia)

### Tài liệu tham khảo
- [Tài liệu PostgreSQL 16](https://www.postgresql.org/docs/16/functions-subquery.html)',NULL,NULL,6 FROM lessons l WHERE l.slug='truy-van-con-vo-huong-va-tuong-quan' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lessons (id,category_id,title,slug,description,objectives,practice_topic_code,sort_order) SELECT '0995c7b6-1285-5183-9903-dad5e4e6b147',c.id,'07. EXISTS, NOT EXISTS và kiểm tra tồn tại','exists-not-exists-va-tranh-trung-dong','Kiểm tra có quan hệ mà không nhân bản đầu ra; Diễn đạt không có bằng NOT EXISTS; Kết hợp có ít nhất một với không có bất kỳ.','["Kiểm tra có quan hệ mà không nhân bản đầu ra", "Diễn đạt không có bằng NOT EXISTS", "Kết hợp có ít nhất một với không có bất kỳ"]'::jsonb,'SQL_ADVANCED',7 FROM course_categories c WHERE lower(trim(c.name))=lower('SQL nâng cao') ORDER BY c.created_at LIMIT 1 ON CONFLICT (slug) DO UPDATE SET category_id=EXCLUDED.category_id,title=EXCLUDED.title,description=EXCLUDED.description,objectives=EXCLUDED.objectives,practice_topic_code=EXCLUDED.practice_topic_code,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=1 ORDER BY s.created_at LIMIT 1),'b63fc646-d1ff-5c6e-b182-372894c25a6d'::uuid),l.id,'1. Mục tiêu và chuẩn bị','### Sau bài này bạn có thể
- Kiểm tra có quan hệ mà không nhân bản đầu ra
- Diễn đạt không có bằng NOT EXISTS
- Kết hợp có ít nhất một với không có bất kỳ

### Kiến thức cần có
Đã học các bài SQL cơ bản và hiểu books, publishers, categories, authors, book_authors.

### Cách học
Đọc lý thuyết, dự đoán kết quả SQL trước khi chạy, sau đó đối chiếu giải thích. Các ví dụ truy vấn dùng bộ sách với 15 sách, 5 tác giả, 5 nhà xuất bản, 5 thể loại và 20 liên kết sách–tác giả. Một đầu sách có thể có nhiều tác giả; giá tính theo VND. Không cộng lẫn số đầu sách với số cuốn tồn kho.',NULL,'{"type": "info", "title": "Môi trường ví dụ", "content": "Cú pháp theo PostgreSQL 16. Ví dụ CREATE/UPDATE/DELETE và EXPLAIN được đánh dấu riêng, không phải đáp án cho phòng chỉ cho phép SELECT."}'::jsonb,1 FROM lessons l WHERE l.slug='exists-not-exists-va-tranh-trung-dong' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=2 ORDER BY s.created_at LIMIT 1),'b02af142-a691-5048-833a-ffc68523f819'::uuid),l.id,'2. Khái niệm và cách suy luận','EXISTS trả TRUE khi truy vấn con có ít nhất một dòng; NOT EXISTS trả TRUE khi không có dòng nào. Nội dung SELECT trong thường không quan trọng nên SELECT 1 thể hiện mục đích rõ. Một tác giả có ba sách phù hợp vẫn chỉ có một dòng ở ngoài, vì EXISTS là điều kiện lọc chứ không ghép từng sách vào đầu ra.

Để hỏi tác giả có sách Công nghệ, truy vấn con nối book_authors tới books và categories, rồi liên hệ ba.author_id = a.author_id. Với NOT EXISTS, điều kiện bên trong mô tả điều bị cấm. Muốn tác giả không có sách hết hàng, kiểm tra không tồn tại sách của họ có stock_quantity = 0. Không đổi thành EXISTS sách còn hàng: một tác giả có thể có cả sách còn hàng lẫn hết hàng.

Câu có sách nhưng không có sách hết hàng cần cả EXISTS sách bất kỳ và NOT EXISTS sách hết hàng. Chỉ dùng NOT EXISTS thì tác giả chưa từng viết sách cũng thỏa vì không có phản ví dụ. Đó là điểm quan trọng ở bài mọi/tất cả. Thử dữ liệu biên: không có bản ghi con, một con phù hợp, nhiều con và có cả con vi phạm.',NULL,NULL,2 FROM lessons l WHERE l.slug='exists-not-exists-va-tranh-trung-dong' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=3 ORDER BY s.created_at LIMIT 1),'3c3cace6-64cc-511b-8067-ce5b54371065'::uuid),l.id,'3. Nhà xuất bản không có sách','### Phân tích ví dụ
Chỉ có publisher_id 5, Cánh Diều. Không cần JOIN rồi DISTINCT.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **1**.

| publisher_id | publisher_name |
| --- | --- |
| 5 | Cánh Diều |','SELECT p.publisher_id, p.name AS publisher_name
FROM publishers p
WHERE NOT EXISTS (SELECT 1 FROM books b WHERE b.publisher_id = p.publisher_id)
ORDER BY p.publisher_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,3 FROM lessons l WHERE l.slug='exists-not-exists-va-tranh-trung-dong' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=4 ORDER BY s.created_at LIMIT 1),'2e8a5ccb-9998-54f6-81e4-6e0577197354'::uuid),l.id,'4. Có sách nhưng không có sách hết hàng','### Phân tích ví dụ
Có tác giả 2, 3, 4. Tác giả 1 bị loại vì tham gia sách 5 hết hàng; tác giả 5 cũng có sách hết hàng.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **3**.

| author_id | author_name |
| --- | --- |
| 2 | Trần Thu Hà |
| 3 | Lê Hoàng Nam |
| 4 | Phạm Ngọc Linh |','SELECT a.author_id, a.full_name AS author_name
FROM authors a
WHERE EXISTS (SELECT 1 FROM book_authors ba WHERE ba.author_id = a.author_id)
AND NOT EXISTS (
    SELECT 1 FROM book_authors ba JOIN books b ON b.book_id = ba.book_id
    WHERE ba.author_id = a.author_id AND b.stock_quantity = 0
)
ORDER BY a.author_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,4 FROM lessons l WHERE l.slug='exists-not-exists-va-tranh-trung-dong' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=5 ORDER BY s.created_at LIMIT 1),'1d689491-cfc0-5718-9722-9e07b2470331'::uuid),l.id,'5. Lỗi thường gặp và cách tránh','**1.** EXISTS kiểm tra có dòng, không kiểm tra SELECT bên trong có NULL không.

**2.** Không có bản ghi vi phạm khác với có một bản ghi đạt yêu cầu.

**3.** NOT EXISTS không tự yêu cầu phải có ít nhất một bản ghi con.

### Quy trình kiểm tra
Đọc lại điều kiện tại đúng giá trị ranh giới; kiểm tra NULL, dòng không có đối tác và trường hợp đồng giá. Một câu SQL chạy được chưa chứng minh kết quả đã đúng với yêu cầu.',NULL,'{"type": "warning", "title": "Kiểm tra ý nghĩa trước cú pháp", "content": "EXISTS kiểm tra có dòng, không kiểm tra SELECT bên trong có NULL không."}'::jsonb,5 FROM lessons l WHERE l.slug='exists-not-exists-va-tranh-trung-dong' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=6 ORDER BY s.created_at LIMIT 1),'4ae033c0-91e8-553b-ad3c-cb79ae64e354'::uuid),l.id,'6. Tự kiểm tra và chuyển sang thực hành','**Câu 1: EXISTS(SELECT NULL FROM books) có TRUE không?**

Tự trả lời trước khi đọc. **Giải thích:** Có nếu books có dòng, dù giá trị được chọn là NULL.

**Câu 2: Cần DISTINCT khi dùng EXISTS lọc authors không?**

Tự trả lời trước khi đọc. **Giải thích:** Không nếu nguồn ngoài đã có mỗi author_id duy nhất.

### Thực hành sau khi học
Làm bài khó 4, 5, 10; diễn đạt điều kiện bằng tiếng Việt trước.

[Mở database sách để thực hành](/practice/quan-ly-sach-va-tac-gia)

### Tài liệu tham khảo
- [Tài liệu PostgreSQL 16](https://www.postgresql.org/docs/16/functions-subquery.html)',NULL,NULL,6 FROM lessons l WHERE l.slug='exists-not-exists-va-tranh-trung-dong' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lessons (id,category_id,title,slug,description,objectives,practice_topic_code,sort_order) SELECT '53f97d9b-8af8-5eb5-a79b-c5a7881be4ae',c.id,'08. IN, NOT IN, ANY, ALL và bẫy NULL','in-not-in-any-all-va-bay-null','So sánh một giá trị với một tập; Phân biệt ANY và ALL; Tránh NOT IN khi tập con có thể có NULL.','["So sánh một giá trị với một tập", "Phân biệt ANY và ALL", "Tránh NOT IN khi tập con có thể có NULL"]'::jsonb,'SQL_ADVANCED',8 FROM course_categories c WHERE lower(trim(c.name))=lower('SQL nâng cao') ORDER BY c.created_at LIMIT 1 ON CONFLICT (slug) DO UPDATE SET category_id=EXCLUDED.category_id,title=EXCLUDED.title,description=EXCLUDED.description,objectives=EXCLUDED.objectives,practice_topic_code=EXCLUDED.practice_topic_code,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=1 ORDER BY s.created_at LIMIT 1),'4632cfc8-e9f5-541a-8842-a6a0ebb527dd'::uuid),l.id,'1. Mục tiêu và chuẩn bị','### Sau bài này bạn có thể
- So sánh một giá trị với một tập
- Phân biệt ANY và ALL
- Tránh NOT IN khi tập con có thể có NULL

### Kiến thức cần có
Đã học các bài SQL cơ bản và hiểu books, publishers, categories, authors, book_authors.

### Cách học
Đọc lý thuyết, dự đoán kết quả SQL trước khi chạy, sau đó đối chiếu giải thích. Các ví dụ truy vấn dùng bộ sách với 15 sách, 5 tác giả, 5 nhà xuất bản, 5 thể loại và 20 liên kết sách–tác giả. Một đầu sách có thể có nhiều tác giả; giá tính theo VND. Không cộng lẫn số đầu sách với số cuốn tồn kho.',NULL,'{"type": "info", "title": "Môi trường ví dụ", "content": "Cú pháp theo PostgreSQL 16. Ví dụ CREATE/UPDATE/DELETE và EXPLAIN được đánh dấu riêng, không phải đáp án cho phòng chỉ cho phép SELECT."}'::jsonb,1 FROM lessons l WHERE l.slug='in-not-in-any-all-va-bay-null' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=2 ORDER BY s.created_at LIMIT 1),'7d4f2f57-73ea-5265-85dc-2aca182a26b1'::uuid),l.id,'2. Khái niệm và cách suy luận','IN kiểm tra bằng ít nhất một giá trị của tập, tương đương = ANY trong tình huống so sánh thông thường. ANY yêu cầu ít nhất một phép so sánh đúng; ALL yêu cầu mọi phép so sánh đúng. price > ALL(tập_giá) đòi lớn hơn tất cả mức giá, không chỉ vượt một mức bất kỳ.

Nếu không khớp giá trị nào nhưng tập IN chứa NULL, kết quả có thể UNKNOWN. Với NOT IN, NULL có thể làm cả những giá trị ngoài tập cũng không được giữ bởi WHERE. Khi hỏi không có bản ghi liên quan, NOT EXISTS thường diễn đạt rõ hơn và tránh bẫy này. Không loại NULL theo thói quen nếu nó mang ý nghĩa nghiệp vụ cần xét.

Tập rỗng là trường hợp riêng: ANY trên tập rỗng là FALSE, ALL trên tập rỗng là TRUE. Vì vậy > ALL không luôn thay thế bằng > MAX khi tập rỗng hoặc có NULL. Với price NOT NULL và tập chắc chắn có dòng, hai cách có thể cho cùng kết quả ở bài cụ thể. Nếu đối tượng so sánh phải tồn tại, bổ sung EXISTS hoặc bảo đảm bằng ràng buộc phù hợp.',NULL,NULL,2 FROM lessons l WHERE l.slug='in-not-in-any-all-va-bay-null' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=3 ORDER BY s.created_at LIMIT 1),'ca682281-6a20-5ff2-a0ea-5a25d9a99eb3'::uuid),l.id,'3. Đắt hơn mọi sách Công nghệ','### Phân tích ví dụ
Giá cao nhất Công nghệ là 145000. Có sách 4, 9, 14 giá 180000. > ANY trả tập rộng hơn vì chỉ cần vượt một mức giá.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **3**.

| book_id | title | price |
| --- | --- | --- |
| 4 | Mùa hạ bên ô cửa | 180000.0 |
| 9 | Nghệ thuật làm việc nhóm | 180000.0 |
| 14 | Khu vườn biết nói | 180000.0 |','SELECT b.book_id, b.title, b.price
FROM books b
WHERE b.price > ALL (
    SELECT x.price FROM books x JOIN categories c ON c.category_id = x.category_id
    WHERE c.name = ''Công nghệ''
)
ORDER BY b.book_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,3 FROM lessons l WHERE l.slug='in-not-in-any-all-va-bay-null' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=4 ORDER BY s.created_at LIMIT 1),'45a1b390-f40b-5734-90f2-d0ae58bd9de7'::uuid),l.id,'4. Quan sát bẫy NULL','### Phân tích ví dụ
Cột đầu NULL, cột sau TRUE. Dòng NULL không làm v.x = 2 đúng, nên NOT EXISTS xác nhận không có dòng khớp.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **1**.

| not_in_result | not_exists_result |
| --- | --- |
| NULL | TRUE |','SELECT 2 NOT IN (1, NULL::integer) AS not_in_result,
       NOT EXISTS (
           SELECT 1 FROM (VALUES (1), (NULL::integer)) v(x) WHERE v.x = 2
       ) AS not_exists_result;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,4 FROM lessons l WHERE l.slug='in-not-in-any-all-va-bay-null' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=5 ORDER BY s.created_at LIMIT 1),'cb8f7069-52dd-5d0b-9ba5-d140fe821466'::uuid),l.id,'5. Lỗi thường gặp và cách tránh','**1.** NOT IN và NOT EXISTS không luôn tương đương khi có NULL.

**2.** ANY nghĩa ít nhất một, không phải trung bình hoặc phần lớn.

**3.** ALL trên tập rỗng có thể đúng; xem có đúng nghiệp vụ không.

### Quy trình kiểm tra
Đọc lại điều kiện tại đúng giá trị ranh giới; kiểm tra NULL, dòng không có đối tác và trường hợp đồng giá. Một câu SQL chạy được chưa chứng minh kết quả đã đúng với yêu cầu.',NULL,'{"type": "warning", "title": "Kiểm tra ý nghĩa trước cú pháp", "content": "NOT IN và NOT EXISTS không luôn tương đương khi có NULL."}'::jsonb,5 FROM lessons l WHERE l.slug='in-not-in-any-all-va-bay-null' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=6 ORDER BY s.created_at LIMIT 1),'cc0d6b27-bb1a-58a8-8df6-4cbbd6ae23eb'::uuid),l.id,'6. Tự kiểm tra và chuyển sang thực hành','**Câu 1: 2 > ALL với tập rỗng cho gì?**

Tự trả lời trước khi đọc. **Giải thích:** TRUE, vì không có phần tử làm điều kiện sai.

**Câu 2: Tập con của IN có mã lặp thì dòng ngoài bị lặp không?**

Tự trả lời trước khi đọc. **Giải thích:** Không; IN là điều kiện lọc, không phải phép nối các cặp.

### Thực hành sau khi học
Làm bài khó 6–7 và thử thay ALL bằng ANY để giải thích khác nhau.

[Mở database sách để thực hành](/practice/quan-ly-sach-va-tac-gia)

### Tài liệu tham khảo
- [Tài liệu PostgreSQL 16](https://www.postgresql.org/docs/16/functions-subquery.html)',NULL,NULL,6 FROM lessons l WHERE l.slug='in-not-in-any-all-va-bay-null' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lessons (id,category_id,title,slug,description,objectives,practice_topic_code,sort_order) SELECT 'c0422a7a-bad6-536c-9682-b7325e7c0a3f',c.id,'09. Bảng dẫn xuất và CTE để chia nhỏ truy vấn','bang-dan-xuat-va-cte-de-chia-nho-truy-van','Tổng hợp qua hai cấp bằng truy vấn trong FROM; Đặt tên bước bằng WITH; Không mặc định CTE được lưu hoặc luôn tăng tốc.','["Tổng hợp qua hai cấp bằng truy vấn trong FROM", "Đặt tên bước bằng WITH", "Không mặc định CTE được lưu hoặc luôn tăng tốc"]'::jsonb,'SQL_ADVANCED',9 FROM course_categories c WHERE lower(trim(c.name))=lower('SQL nâng cao') ORDER BY c.created_at LIMIT 1 ON CONFLICT (slug) DO UPDATE SET category_id=EXCLUDED.category_id,title=EXCLUDED.title,description=EXCLUDED.description,objectives=EXCLUDED.objectives,practice_topic_code=EXCLUDED.practice_topic_code,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=1 ORDER BY s.created_at LIMIT 1),'99bee550-46d8-522c-a86b-e2273dff1556'::uuid),l.id,'1. Mục tiêu và chuẩn bị','### Sau bài này bạn có thể
- Tổng hợp qua hai cấp bằng truy vấn trong FROM
- Đặt tên bước bằng WITH
- Không mặc định CTE được lưu hoặc luôn tăng tốc

### Kiến thức cần có
Đã học các bài SQL cơ bản và hiểu books, publishers, categories, authors, book_authors.

### Cách học
Đọc lý thuyết, dự đoán kết quả SQL trước khi chạy, sau đó đối chiếu giải thích. Các ví dụ truy vấn dùng bộ sách với 15 sách, 5 tác giả, 5 nhà xuất bản, 5 thể loại và 20 liên kết sách–tác giả. Một đầu sách có thể có nhiều tác giả; giá tính theo VND. Không cộng lẫn số đầu sách với số cuốn tồn kho.',NULL,'{"type": "info", "title": "Môi trường ví dụ", "content": "Cú pháp theo PostgreSQL 16. Ví dụ CREATE/UPDATE/DELETE và EXPLAIN được đánh dấu riêng, không phải đáp án cho phòng chỉ cho phép SELECT."}'::jsonb,1 FROM lessons l WHERE l.slug='bang-dan-xuat-va-cte-de-chia-nho-truy-van' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=2 ORDER BY s.created_at LIMIT 1),'18744351-507b-59a2-a2aa-3393240e6c6d'::uuid),l.id,'2. Khái niệm và cách suy luận','Bảng dẫn xuất là kết quả truy vấn con trong FROM với một bí danh. Nó hữu ích khi kết quả một lần tổng hợp là đầu vào cho lần khác. Muốn trung bình số sách các nhà xuất bản, trước hết đếm theo nhà xuất bản rồi AVG các số đếm. Không lồng AVG(COUNT(*)) tùy ý trong cùng cấp GROUP BY để thay hai bước này.

CTE dùng WITH đặt tên một bước trong phạm vi một câu lệnh. Các bước publisher_counts và average_count làm rõ ý nghĩa từng cấp. CTE không tạo bảng vĩnh viễn. PostgreSQL có thể gộp hoặc materialize CTE tùy trường hợp, nên WITH không phải mẹo tăng tốc mặc định.

Ở bước đếm, dùng publishers LEFT JOIN books để đưa nhà xuất bản 0 sách vào mẫu số trung bình. Có năm đơn vị với số sách 4,4,4,3,0 nên trung bình là 3, không phải 3.75 nếu chỉ tính đơn vị đã có sách. Mỗi bước nên ghi rõ một dòng đại diện cho đối tượng gì. Đổi JOIN có thể làm sai mẫu số mà câu lệnh vẫn hợp lệ.',NULL,NULL,2 FROM lessons l WHERE l.slug='bang-dan-xuat-va-cte-de-chia-nho-truy-van' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=3 ORDER BY s.created_at LIMIT 1),'47c5a1f9-635c-5eee-986a-15bcb5c14b3e'::uuid),l.id,'3. Trung bình của các số đếm','### Phân tích ví dụ
Kết quả 3.00. Bảng dẫn xuất có 5 dòng; AVG lấy trung bình năm số đếm, không phải 15 dòng sách.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **1**.

| average_book_count |
| --- |
| 3.0 |','SELECT ROUND(AVG(t.book_count), 2) AS average_book_count
FROM (
    SELECT p.publisher_id, COUNT(b.book_id) AS book_count
    FROM publishers p LEFT JOIN books b ON b.publisher_id = p.publisher_id
    GROUP BY p.publisher_id
) AS t;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,3 FROM lessons l WHERE l.slug='bang-dan-xuat-va-cte-de-chia-nho-truy-van' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=4 ORDER BY s.created_at LIMIT 1),'8d5008c6-2870-5264-b84e-7d5bd5facd20'::uuid),l.id,'4. Chia báo cáo thành các bước','### Phân tích ví dụ
Có ba nhà xuất bản mã 1, 2, 3 với 4 sách. Hai CTE chỉ tồn tại trong câu lệnh này.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **3**.

| publisher_id | publisher_name | book_count |
| --- | --- | --- |
| 1 | Tri Thức Mới | 4 |
| 2 | Ánh Dương | 4 |
| 3 | Hành Trang | 4 |','WITH publisher_counts AS (
    SELECT p.publisher_id, p.name AS publisher_name, COUNT(b.book_id) AS book_count
    FROM publishers p LEFT JOIN books b ON b.publisher_id = p.publisher_id
    GROUP BY p.publisher_id, p.name
), average_count AS (
    SELECT AVG(book_count) AS value FROM publisher_counts
)
SELECT publisher_id, publisher_name, book_count FROM publisher_counts
WHERE book_count > (SELECT value FROM average_count)
ORDER BY publisher_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,4 FROM lessons l WHERE l.slug='bang-dan-xuat-va-cte-de-chia-nho-truy-van' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=5 ORDER BY s.created_at LIMIT 1),'c7017a52-64ce-51ea-913d-d2f4bd04111e'::uuid),l.id,'5. Lỗi thường gặp và cách tránh','**1.** Không tính trung bình trên các nhóm bị lọc nếu đề yêu cầu toàn bộ nhóm.

**2.** CTE không lưu dữ liệu cho câu lệnh tiếp theo.

**3.** Bảng dẫn xuất cần tên rõ để phân biệt các cấp.

### Quy trình kiểm tra
Đọc lại điều kiện tại đúng giá trị ranh giới; kiểm tra NULL, dòng không có đối tác và trường hợp đồng giá. Một câu SQL chạy được chưa chứng minh kết quả đã đúng với yêu cầu.',NULL,'{"type": "warning", "title": "Kiểm tra ý nghĩa trước cú pháp", "content": "Không tính trung bình trên các nhóm bị lọc nếu đề yêu cầu toàn bộ nhóm."}'::jsonb,5 FROM lessons l WHERE l.slug='bang-dan-xuat-va-cte-de-chia-nho-truy-van' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=6 ORDER BY s.created_at LIMIT 1),'2b960792-43c6-517a-8bc0-3299c5967707'::uuid),l.id,'6. Tự kiểm tra và chuyển sang thực hành','**Câu 1: Vì sao trung bình 3 thay vì 3.75?**

Tự trả lời trước khi đọc. **Giải thích:** Cánh Diều có 0 sách vẫn nằm trong mẫu 5 đơn vị.

**Câu 2: WITH luôn nhanh hơn truy vấn con không?**

Tự trả lời trước khi đọc. **Giải thích:** Không; cần đo kế hoạch trên dữ liệu thực tế.

### Thực hành sau khi học
Học trước bài khó 9; có thể dùng phiên bản bảng dẫn xuất nếu phòng chạy yêu cầu SELECT đầu câu.

[Mở database sách để thực hành](/practice/quan-ly-sach-va-tac-gia)

### Tài liệu tham khảo
- [Tài liệu PostgreSQL 16](https://www.postgresql.org/docs/16/queries-with.html)',NULL,NULL,6 FROM lessons l WHERE l.slug='bang-dan-xuat-va-cte-de-chia-nho-truy-van' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lessons (id,category_id,title,slug,description,objectives,practice_topic_code,sort_order) SELECT '516d7d8b-5442-58a6-b619-a14ff8b84467',c.id,'10. UNION, INTERSECT, EXCEPT và kết hợp tập','union-intersect-except-va-ket-hop-tap','Ghép dọc các kết quả tương thích; Phân biệt UNION với UNION ALL; Tìm giao và phần khác nhau giữa hai tập.','["Ghép dọc các kết quả tương thích", "Phân biệt UNION với UNION ALL", "Tìm giao và phần khác nhau giữa hai tập"]'::jsonb,'SQL_ADVANCED',10 FROM course_categories c WHERE lower(trim(c.name))=lower('SQL nâng cao') ORDER BY c.created_at LIMIT 1 ON CONFLICT (slug) DO UPDATE SET category_id=EXCLUDED.category_id,title=EXCLUDED.title,description=EXCLUDED.description,objectives=EXCLUDED.objectives,practice_topic_code=EXCLUDED.practice_topic_code,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=1 ORDER BY s.created_at LIMIT 1),'dc0bdcd1-578b-503b-9437-08c7e4bfa5bc'::uuid),l.id,'1. Mục tiêu và chuẩn bị','### Sau bài này bạn có thể
- Ghép dọc các kết quả tương thích
- Phân biệt UNION với UNION ALL
- Tìm giao và phần khác nhau giữa hai tập

### Kiến thức cần có
Đã học các bài SQL cơ bản và hiểu books, publishers, categories, authors, book_authors.

### Cách học
Đọc lý thuyết, dự đoán kết quả SQL trước khi chạy, sau đó đối chiếu giải thích. Các ví dụ truy vấn dùng bộ sách với 15 sách, 5 tác giả, 5 nhà xuất bản, 5 thể loại và 20 liên kết sách–tác giả. Một đầu sách có thể có nhiều tác giả; giá tính theo VND. Không cộng lẫn số đầu sách với số cuốn tồn kho.',NULL,'{"type": "info", "title": "Môi trường ví dụ", "content": "Cú pháp theo PostgreSQL 16. Ví dụ CREATE/UPDATE/DELETE và EXPLAIN được đánh dấu riêng, không phải đáp án cho phòng chỉ cho phép SELECT."}'::jsonb,1 FROM lessons l WHERE l.slug='union-intersect-except-va-ket-hop-tap' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=2 ORDER BY s.created_at LIMIT 1),'9cf3d161-ba50-5406-82fe-bdabc252bbb7'::uuid),l.id,'2. Khái niệm và cách suy luận','JOIN ghép cột theo quan hệ, còn phép tập kết hợp kết quả theo chiều dọc. Các nhánh phải có cùng số cột và kiểu tương thích theo từng vị trí. Tên đầu ra thường lấy từ nhánh đầu nên đặt bí danh rõ ngay ở đó. Tên giống nhau không bù cho việc kiểu hoặc vị trí không tương thích.

UNION loại dòng trùng; UNION ALL giữ mọi dòng và tránh công đoạn loại trùng khi không cần. INTERSECT lấy dòng ở cả hai tập; EXCEPT lấy dòng ở tập trái nhưng không ở tập phải. Mặc định việc loại trùng xét toàn bộ tổ hợp cột, giống DISTINCT. Thêm cột nhãn nguồn khác nhau làm hai dòng cùng mã không còn trùng toàn bộ.

ORDER BY cuối áp dụng cho toàn kết quả. Nếu mỗi nhánh cần LIMIT hoặc ORDER BY riêng, bọc phạm vi bằng ngoặc. Với nhiều phép tập, dùng ngoặc để biểu đạt ý định thay vì chỉ dựa vào thứ tự ưu tiên. Không dùng UNION thay JOIN khi cần sách đi cùng tên nhà xuất bản: đó là hai loại câu hỏi khác nhau. Khi cần xét số lần lặp, tìm hiểu biến thể ALL thay vì chỉ so sánh tập giá trị.',NULL,NULL,2 FROM lessons l WHERE l.slug='union-intersect-except-va-ket-hop-tap' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=3 ORDER BY s.created_at LIMIT 1),'b43fc978-8196-5fc4-ace4-fafa272a80d8'::uuid),l.id,'3. Gộp hai danh sách mã','### Phân tích ví dụ
Có 8 mã khác nhau. Sách 3 thỏa cả hai nhánh nhưng UNION giữ một dòng; UNION ALL giữ hai lần.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **8**.

| book_id |
| --- |
| 1 |
| 2 |
| 3 |
| 4 |
| 8 |

Bảng trên minh họa 5 dòng đầu; truy vấn trả đầy đủ 8 dòng.','SELECT book_id FROM books WHERE category_id = 1
UNION
SELECT book_id FROM books WHERE price >= 145000
ORDER BY book_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,3 FROM lessons l WHERE l.slug='union-intersect-except-va-ket-hop-tap' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=4 ORDER BY s.created_at LIMIT 1),'f4df14a7-08d0-5c6f-9bd4-123c67ca9e58'::uuid),l.id,'4. Nhà xuất bản chưa có sách bằng EXCEPT','### Phân tích ví dụ
Chỉ có mã 5. NOT EXISTS thường thuận tiện hơn nếu cần nhiều cột mô tả từ publishers.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **1**.

| publisher_id |
| --- |
| 5 |','SELECT publisher_id FROM publishers
EXCEPT
SELECT publisher_id FROM books
ORDER BY publisher_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,4 FROM lessons l WHERE l.slug='union-intersect-except-va-ket-hop-tap' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=5 ORDER BY s.created_at LIMIT 1),'8b74f53c-bacc-530c-b145-27bc16796a04'::uuid),l.id,'5. Lỗi thường gặp và cách tránh','**1.** Số cột và kiểu theo vị trí phải tương thích.

**2.** UNION ALL giữ trùng; chọn theo yêu cầu thay vì thói quen.

**3.** ORDER BY trong một nhánh không bảo đảm thứ tự kết quả hợp nhất.

### Quy trình kiểm tra
Đọc lại điều kiện tại đúng giá trị ranh giới; kiểm tra NULL, dòng không có đối tác và trường hợp đồng giá. Một câu SQL chạy được chưa chứng minh kết quả đã đúng với yêu cầu.',NULL,'{"type": "warning", "title": "Kiểm tra ý nghĩa trước cú pháp", "content": "Số cột và kiểu theo vị trí phải tương thích."}'::jsonb,5 FROM lessons l WHERE l.slug='union-intersect-except-va-ket-hop-tap' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=6 ORDER BY s.created_at LIMIT 1),'06453119-336f-511f-94ef-ac7a69c23052'::uuid),l.id,'6. Tự kiểm tra và chuyển sang thực hành','**Câu 1: UNION giống JOIN không?**

Tự trả lời trước khi đọc. **Giải thích:** Không: UNION ghép dòng của các kết quả; JOIN ghép cột theo các cặp liên quan.

**Câu 2: Giao sách Công nghệ và sách giá từ 145000 là mã nào?**

Tự trả lời trước khi đọc. **Giải thích:** Mã 3, sách Công nghệ duy nhất đạt ngưỡng.

### Thực hành sau khi học
EXCEPT hai chiều giúp tìm dòng thiếu/thừa nhưng mặc định không kiểm tra số lần lặp.

[Mở database sách để thực hành](/practice/quan-ly-sach-va-tac-gia)

### Tài liệu tham khảo
- [Tài liệu PostgreSQL 16](https://www.postgresql.org/docs/16/queries-table-expressions.html)',NULL,NULL,6 FROM lessons l WHERE l.slug='union-intersect-except-va-ket-hop-tap' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lessons (id,category_id,title,slug,description,objectives,practice_topic_code,sort_order) SELECT '792b7072-d7ec-549a-a80b-a47e4d599840',c.id,'11. Hàm cửa sổ: ROW_NUMBER, RANK và DENSE_RANK','window-functions-row-number-rank-dense-rank','Giữ từng dòng khi tính theo nhóm; Phân biệt ba cách xếp hạng; Lọc kết quả cửa sổ ở cấp ngoài.','["Giữ từng dòng khi tính theo nhóm", "Phân biệt ba cách xếp hạng", "Lọc kết quả cửa sổ ở cấp ngoài"]'::jsonb,'SQL_ADVANCED',11 FROM course_categories c WHERE lower(trim(c.name))=lower('SQL nâng cao') ORDER BY c.created_at LIMIT 1 ON CONFLICT (slug) DO UPDATE SET category_id=EXCLUDED.category_id,title=EXCLUDED.title,description=EXCLUDED.description,objectives=EXCLUDED.objectives,practice_topic_code=EXCLUDED.practice_topic_code,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=1 ORDER BY s.created_at LIMIT 1),'5b77f7a2-2e6e-59c8-b05a-b166136f5799'::uuid),l.id,'1. Mục tiêu và chuẩn bị','### Sau bài này bạn có thể
- Giữ từng dòng khi tính theo nhóm
- Phân biệt ba cách xếp hạng
- Lọc kết quả cửa sổ ở cấp ngoài

### Kiến thức cần có
Đã học các bài SQL cơ bản và hiểu books, publishers, categories, authors, book_authors.

### Cách học
Đọc lý thuyết, dự đoán kết quả SQL trước khi chạy, sau đó đối chiếu giải thích. Các ví dụ truy vấn dùng bộ sách với 15 sách, 5 tác giả, 5 nhà xuất bản, 5 thể loại và 20 liên kết sách–tác giả. Một đầu sách có thể có nhiều tác giả; giá tính theo VND. Không cộng lẫn số đầu sách với số cuốn tồn kho.',NULL,'{"type": "info", "title": "Môi trường ví dụ", "content": "Cú pháp theo PostgreSQL 16. Ví dụ CREATE/UPDATE/DELETE và EXPLAIN được đánh dấu riêng, không phải đáp án cho phòng chỉ cho phép SELECT."}'::jsonb,1 FROM lessons l WHERE l.slug='window-functions-row-number-rank-dense-rank' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=2 ORDER BY s.created_at LIMIT 1),'bef3ed4c-fdb1-5a37-92f2-ac41fc5cfffa'::uuid),l.id,'2. Khái niệm và cách suy luận','Hàm cửa sổ tính trên các dòng liên quan nhưng giữ từng dòng đầu vào. GROUP BY category_id làm mỗi thể loại còn một dòng; AVG(price) OVER (PARTITION BY category_id) giữ từng sách và gắn thêm trung bình thể loại. PARTITION BY chia vùng tính; ORDER BY trong OVER phục vụ phép tính, không thay ORDER BY cuối.

ROW_NUMBER đánh số liên tiếp từng dòng. RANK cho cùng hạng với các dòng đồng giá và để khoảng trống sau nhóm đồng hạng. DENSE_RANK cũng cho cùng hạng nhưng không để khoảng trống. Với giá 180000,180000,145000: ROW_NUMBER có thể 1,2,3; RANK là 1,1,3; DENSE_RANK là 1,1,2. ROW_NUMBER cần khóa phụ duy nhất để chọn dòng ổn định.

Muốn giữ mọi sách đắt nhất mỗi thể loại, tính DENSE_RANK theo price giảm rồi lọc hạng 1 ở cấp ngoài. Không thêm book_id vào thứ tự DENSE_RANK nếu muốn đồng hạng theo giá: book_id khác nhau sẽ phá nhóm đồng hạng. Hàm cửa sổ không dùng trực tiếp trong WHERE cùng cấp, nên cần CTE hoặc bảng dẫn xuất. Chọn hạng theo dòng hay theo giá là quyết định nghiệp vụ, không phải chỉ khác tên hàm.',NULL,NULL,2 FROM lessons l WHERE l.slug='window-functions-row-number-rank-dense-rank' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=3 ORDER BY s.created_at LIMIT 1),'216783fb-8ddf-58a3-9a75-98feaafec0e9'::uuid),l.id,'3. So sánh ba cách xếp hạng','### Phân tích ví dụ
Ba sách giá 180000 cùng RANK = 1 và DENSE_RANK = 1. Mức giá 145000 bắt đầu RANK = 4 nhưng DENSE_RANK = 2.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **15**.

| book_id | price | row_no | price_rank | dense_price_rank |
| --- | --- | --- | --- | --- |
| 4 | 180000.0 | 1 | 1 | 1 |
| 9 | 180000.0 | 2 | 1 | 1 |
| 14 | 180000.0 | 3 | 1 | 1 |
| 3 | 145000.0 | 4 | 4 | 2 |
| 8 | 145000.0 | 5 | 4 | 2 |

Bảng trên minh họa 5 dòng đầu; truy vấn trả đầy đủ 15 dòng.','SELECT book_id, price,
       ROW_NUMBER() OVER (ORDER BY price DESC, book_id) AS row_no,
       RANK() OVER (ORDER BY price DESC) AS price_rank,
       DENSE_RANK() OVER (ORDER BY price DESC) AS dense_price_rank
FROM books ORDER BY price DESC, book_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,3 FROM lessons l WHERE l.slug='window-functions-row-number-rank-dense-rank' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=4 ORDER BY s.created_at LIMIT 1),'1582e0e9-310c-547e-a0aa-541c97ec5435'::uuid),l.id,'4. Mọi sách đắt nhất từng thể loại','### Phân tích ví dụ
Có 5 sách mã 3, 4, 9, 12, 14. Nếu sau này có hai sách đồng giá cao nhất một thể loại, cả hai được giữ.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **5**.

| book_id | title | category_id | price |
| --- | --- | --- | --- |
| 3 | Python cho người mới bắt đầu | 1 | 145000.0 |
| 4 | Mùa hạ bên ô cửa | 2 | 180000.0 |
| 9 | Nghệ thuật làm việc nhóm | 3 | 180000.0 |
| 12 | Hành tinh xanh của chúng ta | 4 | 120000.0 |
| 14 | Khu vườn biết nói | 5 | 180000.0 |','SELECT book_id, title, category_id, price
FROM (
    SELECT b.*, DENSE_RANK() OVER (PARTITION BY category_id ORDER BY price DESC) AS price_rank
    FROM books b
) ranked
WHERE price_rank = 1
ORDER BY category_id, book_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,4 FROM lessons l WHERE l.slug='window-functions-row-number-rank-dense-rank' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=5 ORDER BY s.created_at LIMIT 1),'3a785454-b50e-5805-802c-c3fc1fe27cb3'::uuid),l.id,'5. Lỗi thường gặp và cách tránh','**1.** ORDER BY trong OVER không bảo đảm thứ tự hiển thị cuối.

**2.** Không lọc ROW_NUMBER trực tiếp ở WHERE cùng cấp.

**3.** DENSE_RANK theo price, book_id không còn xếp hạng chỉ theo giá.

### Quy trình kiểm tra
Đọc lại điều kiện tại đúng giá trị ranh giới; kiểm tra NULL, dòng không có đối tác và trường hợp đồng giá. Một câu SQL chạy được chưa chứng minh kết quả đã đúng với yêu cầu.',NULL,'{"type": "warning", "title": "Kiểm tra ý nghĩa trước cú pháp", "content": "ORDER BY trong OVER không bảo đảm thứ tự hiển thị cuối."}'::jsonb,5 FROM lessons l WHERE l.slug='window-functions-row-number-rank-dense-rank' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=6 ORDER BY s.created_at LIMIT 1),'201f665a-4fea-588e-8395-956f41bc3c19'::uuid),l.id,'6. Tự kiểm tra và chuyển sang thực hành','**Câu 1: Lấy đúng một sách mỗi thể loại dùng gì?**

Tự trả lời trước khi đọc. **Giải thích:** ROW_NUMBER theo price DESC, book_id rồi lọc row_no = 1.

**Câu 2: Lấy mọi sách ở hai mức giá cao nhất dùng gì?**

Tự trả lời trước khi đọc. **Giải thích:** DENSE_RANK theo price DESC rồi lọc hạng <= 2 ở cấp ngoài.

### Thực hành sau khi học
So sánh với cách giải truy vấn con ở bài khó 3 và 8.

[Mở database sách để thực hành](/practice/quan-ly-sach-va-tac-gia)

### Tài liệu tham khảo
- [Tài liệu PostgreSQL 16](https://www.postgresql.org/docs/16/tutorial-window.html)',NULL,NULL,6 FROM lessons l WHERE l.slug='window-functions-row-number-rank-dense-rank' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lessons (id,category_id,title,slug,description,objectives,practice_topic_code,sort_order) SELECT '89c5e301-ea1c-5ea5-b0c3-008f1e23a368',c.id,'12. Khung cửa sổ, tổng lũy kế, LAG và LEAD','window-frames-luy-ke-lag-lead','Chỉ định ROWS khi tính lũy kế; Đọc dòng trước hoặc sau bằng hàm cửa sổ; Phân biệt thứ tự kỹ thuật và thời gian thực.','["Chỉ định ROWS khi tính lũy kế", "Đọc dòng trước hoặc sau bằng hàm cửa sổ", "Phân biệt thứ tự kỹ thuật và thời gian thực"]'::jsonb,'SQL_ADVANCED',12 FROM course_categories c WHERE lower(trim(c.name))=lower('SQL nâng cao') ORDER BY c.created_at LIMIT 1 ON CONFLICT (slug) DO UPDATE SET category_id=EXCLUDED.category_id,title=EXCLUDED.title,description=EXCLUDED.description,objectives=EXCLUDED.objectives,practice_topic_code=EXCLUDED.practice_topic_code,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=1 ORDER BY s.created_at LIMIT 1),'37577551-13fc-52cc-91cd-ecaf23b3b199'::uuid),l.id,'1. Mục tiêu và chuẩn bị','### Sau bài này bạn có thể
- Chỉ định ROWS khi tính lũy kế
- Đọc dòng trước hoặc sau bằng hàm cửa sổ
- Phân biệt thứ tự kỹ thuật và thời gian thực

### Kiến thức cần có
Đã học các bài SQL cơ bản và hiểu books, publishers, categories, authors, book_authors.

### Cách học
Đọc lý thuyết, dự đoán kết quả SQL trước khi chạy, sau đó đối chiếu giải thích. Các ví dụ truy vấn dùng bộ sách với 15 sách, 5 tác giả, 5 nhà xuất bản, 5 thể loại và 20 liên kết sách–tác giả. Một đầu sách có thể có nhiều tác giả; giá tính theo VND. Không cộng lẫn số đầu sách với số cuốn tồn kho.',NULL,'{"type": "info", "title": "Môi trường ví dụ", "content": "Cú pháp theo PostgreSQL 16. Ví dụ CREATE/UPDATE/DELETE và EXPLAIN được đánh dấu riêng, không phải đáp án cho phòng chỉ cho phép SELECT."}'::jsonb,1 FROM lessons l WHERE l.slug='window-frames-luy-ke-lag-lead' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=2 ORDER BY s.created_at LIMIT 1),'e66be6a3-a9de-5219-b20f-10e3bb94c612'::uuid),l.id,'2. Khái niệm và cách suy luận','Với hàm tổng hợp dùng OVER có ORDER BY, khung cửa sổ quyết định dòng nào được tính cho mỗi dòng hiện tại. Lũy kế từng sách theo book_id nên viết ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW để xác định từ đầu phân vùng đến dòng hiện tại. Tránh phụ thuộc khung mặc định khi khóa sắp xếp có giá trị bằng nhau.

PARTITION BY làm phép tính bắt đầu lại theo nhóm, ví dụ lũy kế riêng mỗi thể loại. Không có PARTITION BY thì toàn kết quả là một phân vùng. book_id ở đây là thứ tự kỹ thuật: books không có bảng giao dịch nên không thể suy ra lịch sử nhập xuất kho từ các dòng tồn kho hiện tại. Diễn giải đúng ý nghĩa số liệu quan trọng không kém cú pháp.

LAG lấy giá trị dòng trước; LEAD lấy dòng sau trong phân vùng đã sắp xếp. Dòng đầu không có giá trị trước nên LAG thường trả NULL nếu không có mặc định khác. So sánh giá sách theo năm cần khóa phụ book_id để xử lý cùng năm. Phép tính cửa sổ chỉ nhìn các dòng còn lại sau các bước lọc/nhóm của truy vấn; đổi WHERE có thể đổi cả chuỗi so sánh, không chỉ cắt bớt đầu ra.',NULL,NULL,2 FROM lessons l WHERE l.slug='window-frames-luy-ke-lag-lead' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=3 ORDER BY s.created_at LIMIT 1),'38a085b8-3c70-5b77-82c3-587763f8e102'::uuid),l.id,'3. Tồn kho lũy kế theo mã','### Phân tích ví dụ
Ba giá trị đầu là 3, 11, 26; dòng cuối là 150. Đây là cộng dồn theo mã, không phải biến động kho theo ngày.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **15**.

| book_id | title | stock_quantity | running_stock |
| --- | --- | --- | --- |
| 1 | SQL từ cơ bản đến thực hành | 3 | 3 |
| 2 | Thiết kế cơ sở dữ liệu | 8 | 11 |
| 3 | Python cho người mới bắt đầu | 15 | 26 |
| 4 | Mùa hạ bên ô cửa | 24 | 50 |
| 5 | Những con đường cũ | 0 | 50 |

Bảng trên minh họa 5 dòng đầu; truy vấn trả đầy đủ 15 dòng.','SELECT book_id, title, stock_quantity,
       SUM(stock_quantity) OVER (
           ORDER BY book_id ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
       ) AS running_stock
FROM books ORDER BY book_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,3 FROM lessons l WHERE l.slug='window-frames-luy-ke-lag-lead' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=4 ORDER BY s.created_at LIMIT 1),'0ae1a57d-3ccd-504c-9bb5-d79e5d8e1e7c'::uuid),l.id,'4. So với sách đứng trước trong thể loại','### Phân tích ví dụ
Trong Công nghệ, dòng đầu có previous_price NULL; hai dòng sau lần lượt 99000 và 120000. Thể loại tiếp theo bắt đầu lại chuỗi.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **15**.

| book_id | category_id | published_year | price | previous_price |
| --- | --- | --- | --- | --- |
| 1 | 1 | 2019 | 99000.0 | NULL |
| 2 | 1 | 2020 | 120000.0 | 99000.0 |
| 3 | 1 | 2021 | 145000.0 | 120000.0 |
| 4 | 2 | 2022 | 180000.0 | NULL |
| 5 | 2 | 2023 | 75000.0 | 180000.0 |

Bảng trên minh họa 5 dòng đầu; truy vấn trả đầy đủ 15 dòng.','SELECT book_id, category_id, published_year, price,
       LAG(price) OVER (PARTITION BY category_id ORDER BY published_year, book_id) AS previous_price
FROM books ORDER BY category_id, published_year, book_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,4 FROM lessons l WHERE l.slug='window-frames-luy-ke-lag-lead' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=5 ORDER BY s.created_at LIMIT 1),'e20d0d5a-c3d2-5ef3-bf90-39f1706f314d'::uuid),l.id,'5. Lỗi thường gặp và cách tránh','**1.** Không gọi lũy kế book_id là tổng theo thời gian nếu thiếu dữ liệu lịch sử.

**2.** Khóa sắp xếp trùng ảnh hưởng khung mặc định; hãy viết khung rõ.

**3.** LAG cần thứ tự có ý nghĩa và khóa phụ khi hòa.

### Quy trình kiểm tra
Đọc lại điều kiện tại đúng giá trị ranh giới; kiểm tra NULL, dòng không có đối tác và trường hợp đồng giá. Một câu SQL chạy được chưa chứng minh kết quả đã đúng với yêu cầu.',NULL,'{"type": "warning", "title": "Kiểm tra ý nghĩa trước cú pháp", "content": "Không gọi lũy kế book_id là tổng theo thời gian nếu thiếu dữ liệu lịch sử."}'::jsonb,5 FROM lessons l WHERE l.slug='window-frames-luy-ke-lag-lead' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=6 ORDER BY s.created_at LIMIT 1),'56c1aa33-e403-545c-827c-2ecf65829913'::uuid),l.id,'6. Tự kiểm tra và chuyển sang thực hành','**Câu 1: Cộng lũy kế riêng mỗi nhà xuất bản thêm gì?**

Tự trả lời trước khi đọc. **Giải thích:** PARTITION BY publisher_id và một ORDER BY ổn định trong OVER.

**Câu 2: Vì sao đầu mỗi phân vùng LAG trả NULL?**

Tự trả lời trước khi đọc. **Giải thích:** Không có dòng đứng trước trong chính phân vùng đó.

### Thực hành sau khi học
Mở rộng từ báo cáo theo nhóm sang giữ từng sách kèm thông tin phân tích.

[Mở database sách để thực hành](/practice/quan-ly-sach-va-tac-gia)

### Tài liệu tham khảo
- [Tài liệu PostgreSQL 16](https://www.postgresql.org/docs/16/tutorial-window.html)',NULL,NULL,6 FROM lessons l WHERE l.slug='window-frames-luy-ke-lag-lead' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lessons (id,category_id,title,slug,description,objectives,practice_topic_code,sort_order) SELECT 'def3042a-1960-53cd-bb51-7414c5b5f4fe',c.id,'13. CTE đệ quy và dữ liệu phân cấp','cte-de-quy-va-du-lieu-phan-cap','Hiểu phần neo và phần đệ quy; Theo dõi cấp độ cây dữ liệu; Ngăn chu trình bằng điều kiện đường đi.','["Hiểu phần neo và phần đệ quy", "Theo dõi cấp độ cây dữ liệu", "Ngăn chu trình bằng điều kiện đường đi"]'::jsonb,'SQL_ADVANCED',13 FROM course_categories c WHERE lower(trim(c.name))=lower('SQL nâng cao') ORDER BY c.created_at LIMIT 1 ON CONFLICT (slug) DO UPDATE SET category_id=EXCLUDED.category_id,title=EXCLUDED.title,description=EXCLUDED.description,objectives=EXCLUDED.objectives,practice_topic_code=EXCLUDED.practice_topic_code,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=1 ORDER BY s.created_at LIMIT 1),'15c24169-4615-59a5-89af-072c7415e225'::uuid),l.id,'1. Mục tiêu và chuẩn bị','### Sau bài này bạn có thể
- Hiểu phần neo và phần đệ quy
- Theo dõi cấp độ cây dữ liệu
- Ngăn chu trình bằng điều kiện đường đi

### Kiến thức cần có
Đã học các bài SQL cơ bản và hiểu books, publishers, categories, authors, book_authors.

### Cách học
Đọc lý thuyết, dự đoán kết quả SQL trước khi chạy, sau đó đối chiếu giải thích. Các ví dụ truy vấn dùng bộ sách với 15 sách, 5 tác giả, 5 nhà xuất bản, 5 thể loại và 20 liên kết sách–tác giả. Một đầu sách có thể có nhiều tác giả; giá tính theo VND. Không cộng lẫn số đầu sách với số cuốn tồn kho.',NULL,'{"type": "info", "title": "Môi trường ví dụ", "content": "Cú pháp theo PostgreSQL 16. Ví dụ CREATE/UPDATE/DELETE và EXPLAIN được đánh dấu riêng, không phải đáp án cho phòng chỉ cho phép SELECT."}'::jsonb,1 FROM lessons l WHERE l.slug='cte-de-quy-va-du-lieu-phan-cap' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=2 ORDER BY s.created_at LIMIT 1),'a44adf2a-31a2-5903-87b1-a6e726ce33a2'::uuid),l.id,'2. Khái niệm và cách suy luận','WITH RECURSIVE phục vụ quan hệ lặp như cây danh mục hoặc chuỗi quản lý. Phần neo tạo dòng ban đầu; phần đệ quy dùng dòng đã sinh để tìm cấp tiếp theo. Các vòng tích lũy kết quả đến khi vòng mới không tạo dòng hoặc đạt điều kiện dừng. Hiểu điều kiện kết thúc trước khi chạy trên dữ liệu lớn.

categories hiện tại không có parent_id, nên ví dụ cây dùng VALUES độc lập, không giả định schema đã có phân cấp. Mỗi dòng có id, parent_id, name. Phần neo chọn gốc parent_id IS NULL; phần đệ quy nối con.parent_id = cha.id; depth tăng một qua mỗi cấp. ORDER BY cuối quyết định hiển thị, không cam kết thứ tự duyệt vật lý của hệ quản trị.

Dữ liệu thực có thể chứa chu trình. Chỉ giới hạn depth có thể cắt kết quả mà không giải quyết lỗi mô hình. Có thể giữ mảng path chứa các id đã đi và chỉ nối con chưa nằm trong path. UNION loại dòng trùng toàn bộ nhưng không đủ chống chu trình nếu depth hoặc path thay đổi mỗi vòng. Ngoài chống lặp, cần xét nút mồ côi không nối tới gốc: truy vấn bắt đầu từ gốc có thể không nhìn thấy chúng.',NULL,NULL,2 FROM lessons l WHERE l.slug='cte-de-quy-va-du-lieu-phan-cap' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=3 ORDER BY s.created_at LIMIT 1),'47966303-bcfb-514a-a004-8f1be3020362'::uuid),l.id,'3. Dãy có điều kiện dừng','### Phân tích ví dụ
Có 5 dòng từ 1 đến 5. Kiểm tra n < 5 trước khi sinh n + 1 nên giá trị cuối là 5.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **5**.

| n |
| --- |
| 1 |
| 2 |
| 3 |
| 4 |
| 5 |','WITH RECURSIVE numbers(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM numbers WHERE n < 5
)
SELECT n FROM numbers ORDER BY n;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,3 FROM lessons l WHERE l.slug='cte-de-quy-va-du-lieu-phan-cap' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=4 ORDER BY s.created_at LIMIT 1),'adc6caaf-63a4-507e-8924-c86e84499c55'::uuid),l.id,'4. Cây nhỏ có lưu đường đi','### Phân tích ví dụ
Gốc Sách có depth 0; Công nghệ, Văn học có depth 1; Cơ sở dữ liệu có depth 2. path hỗ trợ hiển thị và phát hiện id đã đi qua.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **4**.

| id | name | depth | path |
| --- | --- | --- | --- |
| 1 | Sách | 0 | [1] |
| 2 | Công nghệ | 1 | [1, 2] |
| 4 | Cơ sở dữ liệu | 2 | [1, 2, 4] |
| 3 | Văn học | 1 | [1, 3] |','WITH RECURSIVE nodes(id, parent_id, name) AS (
    VALUES (1, NULL::integer, ''Sách''), (2, 1, ''Công nghệ''),
           (3, 1, ''Văn học''), (4, 2, ''Cơ sở dữ liệu'')
), tree AS (
    SELECT id, parent_id, name, 0 AS depth, ARRAY[id] AS path
    FROM nodes WHERE parent_id IS NULL
    UNION ALL
    SELECT n.id, n.parent_id, n.name, t.depth + 1, t.path || n.id
    FROM nodes n JOIN tree t ON n.parent_id = t.id
    WHERE NOT n.id = ANY(t.path)
)
SELECT id, name, depth, path FROM tree ORDER BY path;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,4 FROM lessons l WHERE l.slug='cte-de-quy-va-du-lieu-phan-cap' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=5 ORDER BY s.created_at LIMIT 1),'dc4e08ee-7e3e-5019-b7aa-183104825df5'::uuid),l.id,'5. Lỗi thường gặp và cách tránh','**1.** Không chạy đệ quy thiếu điều kiện dừng trên dữ liệu có thể lặp.

**2.** UNION không chặn mọi chu trình nếu depth/path thay đổi.

**3.** Không giả định categories hiện có parent_id.

### Quy trình kiểm tra
Đọc lại điều kiện tại đúng giá trị ranh giới; kiểm tra NULL, dòng không có đối tác và trường hợp đồng giá. Một câu SQL chạy được chưa chứng minh kết quả đã đúng với yêu cầu.',NULL,'{"type": "warning", "title": "Kiểm tra ý nghĩa trước cú pháp", "content": "Không chạy đệ quy thiếu điều kiện dừng trên dữ liệu có thể lặp."}'::jsonb,5 FROM lessons l WHERE l.slug='cte-de-quy-va-du-lieu-phan-cap' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=6 ORDER BY s.created_at LIMIT 1),'e7c4ca11-0414-5bfa-8aaf-f960bdba9a08'::uuid),l.id,'6. Tự kiểm tra và chuyển sang thực hành','**Câu 1: Phần neo dùng làm gì?**

Tự trả lời trước khi đọc. **Giải thích:** Tạo tập bắt đầu, ví dụ nút gốc hoặc số đầu tiên.

**Câu 2: NOT n.id = ANY(t.path) giúp gì?**

Tự trả lời trước khi đọc. **Giải thích:** Không đi lại id đã nằm trên đường đi hiện tại.

### Thực hành sau khi học
Chuyên đề mở rộng sau CTE thường; không cần dùng đệ quy nếu đề chỉ yêu cầu truy vấn con thông thường.

[Mở database sách để thực hành](/practice/quan-ly-sach-va-tac-gia)

### Tài liệu tham khảo
- [Tài liệu PostgreSQL 16](https://www.postgresql.org/docs/16/queries-with.html)',NULL,NULL,6 FROM lessons l WHERE l.slug='cte-de-quy-va-du-lieu-phan-cap' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lessons (id,category_id,title,slug,description,objectives,practice_topic_code,sort_order) SELECT 'd59ce1b6-d70c-56ec-b47c-65708ead0ea4',c.id,'14. Chỉ mục, EXPLAIN và kiểm tra hiệu năng','chi-muc-explain-va-toi-uu-truy-van','Hiểu lợi ích và chi phí chỉ mục; Đọc kế hoạch thay vì đoán tốc độ; Phân biệt EXPLAIN và EXPLAIN ANALYZE.','["Hiểu lợi ích và chi phí chỉ mục", "Đọc kế hoạch thay vì đoán tốc độ", "Phân biệt EXPLAIN và EXPLAIN ANALYZE"]'::jsonb,'SQL_ADVANCED',14 FROM course_categories c WHERE lower(trim(c.name))=lower('SQL nâng cao') ORDER BY c.created_at LIMIT 1 ON CONFLICT (slug) DO UPDATE SET category_id=EXCLUDED.category_id,title=EXCLUDED.title,description=EXCLUDED.description,objectives=EXCLUDED.objectives,practice_topic_code=EXCLUDED.practice_topic_code,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=1 ORDER BY s.created_at LIMIT 1),'488a51bf-ae18-5352-ad20-6b84ff360d04'::uuid),l.id,'1. Mục tiêu và chuẩn bị','### Sau bài này bạn có thể
- Hiểu lợi ích và chi phí chỉ mục
- Đọc kế hoạch thay vì đoán tốc độ
- Phân biệt EXPLAIN và EXPLAIN ANALYZE

### Kiến thức cần có
Đã học các bài SQL cơ bản và hiểu books, publishers, categories, authors, book_authors.

### Cách học
Đọc lý thuyết, dự đoán kết quả SQL trước khi chạy, sau đó đối chiếu giải thích. Các ví dụ truy vấn dùng bộ sách với 15 sách, 5 tác giả, 5 nhà xuất bản, 5 thể loại và 20 liên kết sách–tác giả. Một đầu sách có thể có nhiều tác giả; giá tính theo VND. Không cộng lẫn số đầu sách với số cuốn tồn kho.',NULL,'{"type": "info", "title": "Môi trường ví dụ", "content": "Cú pháp theo PostgreSQL 16. Ví dụ CREATE/UPDATE/DELETE và EXPLAIN được đánh dấu riêng, không phải đáp án cho phòng chỉ cho phép SELECT."}'::jsonb,1 FROM lessons l WHERE l.slug='chi-muc-explain-va-toi-uu-truy-van' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=2 ORDER BY s.created_at LIMIT 1),'35eaa685-4128-5208-9d31-599755194adf'::uuid),l.id,'2. Khái niệm và cách suy luận','Chỉ mục hỗ trợ tìm dòng phù hợp mà không luôn đọc toàn bảng. Nó tốn dung lượng và cần cập nhật khi ghi dữ liệu. Chỉ mục phù hợp có thể giúp lọc hoặc JOIN, nhưng với bảng 15 dòng, đọc tuần tự thường hợp lý vì chi phí dùng chỉ mục có thể không đáng. Đừng lấy bộ mẫu nhỏ làm kết luận cho hệ thống lớn.

EXPLAIN mô tả kế hoạch dự kiến. EXPLAIN ANALYZE thực sự chạy câu lệnh để báo số liệu, nên phải hiểu tác dụng của lệnh được phân tích. cost là đơn vị ước lượng nội bộ, không trực tiếp là mili giây. So sánh số dòng dự kiến và thực tế giúp phát hiện ước lượng sai; không chỉ nhìn Index Scan hay Sequential Scan để kết luận tốt/xấu.

Trước tối ưu, kiểm tra tính đúng, mức dữ liệu và nhu cầu: cần cột nào, bao nhiêu dòng, lọc có chọn lọc không, JOIN có nhân bản không. Với chỉ mục ghép, thứ tự cột và dạng điều kiện quan trọng; không thêm mọi cột theo thói quen. PostgreSQL thường tạo chỉ mục cho PRIMARY KEY/UNIQUE nhưng không tự tạo riêng mọi chỉ mục phía cột khóa ngoại tham chiếu. Đo trên dữ liệu đại diện và chú ý ảnh hưởng bộ nhớ đệm, thống kê và tải đồng thời.',NULL,NULL,2 FROM lessons l WHERE l.slug='chi-muc-explain-va-toi-uu-truy-van' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=3 ORDER BY s.created_at LIMIT 1),'cd3d4544-a6c6-5261-af63-b9d133ab3360'::uuid),l.id,'3. Xem kế hoạch dự kiến','### Phân tích ví dụ
Đầu ra là mô tả kế hoạch, không phải dòng sách. Kế hoạch cụ thể phụ thuộc môi trường; không cần học thuộc tên một kiểu quét.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.','EXPLAIN
SELECT book_id, title, price FROM books
WHERE publisher_id = 1 AND price >= 100000;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,3 FROM lessons l WHERE l.slug='chi-muc-explain-va-toi-uu-truy-van' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=4 ORDER BY s.created_at LIMIT 1),'bf05873a-46fe-52c4-93c4-97dfc96bf472'::uuid),l.id,'4. Đo một SELECT trên mẫu','### Phân tích ví dụ
SELECT được thực thi để thu số liệu, nhưng hiển thị kế hoạch. Thời gian phụ thuộc máy và trạng thái bộ nhớ đệm, không có một số mili giây chuẩn chung.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.','EXPLAIN (ANALYZE, BUFFERS)
SELECT b.book_id, b.title, p.name
FROM books b JOIN publishers p ON p.publisher_id = b.publisher_id
WHERE b.stock_quantity > 0;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,4 FROM lessons l WHERE l.slug='chi-muc-explain-va-toi-uu-truy-van' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=5 ORDER BY s.created_at LIMIT 1),'9a6c492e-a40e-54b4-920c-42ba775e0b17'::uuid),l.id,'5. Lỗi thường gặp và cách tránh','**1.** Index không luôn nhanh hơn đọc tuần tự, nhất là bảng nhỏ.

**2.** EXPLAIN ANALYZE thực sự chạy câu lệnh.

**3.** Không tối ưu truy vấn đang sai kết quả hoặc cộng lặp do JOIN.

### Quy trình kiểm tra
Đọc lại điều kiện tại đúng giá trị ranh giới; kiểm tra NULL, dòng không có đối tác và trường hợp đồng giá. Một câu SQL chạy được chưa chứng minh kết quả đã đúng với yêu cầu.',NULL,'{"type": "warning", "title": "Kiểm tra ý nghĩa trước cú pháp", "content": "Index không luôn nhanh hơn đọc tuần tự, nhất là bảng nhỏ."}'::jsonb,5 FROM lessons l WHERE l.slug='chi-muc-explain-va-toi-uu-truy-van' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=6 ORDER BY s.created_at LIMIT 1),'3a076cac-92a0-5f25-8f9c-4424d092693b'::uuid),l.id,'6. Tự kiểm tra và chuyển sang thực hành','**Câu 1: Thêm chỉ mục có làm ghi dữ liệu miễn phí không?**

Tự trả lời trước khi đọc. **Giải thích:** Không; các thay đổi có thể phải cập nhật thêm chỉ mục.

**Câu 2: cost = 100 có nghĩa 100 ms không?**

Tự trả lời trước khi đọc. **Giải thích:** Không, cost là đơn vị ước lượng của bộ tối ưu.

### Thực hành sau khi học
Đo sau khi có đáp án đúng; EXPLAIN cần môi trường cho phép lệnh phân tích.

[Mở database sách để thực hành](/practice/quan-ly-sach-va-tac-gia)

### Tài liệu tham khảo
- [Tài liệu PostgreSQL 16](https://www.postgresql.org/docs/16/using-explain.html)',NULL,NULL,6 FROM lessons l WHERE l.slug='chi-muc-explain-va-toi-uu-truy-van' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lessons (id,category_id,title,slug,description,objectives,practice_topic_code,sort_order) SELECT 'a91e6299-fbe5-590b-9148-d0c2cfd3e772',c.id,'15. Transaction, SAVEPOINT và tính nhất quán','transaction-savepoint-va-nhat-quan-du-lieu','Dùng BEGIN/COMMIT/ROLLBACK đúng phạm vi; Hoàn tác một phần bằng SAVEPOINT; Hiểu tranh chấp khi nhiều phiên cùng sửa.','["Dùng BEGIN/COMMIT/ROLLBACK đúng phạm vi", "Hoàn tác một phần bằng SAVEPOINT", "Hiểu tranh chấp khi nhiều phiên cùng sửa"]'::jsonb,'SQL_ADVANCED',15 FROM course_categories c WHERE lower(trim(c.name))=lower('SQL nâng cao') ORDER BY c.created_at LIMIT 1 ON CONFLICT (slug) DO UPDATE SET category_id=EXCLUDED.category_id,title=EXCLUDED.title,description=EXCLUDED.description,objectives=EXCLUDED.objectives,practice_topic_code=EXCLUDED.practice_topic_code,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=1 ORDER BY s.created_at LIMIT 1),'e0431b54-019d-545f-92fb-86bfd8dfb867'::uuid),l.id,'1. Mục tiêu và chuẩn bị','### Sau bài này bạn có thể
- Dùng BEGIN/COMMIT/ROLLBACK đúng phạm vi
- Hoàn tác một phần bằng SAVEPOINT
- Hiểu tranh chấp khi nhiều phiên cùng sửa

### Kiến thức cần có
Đã học các bài SQL cơ bản và hiểu books, publishers, categories, authors, book_authors.

### Cách học
Đọc lý thuyết, dự đoán kết quả SQL trước khi chạy, sau đó đối chiếu giải thích. Các ví dụ truy vấn dùng bộ sách với 15 sách, 5 tác giả, 5 nhà xuất bản, 5 thể loại và 20 liên kết sách–tác giả. Một đầu sách có thể có nhiều tác giả; giá tính theo VND. Không cộng lẫn số đầu sách với số cuốn tồn kho.',NULL,'{"type": "info", "title": "Môi trường ví dụ", "content": "Cú pháp theo PostgreSQL 16. Ví dụ CREATE/UPDATE/DELETE và EXPLAIN được đánh dấu riêng, không phải đáp án cho phòng chỉ cho phép SELECT."}'::jsonb,1 FROM lessons l WHERE l.slug='transaction-savepoint-va-nhat-quan-du-lieu' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=2 ORDER BY s.created_at LIMIT 1),'8ca16b93-602f-5180-995c-8c41155df119'::uuid),l.id,'2. Khái niệm và cách suy luận','Transaction gom thao tác liên quan thành đơn vị nhất quán: COMMIT chấp nhận hoặc ROLLBACK hoàn tác các thay đổi chưa commit. Bán một cuốn sách có thể gồm giảm tồn và ghi nhận giao dịch; nếu một phần lỗi không nên để phần kia tồn tại riêng. ACID diễn tả nguyên tử, nhất quán theo ràng buộc, cô lập và bền vững sau commit theo cấu hình hệ thống.

SAVEPOINT đặt mốc bên trong transaction để ROLLBACK TO SAVEPOINT giữ các bước hợp lệ trước mốc. PostgreSQL thường đánh dấu transaction lỗi sau một lệnh lỗi; cần rollback toàn bộ hoặc về savepoint đã đặt trước lỗi để tiếp tục. BEGIN không tự làm mọi truy vấn tránh tranh chấp giữa các phiên.

Hai người cùng đọc tồn rồi ghi giá trị đã tính từ dữ liệu cũ có thể gây sai lệch. Mẫu UPDATE ... SET stock = stock - 1 WHERE stock > 0 RETURNING ... kiểm tra và giảm trong một lệnh. Mức cô lập và khóa còn quyết định các trường hợp đồng thời khác. Ví dụ dùng bảng tạm và rollback, không sửa bộ dữ liệu Practice. Phòng bài tập chỉ cho phép SELECT không phải nơi chạy các lệnh quản trị này.',NULL,NULL,2 FROM lessons l WHERE l.slug='transaction-savepoint-va-nhat-quan-du-lieu' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=3 ORDER BY s.created_at LIMIT 1),'4fb474ab-1c4b-538e-b646-5f029a8fed0f'::uuid),l.id,'3. Quay về một mốc trong giao dịch','### Phân tích ví dụ
SELECT thấy stock = 3 vì bước giảm đã hoàn tác. ROLLBACK cuối hủy cả bảng tạm và INSERT.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.','BEGIN;
CREATE TEMP TABLE demo_inventory (book_id INT PRIMARY KEY, stock INT CHECK (stock >= 0));
INSERT INTO demo_inventory VALUES (1, 3);
SAVEPOINT before_sale;
UPDATE demo_inventory SET stock = stock - 1 WHERE book_id = 1 AND stock > 0;
ROLLBACK TO SAVEPOINT before_sale;
SELECT book_id, stock FROM demo_inventory;
ROLLBACK;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,3 FROM lessons l WHERE l.slug='transaction-savepoint-va-nhat-quan-du-lieu' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=4 ORDER BY s.created_at LIMIT 1),'617fc91d-30d7-5af0-a5c9-ace31e4df309'::uuid),l.id,'4. Giảm tồn có điều kiện','### Phân tích ví dụ
RETURNING cho (1, 0). Không khớp điều kiện thì UPDATE không trả dòng; ứng dụng phải xử lý hết hàng thay vì giả định luôn thành công.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.','BEGIN;
CREATE TEMP TABLE demo_inventory (book_id INT PRIMARY KEY, stock INT NOT NULL CHECK (stock >= 0));
INSERT INTO demo_inventory VALUES (1, 1), (2, 0);
UPDATE demo_inventory SET stock = stock - 1
WHERE book_id = 1 AND stock > 0 RETURNING book_id, stock;
ROLLBACK;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,4 FROM lessons l WHERE l.slug='transaction-savepoint-va-nhat-quan-du-lieu' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=5 ORDER BY s.created_at LIMIT 1),'ba29d2a2-0a8f-5388-96af-9f48bcb59f51'::uuid),l.id,'5. Lỗi thường gặp và cách tránh','**1.** ROLLBACK không hoàn tác được transaction đã COMMIT.

**2.** Không giữ transaction mở lâu khi chờ thao tác người dùng.

**3.** Đọc rồi cập nhật bằng hai lệnh riêng cần xét khóa hoặc điều kiện để tránh dữ liệu cũ.

### Quy trình kiểm tra
Đọc lại điều kiện tại đúng giá trị ranh giới; kiểm tra NULL, dòng không có đối tác và trường hợp đồng giá. Một câu SQL chạy được chưa chứng minh kết quả đã đúng với yêu cầu.',NULL,'{"type": "warning", "title": "Kiểm tra ý nghĩa trước cú pháp", "content": "ROLLBACK không hoàn tác được transaction đã COMMIT."}'::jsonb,5 FROM lessons l WHERE l.slug='transaction-savepoint-va-nhat-quan-du-lieu' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=6 ORDER BY s.created_at LIMIT 1),'35d6b4e6-8362-5aa4-8aef-bc9592ce455f'::uuid),l.id,'6. Tự kiểm tra và chuyển sang thực hành','**Câu 1: SAVEPOINT có thay thế COMMIT không?**

Tự trả lời trước khi đọc. **Giải thích:** Không, chỉ là mốc trong transaction chưa chấp nhận cuối cùng.

**Câu 2: RETURNING không có dòng có nhất thiết lỗi cú pháp không?**

Tự trả lời trước khi đọc. **Giải thích:** Không, có thể không có dòng thỏa điều kiện, ví dụ stock bằng 0.

### Thực hành sau khi học
Hiểu cách rollback dữ liệu mẫu; không thử sửa catalog ứng dụng trong phòng bài tập.

[Mở database sách để thực hành](/practice/quan-ly-sach-va-tac-gia)

### Tài liệu tham khảo
- [Tài liệu PostgreSQL 16](https://www.postgresql.org/docs/16/tutorial-transactions.html)',NULL,NULL,6 FROM lessons l WHERE l.slug='transaction-savepoint-va-nhat-quan-du-lieu' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lessons (id,category_id,title,slug,description,objectives,practice_topic_code,sort_order) SELECT 'e3cc1762-4e72-5755-b353-db210a035525',c.id,'16. Phương pháp giải bài SQL và kiểm tra đáp án','phuong-phap-giai-bai-sql-va-kiem-tra-dap-an','Chuyển đề thành mức dữ liệu và cột đầu ra; Kiểm tra NULL, dòng thừa và đồng hạng; Chọn kỹ thuật theo câu hỏi.','["Chuyển đề thành mức dữ liệu và cột đầu ra", "Kiểm tra NULL, dòng thừa và đồng hạng", "Chọn kỹ thuật theo câu hỏi"]'::jsonb,'SQL_ADVANCED',16 FROM course_categories c WHERE lower(trim(c.name))=lower('SQL nâng cao') ORDER BY c.created_at LIMIT 1 ON CONFLICT (slug) DO UPDATE SET category_id=EXCLUDED.category_id,title=EXCLUDED.title,description=EXCLUDED.description,objectives=EXCLUDED.objectives,practice_topic_code=EXCLUDED.practice_topic_code,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=1 ORDER BY s.created_at LIMIT 1),'d0bcd28c-18b2-5263-ba61-9fea678018f9'::uuid),l.id,'1. Mục tiêu và chuẩn bị','### Sau bài này bạn có thể
- Chuyển đề thành mức dữ liệu và cột đầu ra
- Kiểm tra NULL, dòng thừa và đồng hạng
- Chọn kỹ thuật theo câu hỏi

### Kiến thức cần có
Đã học các bài SQL cơ bản và hiểu books, publishers, categories, authors, book_authors.

### Cách học
Đọc lý thuyết, dự đoán kết quả SQL trước khi chạy, sau đó đối chiếu giải thích. Các ví dụ truy vấn dùng bộ sách với 15 sách, 5 tác giả, 5 nhà xuất bản, 5 thể loại và 20 liên kết sách–tác giả. Một đầu sách có thể có nhiều tác giả; giá tính theo VND. Không cộng lẫn số đầu sách với số cuốn tồn kho.',NULL,'{"type": "info", "title": "Môi trường ví dụ", "content": "Cú pháp theo PostgreSQL 16. Ví dụ CREATE/UPDATE/DELETE và EXPLAIN được đánh dấu riêng, không phải đáp án cho phòng chỉ cho phép SELECT."}'::jsonb,1 FROM lessons l WHERE l.slug='phuong-phap-giai-bai-sql-va-kiem-tra-dap-an' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=2 ORDER BY s.created_at LIMIT 1),'db515c10-b1b0-593a-aeda-27091f72b240'::uuid),l.id,'2. Khái niệm và cách suy luận','Bắt đầu bằng việc viết một dòng kết quả đại diện cho sách, tác giả hay nhà xuất bản. Liệt kê cột đầu ra đúng thứ tự, xác định tên gốc và bí danh. Ghi riêng điều kiện lọc dòng, lọc nhóm và quy tắc sắp xếp. Đừng viết ngay một truy vấn dài rồi chỉ sửa theo số dòng mà không hiểu nguyên nhân.

Cần thuộc tính bảng liên quan thì JOIN; cần một dòng mỗi nhóm thì GROUP BY; chỉ hỏi có/không quan hệ thì EXISTS/NOT EXISTS; so sánh ngưỡng tính từ dữ liệu thì truy vấn con hoặc CTE; giữ từng dòng kèm hạng/lũy kế thì hàm cửa sổ. Không có một kỹ thuật duy nhất tốt nhất cho mọi bài.

Trước nộp, kiểm tra nhà xuất bản không có sách, sách hai tác giả, giá bằng ngưỡng, tồn bằng 0, NULL do LEFT JOIN và đồng giá. Cùng số dòng không chứng minh cùng kết quả. Kiểm tra tên cột, thứ tự cột, giá trị và số lần lặp. EXCEPT hai chiều tìm khác biệt giá trị nhưng mặc định loại trùng; cần EXCEPT ALL hoặc nhóm đếm nếu muốn xét tần suất. Tối ưu chỉ sau khi đã giải thích được vì sao đáp án đúng trên dữ liệu biên.',NULL,NULL,2 FROM lessons l WHERE l.slug='phuong-phap-giai-bai-sql-va-kiem-tra-dap-an' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=3 ORDER BY s.created_at LIMIT 1),'181f462a-74f7-5e6a-9e49-12a037919332'::uuid),l.id,'3. Tránh cộng lặp khi tính kho','### Phân tích ví dụ
Kết quả 23256000. Không cần JOIN tác giả vì mức dữ liệu là đầu sách; JOIN nhiều–nhiều sẽ cộng lặp một số sách.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **1**.

| inventory_value |
| --- |
| 23256000.0 |','SELECT SUM(price * stock_quantity) AS inventory_value FROM books;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,3 FROM lessons l WHERE l.slug='phuong-phap-giai-bai-sql-va-kiem-tra-dap-an' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=4 ORDER BY s.created_at LIMIT 1),'d1f670de-0a4b-55af-beca-f5e0f5acc33d'::uuid),l.id,'4. Giữ nhóm rỗng ở báo cáo','### Phân tích ví dụ
Có 5 dòng. Nhà xuất bản 5 có book_count = 0, total_stock = 0; các đơn vị còn lại có số sách 4,4,4,3.

Hãy đọc nguồn dữ liệu và điều kiện trước, sau đó kiểm tra tên cột, số dòng và thứ tự đầu ra. Sao chép câu SQL bên dưới để đối chiếu trên đúng bộ dữ liệu mẫu.

### Kết quả trên bộ dữ liệu mẫu
Tổng số dòng: **5**.

| publisher_id | publisher_name | book_count | total_stock |
| --- | --- | --- | --- |
| 1 | Tri Thức Mới | 4 | 42 |
| 2 | Ánh Dương | 4 | 35 |
| 3 | Hành Trang | 4 | 26 |
| 4 | Trang Sách Xanh | 3 | 47 |
| 5 | Cánh Diều | 0 | 0 |','SELECT p.publisher_id, p.name AS publisher_name,
       COUNT(b.book_id) AS book_count,
       COALESCE(SUM(b.stock_quantity), 0) AS total_stock
FROM publishers p LEFT JOIN books b ON b.publisher_id = p.publisher_id
GROUP BY p.publisher_id, p.name ORDER BY p.publisher_id;','{"type": "tip", "title": "Tự kiểm tra", "content": "Trước khi chạy, dự đoán một dòng đại diện cho đối tượng gì và trường hợp biên nào có thể làm kết quả thay đổi."}'::jsonb,4 FROM lessons l WHERE l.slug='phuong-phap-giai-bai-sql-va-kiem-tra-dap-an' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=5 ORDER BY s.created_at LIMIT 1),'28490a48-e133-5c58-abdf-4a53f0f9abd2'::uuid),l.id,'5. Lỗi thường gặp và cách tránh','**1.** Chạy không lỗi chưa chứng minh đúng nghiệp vụ.

**2.** Không thêm LIMIT hoặc bỏ dòng chỉ để khớp số lượng mong đợi.

**3.** Không ép cách giải học thuộc vào đề có mức dữ liệu khác.

### Quy trình kiểm tra
Đọc lại điều kiện tại đúng giá trị ranh giới; kiểm tra NULL, dòng không có đối tác và trường hợp đồng giá. Một câu SQL chạy được chưa chứng minh kết quả đã đúng với yêu cầu.',NULL,'{"type": "warning", "title": "Kiểm tra ý nghĩa trước cú pháp", "content": "Chạy không lỗi chưa chứng minh đúng nghiệp vụ."}'::jsonb,5 FROM lessons l WHERE l.slug='phuong-phap-giai-bai-sql-va-kiem-tra-dap-an' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
INSERT INTO lesson_sections (id,lesson_id,title,content,sample_sql,note_box,sort_order) SELECT COALESCE((SELECT s.id FROM lesson_sections s WHERE s.lesson_id=l.id AND s.sort_order=6 ORDER BY s.created_at LIMIT 1),'b5929cfe-9172-5f61-b234-90bf6974197e'::uuid),l.id,'6. Tự kiểm tra và chuyển sang thực hành','**Câu 1: Mọi sách của tác giả đều đạt giá cao thì EXISTS sách giá cao đã đủ chưa?**

Tự trả lời trước khi đọc. **Giải thích:** Chưa; phải loại phản ví dụ và xác định có yêu cầu ít nhất một sách không.

**Câu 2: Hai truy vấn cùng COUNT(*) có chắc tương đương?**

Tự trả lời trước khi đọc. **Giải thích:** Không; còn phải kiểm tra giá trị và tần suất các dòng.

### Thực hành sau khi học
Thực hành lần lượt 10 bài dễ, 10 trung bình, 10 khó của Quản lý Sách & Tác giả.

[Mở database sách để thực hành](/practice/quan-ly-sach-va-tac-gia)

### Tài liệu tham khảo
- [Tài liệu PostgreSQL 16](https://www.postgresql.org/docs/16/queries-table-expressions.html)',NULL,NULL,6 FROM lessons l WHERE l.slug='phuong-phap-giai-bai-sql-va-kiem-tra-dap-an' ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title,content=EXCLUDED.content,sample_sql=EXCLUDED.sample_sql,note_box=EXCLUDED.note_box,sort_order=EXCLUDED.sort_order;
DO $$ BEGIN
IF EXISTS (SELECT 1 FROM _learning_ids_before old LEFT JOIN lessons l ON l.slug=old.slug WHERE l.id IS DISTINCT FROM old.id)
THEN RAISE EXCEPTION 'Existing lesson IDs changed'; END IF;
IF EXISTS ((SELECT * FROM _learning_progress_before EXCEPT ALL SELECT * FROM user_lesson_progress)
UNION ALL (SELECT * FROM user_lesson_progress EXCEPT ALL SELECT * FROM _learning_progress_before))
THEN RAISE EXCEPTION 'Learning progress unexpectedly changed'; END IF;
END $$;
COMMIT;
