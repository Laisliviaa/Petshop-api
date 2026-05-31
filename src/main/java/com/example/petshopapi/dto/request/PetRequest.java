package com.example.petshopapi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Schema(description = "Dados para cadastrar ou atualizar um pet")
public class PetRequest {
    @Schema(example = "Rex") @NotBlank(message = "O nome do pet é obrigatório") private String nome;
    @Schema(example = "Cachorro") private String especie;
    @Schema(example = "1") @NotNull(message = "O ID do cliente é obrigatório") private Long clienteId;
}
