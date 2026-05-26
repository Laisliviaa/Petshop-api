package com.petshop.api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

/**
 * Envelope padrão de resposta de erro da API PetShop.
 *
 * <pre>
 * Exemplo 404:
 * {
 *   "timestamp": "2026-05-22T14:00:00Z",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Pet com id 99 não encontrado(a).",
 *   "path": "/api/v1/pets/99"
 * }
 *
 * Exemplo 400 (validação):
 * {
 *   ...,
 *   "fieldErrors": [
 *     { "field": "nome", "rejectedValue": "", "message": "O nome do pet é obrigatório" }
 *   ]
 * }
 * </pre>
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ErrorResponse", description = "Envelope padrão de resposta de erro")
public class ErrorResponse {

    @Schema(description = "Momento em que o erro ocorreu", example = "2026-05-22T14:00:00Z")
    private Instant timestamp;

    @Schema(description = "Código HTTP", example = "404")
    private int status;

    @Schema(description = "Texto do status HTTP", example = "Not Found")
    private String error;

    @Schema(description = "Mensagem de erro legível", example = "Pet com id 99 não encontrado(a).")
    private String message;

    @Schema(description = "Caminho da requisição que gerou o erro", example = "/api/v1/pets/99")
    private String path;

    @Schema(description = "Erros de campo (apenas em respostas 400 de validação)")
    private List<FieldErrorDetail> fieldErrors;

    @Getter
    @Builder
    @Schema(name = "FieldErrorDetail", description = "Detalhe de erro de validação em um campo")
    public static class FieldErrorDetail {

        @Schema(description = "Nome do campo com erro", example = "nome")
        private String field;

        @Schema(description = "Valor rejeitado", example = "")
        private Object rejectedValue;

        @Schema(description = "Mensagem de validação", example = "O nome do pet é obrigatório")
        private String message;
    }
}
