package com.tein.karardefteri.common.exception;

import com.tein.karardefteri.common.dto.ApiError;
import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(InvalidRequestException.class)
    ResponseEntity<ApiError> handleValidation(InvalidRequestException exception) {
        return ResponseEntity.badRequest().body(new ApiError(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    ResponseEntity<ApiError> handleBusinessRule(BusinessRuleViolationException exception) {
        return ResponseEntity.status(409).body(new ApiError(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException exception) {
        return ResponseEntity.status(404).body(new ApiError(exception.getCode(), exception.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var errors = new LinkedHashMap<String, String>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return new ResponseEntity<>(new ApiError("VALIDATION_ERROR", "İstek alanlarını kontrol edin.", errors),
                headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return new ResponseEntity<>(new ApiError("HTTP_" + status.value(), "İstek işlenemedi."), headers, status);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpected(Exception exception) {
        LOG.error("Unhandled API error", exception);
        return ResponseEntity.internalServerError()
                .body(new ApiError("INTERNAL_ERROR", "Beklenmeyen bir hata oluştu."));
    }
}
