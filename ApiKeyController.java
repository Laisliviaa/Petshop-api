package com.example.petshopapi.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Corpo padrão de todas as respostas de erro da API.
 * Retornado em qualquer situação de erro (4xx / 5xx).
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Corpo padrão de todas as respostas de erro da API")
public class ApiErrorResponse {

    @Schema(description = "Data e hora do erro (ISO 8601)", example = "2026-06-10T09:00:00")
    private final LocalDateTime timestamp;

    @Schema(description = "Código HTTP do erro", example = "404")
    private final int status;

    @Schema(description = "Descrição curta do status HTTP", example = "Not Found")
    private final String erro;

    @Schema(description = "Mensagem descritiva do problema", example = "Cliente com ID 99 não encontrado.")
    private final String mensagem;

    @Schema(description = "URI do endpoint que gerou o erro", example = "/api/v1/clientes/99")
    private final String caminho;

    @Schema(description = "Método HTTP utilizado", example = "GET")
    private final String metodo;

    @Schema(description = "Lista de detalhes de validação (preenchida apenas em erros 400 de validação)",
            example = "[\"nome: O nome é obrigatório\", \"cpf: CPF deve ter 11 dígitos numéricos\"]")
    private final List<String> detalhes;
}
