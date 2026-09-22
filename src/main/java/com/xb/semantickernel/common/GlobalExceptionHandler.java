package com.xb.semantickernel.common;

/**
 * GlobalExceptionHandler - 全局异常处理器
 *
 * 使用 {@code @RestControllerAdvice} 拦截 Controller 层抛出的异常，
 * 将其转换为统一的 {@link ApiResponse} 格式返回。
 * 教学中演示 Spring MVC 的集中式异常处理机制和 requestId 追踪技巧。
 *
 * @author ibqy
 */
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理请求缺少必要参数时抛出的异常
     *
     * @param ex 参数缺失异常
     * @return 包含缺失参数名的错误响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException ex) {
        log.warn("缺少请求参数: {}", ex.getParameterName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, "缺少必要参数: " + ex.getParameterName()));
    }

    /**
     * 处理 @Valid 校验失败时抛出的异常，提取首个出错字段信息
     *
     * @param ex 校验异常
     * @return 包含字段和错误信息的响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String fieldError = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .orElse("参数校验失败");
        log.warn("参数校验失败: {}", fieldError);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, fieldError));
    }

    /**
     * 处理业务逻辑中的非法参数异常
     *
     * @param ex 非法参数异常
     * @return 包含异常消息的错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("非法参数: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, ex.getMessage()));
    }

    /**
     * 兜底处理所有未被上层捕获的异常
     *
     * 生成一个短 requestId 方便日志排查，避免将内部异常细节暴露给前端。
     *
     * @param ex 未预期的异常
     * @return 包含 requestId 的 500 错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception ex) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        log.error("未知错误 [{}]: {}", requestId, ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "服务器内部错误 [requestId=" + requestId + "]"));
    }
}
