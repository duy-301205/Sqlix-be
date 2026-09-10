package com.example.sqlix.exception;

import com.example.sqlix.dto.response.ApiResponse;
import com.example.sqlix.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    // --- 1. Catch-all: Unexpected RuntimeException (500) ---
    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<ApiResponse> handlingRuntimeException(RuntimeException exception, HttpServletRequest request) {
        String context = buildContext(request);
        log.error("RuntimeException {} | {}", context, exception.getMessage(), exception);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ErrorCode.UNAUTHENTICATED.getCode());
        apiResponse.setMessage(ErrorCode.UNAUTHENTICATED.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
    }

    // --- 2. AppException (business logic errors, 4xx/5xx based on ErrorCode) ---
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlingAppException(AppException exception, HttpServletRequest request) {
        ErrorCode errorCode = exception.getErrorCode();
        int statusCode = errorCode.getStatusCode().value();

        String context = buildContext(request);
        if (statusCode >= 500) {
            log.error("AppException [{}] {} | {}", errorCode.name(), context, errorCode.getMessage(), exception);
        } else {
            log.warn("AppException [{}] {} | {}", errorCode.name(), context, errorCode.getMessage());
        }

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    // --- 3. AccessDeniedException (403) ---
    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handlingAccessDeniedException(AccessDeniedException exception, HttpServletRequest request) {
        String context = buildContext(request);
        log.warn("AccessDenied {} | {}", context, exception.getMessage());

        ErrorCode errorCode = ErrorCode.ACCESS_DENIED;

        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    // --- 4. Validation errors (400) ---
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handlingMethodArgumentNotValidException(
            MethodArgumentNotValidException exception, HttpServletRequest request) {

        String context = buildContext(request);
        String fieldError = exception.getFieldError() != null ? exception.getFieldError().getDefaultMessage() : "unknown";
        String fieldName = exception.getFieldError() != null ? exception.getFieldError().getField() : "unknown";
        log.warn("Validation failed {} | field: {}", context, fieldError);

        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        try {
            errorCode = ErrorCode.valueOf(fieldError);
        } catch (IllegalArgumentException e) {
            // keep INVALID_KEY
        }

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    // --- 5. Catch-all: Exception (safety net for anything not RuntimeException) ---
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse> handlingException(Exception exception, HttpServletRequest request) {
        String context = buildContext(request);
        log.error("Unhandled Exception {} | {}", context, exception.getMessage(), exception);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ErrorCode.ACCESS_DENIED.getCode());
        apiResponse.setMessage(ErrorCode.ACCESS_DENIED.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiResponse> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        // Log WARN để không bị rác file log ERROR, dùng getResourcePath() sau khi fix import sẽ hết báo đỏ
        log.warn("Lỗi 404 - Truy cập sai API: {} | IP: {}", ex.getResourcePath(), getClientIp(request));

        // Trả về 404, KHÔNG bắn Telegram
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(404);
        apiResponse.setMessage("Đường dẫn không tồn tại. Vui lòng kiểm tra lại URL.");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
    }

    // ==================== HELPERS ====================

    private String buildContext(HttpServletRequest request) {
        return String.format("[%s %s] user=%s ip=%s",
                request.getMethod(),
                request.getRequestURI(),
                getCurrentUser(),
                getClientIp(request)
        );
    }

    private String getCurrentUser() {
        try {
            return SecurityUtils.getCurrentUserEmail();
        } catch (Exception ignored) {
            try {
                return SecurityUtils.getCurrentUserFullName();
            } catch (Exception ignoredToo) {
                return "anonymous";
            }
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getRequestBody(HttpServletRequest request) {
        try {
            if (request instanceof ContentCachingRequestWrapper wrapper) {
                byte[] buf = wrapper.getContentAsByteArray();
                if (buf.length > 0) {
                    return new String(buf, StandardCharsets.UTF_8);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
}
