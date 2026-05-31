package com.example.petshopapi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Schema(description = "Dados para cadastrar ou atualizar uma unidade")
public class UnidadeRequest {
    @Schema(example = "Unidade Centro") @NotBlank(message = "O nome é obrigatório") private String nome;
    @Schema(example = "Rua das Flores, 100") private String endereco;
}
