package com.petshop.api.apikey;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Requisição para geração de uma nova chave de API")
public class ApiKeyRequest {

    @NotBlank(message = "O nome do cliente é obrigatório")
    @Schema(description = "Nome do sistema ou cliente que utilizará a chave", example = "App Mobile PetShop")
    private String nomeCliente;
}
