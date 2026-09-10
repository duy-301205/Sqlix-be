package com.example.sqlix.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {

    // =========================================================================
    // 1000 - 1099: User & Authentication
    // Phục vụ: Register, Login, OAuth, Reset Password, Header
    // =========================================================================
    USER_EXISTED(1000, "Tên đăng nhập hoặc email đã được sử dụng.", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1001, "Không tìm thấy thông tin người dùng.", HttpStatus.NOT_FOUND),
    USERNAME_INVALID(1002, "Tên đăng nhập không hợp lệ (chỉ gồm chữ, số và dấu gạch dưới).", HttpStatus.BAD_REQUEST),
    WRONG_PASSWORD(1003, "Mật khẩu không chính xác.", HttpStatus.BAD_REQUEST),
    PASSWORD_RESET_TOKEN_INVALID(1004, "Mã hoặc liên kết khôi phục mật khẩu không hợp lệ.", HttpStatus.BAD_REQUEST),
    PASSWORD_RESET_TOKEN_EXPIRED(1005, "Liên kết khôi phục mật khẩu đã hết hạn.", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_ADMIN(1006, "Không thể xóa tài khoản Quản trị viên.", HttpStatus.FORBIDDEN),
    OAUTH_ACCOUNT_LINKED_OTHER(1007, "Tài khoản mạng xã hội này đã được liên kết với người dùng khác.", HttpStatus.BAD_REQUEST),
    TERMS_NOT_ACCEPTED(1008, "Bạn phải đồng ý với điều khoản & chính sách để tiếp tục.", HttpStatus.BAD_REQUEST),
    ACCOUNT_LOCKED(1009, "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ hỗ trợ.", HttpStatus.FORBIDDEN),

    // =========================================================================
    // 1100 - 1199: Profile & Gamification Settings
    // Phục vụ: Dashboard Settings, Avatar, Streak, Daily Target
    // =========================================================================
    PROFILE_NOT_EXISTED(1100, "Thông tin hồ sơ học viên không tồn tại.", HttpStatus.NOT_FOUND),
    PROFILE_NAME_INVALID(1101, "Họ và tên phải có ít nhất 2 ký tự.", HttpStatus.BAD_REQUEST),
    PROFILE_BIO_TOO_LONG(1102, "Tiểu sử không được vượt quá 500 ký tự.", HttpStatus.BAD_REQUEST),
    DAILY_TARGET_INVALID(1103, "Mục tiêu thời gian học mỗi ngày không hợp lệ.", HttpStatus.BAD_REQUEST),

    // =========================================================================
    // 1200 - 1299: Learning & Lessons
    // Phục vụ: Module 3 - Course Curriculum, Progress, Bookmark
    // =========================================================================
    LESSON_NOT_FOUND(1200, "Bài học không tồn tại.", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND(1201, "Chuyên đề học tập không tồn tại.", HttpStatus.NOT_FOUND),
    LESSON_ALREADY_COMPLETED(1202, "Bài học đã được đánh dấu hoàn thành trước đó.", HttpStatus.BAD_REQUEST),

    // =========================================================================
    // 1300 - 1399: Practice & SQL Sandbox
    // Phục vụ: Module 4 - Practice DB, ERD, SQL Execution & Grading
    // =========================================================================
    DATABASE_CATALOG_NOT_FOUND(1300, "Cơ sở dữ liệu mẫu không tồn tại.", HttpStatus.NOT_FOUND),
    EXERCISE_NOT_FOUND(1301, "Bài tập truy vấn không tồn tại.", HttpStatus.NOT_FOUND),
    SQL_SYNTAX_ERROR(1302, "Cú pháp câu lệnh SQL không hợp lệ.", HttpStatus.BAD_REQUEST),
    SQL_EXECUTION_TIMEOUT(1303, "Thời gian thực thi truy vấn vượt quá giới hạn cho phép.", HttpStatus.REQUEST_TIMEOUT),
    DANGEROUS_SQL_DETECTED(1304, "Phát hiện câu lệnh không an toàn (chỉ cho phép đọc dữ liệu SELECT).", HttpStatus.FORBIDDEN),
    QUERY_RESULT_MISMATCH(1305, "Kết quả truy vấn chưa khớp với bảng kỳ vọng.", HttpStatus.BAD_REQUEST),

    // =========================================================================
    // 1400 - 1499: Mock Test & Exams
    // Phục vụ: Module 5 - Mock Test, Countdown Timer, Submission
    // =========================================================================
    MOCK_TEST_NOT_FOUND(1400, "Đề thi thử không tồn tại.", HttpStatus.NOT_FOUND),
    EXAM_SESSION_EXPIRED(1401, "Thời gian làm bài thi đã hết. Hệ thống đã tự động thu bài.", HttpStatus.BAD_REQUEST),
    EXAM_ALREADY_SUBMITTED(1402, "Bài thi đã được nộp trước đó, không thể nộp lại.", HttpStatus.BAD_REQUEST),
    EXAM_NOT_IN_PROGRESS(1403, "Phiên làm bài thi không còn khả dụng.", HttpStatus.BAD_REQUEST),

    // =========================================================================
    // 7000 - 7099: Input Validation
    // Phục vụ: DTO Validation chung
    // =========================================================================
    VALIDATION_ERROR(7000, "Dữ liệu đầu vào không hợp lệ.", HttpStatus.BAD_REQUEST),
    EMAIL_INCORRECT_FORMAT(7001, "Định dạng email không hợp lệ.", HttpStatus.BAD_REQUEST),
    PASSWORD_TOO_WEAK(7002, "Mật khẩu phải chứa ít nhất 8 ký tự, bao gồm chữ và số.", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED(7003, "Mật khẩu không được để trống.", HttpStatus.BAD_REQUEST),
    PASSWORD_CONFIRM_INCORRECT(7004, "Mật khẩu xác nhận không khớp.", HttpStatus.BAD_REQUEST),

    // =========================================================================
    // 8000 - 8099: File & Storage
    // Phục vụ: Upload Avatar học viên
    // =========================================================================
    FILE_UPLOAD_ERROR(8000, "Lỗi khi tải ảnh đại diện lên hệ thống.", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_IS_EMPTY(8001, "Tệp tải lên không được để trống.", HttpStatus.BAD_REQUEST),
    UNSUPPORTED_FILE_TYPE(8002, "Chỉ chấp nhận tệp hình ảnh (JPG, PNG, WEBP).", HttpStatus.BAD_REQUEST),
    FILE_IMAGE_SIZE_EXCEEDED(8003, "Dung lượng ảnh vượt quá giới hạn tối đa (tối đa 5MB).", HttpStatus.BAD_REQUEST),

    // =========================================================================
    // 9000 - 9099: System & Security Infrastructure
    // Phục vụ: JWT Filter, Guards, Interceptors
    // =========================================================================
    UNAUTHENTICATED(9000, "Yêu cầu đăng nhập để truy cập tài nguyên này.", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(9001, "Bạn không có quyền thực hiện hành động này.", HttpStatus.FORBIDDEN),
    TOKEN_INVALID(9002, "Mã phiên đăng nhập (Token) không hợp lệ hoặc đã hết hạn.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_IS_MISSING(9003, "Thiếu Refresh Token trong yêu cầu.", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR(9004, "Lỗi hệ thống nội bộ, vui lòng thử lại sau.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(9005, "Mã lỗi không xác định.", HttpStatus.BAD_REQUEST);

    private int code;
    private String message;
    private HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
