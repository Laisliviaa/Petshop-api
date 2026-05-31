package com.example.petshopapi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Schema(description = "Dados para cadastrar ou atualizar um gerente")
public class GerenteRequest {
    @Schema(example = "Maria Souza") @NotBlank(message = "O nome é obrigatório") private String nome;
    @Schema(example = "1") @NotNull(message = "O ID da unidade é obrigatório") private Long unidadeId;
}
