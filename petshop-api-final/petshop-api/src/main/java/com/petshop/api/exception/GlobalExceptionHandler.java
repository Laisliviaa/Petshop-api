package com.petshop.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

/**
 * Intercepta todas as exceções lançadas pelos controllers e garante respostas
 * JSON padronizadas no formato {@link ErrorResponse}.
 *
 * <table border="1">
 * <tr><th>Exceção</th><th>HTTP</th></tr>
 * <tr><td>MethodArgumentNotValidException</td><td>400</td></tr>
 * <tr><td>HttpMessageNotReadableException</td><td>400</td></tr>
 * <tr><td>RecursoNaoEncontradoException</td><td>404</td></tr>
 * <tr><td>ConflictException</td><td>409</td></tr>
 * <tr><td>BusinessException</td><td>422</td></tr>
 * <tr><td>Exception (genérico)</td><td>500</td></tr>
 * </table>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<ErrorResponse.FieldErrorDetail> fieldErrors = ex.getBindingResult()
                .getAllErrors().stream()
                .map(error -> {
                    FieldError fe = (FieldError) error;
                    return ErrorResponse.FieldErrorDetail.builder()
                            .field(fe.getField())
                            .rejectedValue(fe.getRejectedValue())
                            .message(fe.getDefaultMessage())
                            .build();
                }).toList();

        return ResponseEntity.badRequest().body(
                ErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(400)
                        .error("Bad Request")
                        .message("Erro de validação nos campos da requisição.")
                        .path(request.getRequestURI())
                        .fieldErrors(fieldErrors)
                        .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFormat(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        String message = "Corpo da requisição inválido ou malformado.";
        Throwable cause = ex.getMostSpecificCause();
        if (cause != null && cause.getMessage() != null
                && cause.getMessage().contains("LocalDateTime")) {
            message = "Data/hora inválida. Use o formato: YYYY-MM-DDTHH:mm:ss (ex: 2026-05-25T14:30:00).";
        }

        return ResponseEntity.badRequest()
                .body(build(400, "Bad Request", message, request));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            RecursoNaoEncontradoException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(build(404, "Not Found", ex.getMessage(), request));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ConflictException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(build(409, "Conflict", ex.getMessage(), request));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(
            BusinessException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(build(422, "Unprocessable Entity", ex.getMessage(), request));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado em {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(build(500, "Internal Server Error",
                        "Ocorreu um erro inesperado. Tente novamente mais tarde.", request));
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private ErrorResponse build(int status, String error, String message,
                                HttpServletRequest request) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status)
                .error(error)
                .message(message)
                .path(request.getRequestURI())
                .build();
    }
}
