package com.petshop.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Dados para cadastrar ou atualizar uma unidade do PetShop")
public class UnidadeRequest {

    @Schema(description = "Nome da unidade", example = "PetShop Centro")
    @NotBlank(message = "O nome da unidade é obrigatório")
    @Size(min = 2, max = 100)
    private String nome;

    @Schema(description = "Endereço completo", example = "Rua das Flores, 100 — Centro, São Paulo/SP")
    @NotBlank(message = "O endereço é obrigatório")
    @Size(max = 200)
    private String endereco;

    @Schema(description = "Telefone da unidade", example = "(11) 3333-4444")
    @Size(max = 20)
    private String telefone;

    @Schema(description = "Indica se a unidade está ativa", example = "true")
    private Boolean ativa;
}
