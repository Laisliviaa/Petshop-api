package com.example.petshopapi.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(RecursoNaoEncontradoException ex,
                                                           HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException ex,
                                                           HttpServletRequest req) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), req);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(ConflictException ex,
                                                           HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
                                                                HttpServletRequest req) {
        return build(HttpStatus.CONFLICT,
                "Operação não permitida: este registro está vinculado a outros dados. "
                + "Remova os vínculos antes de excluir.", req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                             HttpServletRequest req) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();
        ApiErrorResponse body = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(400)
                .erro("Bad Request")
                .mensagem("Validação falhou. Verifique os campos obrigatórios.")
                .caminho(req.getRequestURI())
                .metodo(req.getMethod())
                .detalhes(detalhes)
                .build();
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingHeader(MissingRequestHeaderException ex,
                                                                HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST,
                "Header obrigatório ausente: " + ex.getHeaderName(), req);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParam(MissingServletRequestParameterException ex,
                                                               HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST,
                "Parâmetro obrigatório ausente: '" + ex.getParameterName()
                + "' (tipo esperado: " + ex.getParameterType() + ")", req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedJson(HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido ou ausente. Verifique o JSON enviado.", req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                               HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST,
                "Parâmetro '" + ex.getName() + "' com valor inválido: '" + ex.getValue()
                + "'. Tipo esperado: " + ex.getRequiredType().getSimpleName(), req);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex,
                                                                   HttpServletRequest req) {
        return build(HttpStatus.METHOD_NOT_ALLOWED,
                "Método HTTP '" + ex.getMethod() + "' não é permitido neste endpoint.", req);
    }

    // CORRIGIDO: HTTP 415 agora retorna ApiErrorResponse (antes caía no formato padrão do Spring)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnsupportedMedia(HttpMediaTypeNotSupportedException ex,
                                                                   HttpServletRequest req) {
        String tipo = ex.getContentType() != null ? ex.getContentType().toString() : "desconhecido";
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Content-Type '" + tipo + "' não é suportado. "
                + "Use 'application/json' no header Content-Type.", req);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoHandler(NoHandlerFoundException ex,
                                                            HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND,
                "Endpoint não encontrado: " + ex.getHttpMethod() + " " + ex.getRequestURL(), req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
                                                                  HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno inesperado. Por favor, tente novamente ou contate o suporte.", req);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String mensagem,
                                                   HttpServletRequest req) {
        return ResponseEntity.status(status).body(
                ApiErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(status.value())
                        .erro(status.getReasonPhrase())
                        .mensagem(mensagem)
                        .caminho(req.getRequestURI())
                        .metodo(req.getMethod())
                        .build());
    }
}
