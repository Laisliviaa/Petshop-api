package com.petshop.api.apikey;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
@Schema(description = "Resposta com a chave de API gerada")
public class ApiKeyResponse {

    @Schema(description = "ID da chave", example = "1")
    private Long id;

    @Schema(description = "Chave de API gerada. Guarde-a com segurança!", example = "pk_abc123...")
    private String chave;

    @Schema(description = "Nome do cliente associado", example = "App Mobile PetShop")
    private String nomeCliente;

    @Schema(description = "Data de criação da chave")
    private Instant criadaEm;

    @Schema(description = "Indica se a chave está ativa")
    private boolean ativa;
}
