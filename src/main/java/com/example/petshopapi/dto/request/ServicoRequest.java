package com.example.petshopapi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Schema(description = "Dados para cadastrar ou atualizar um serviço")
public class ServicoRequest {
    @Schema(example = "Banho e Tosa") @NotBlank(message = "A descrição é obrigatória") private String descricao;
    @Schema(example = "80.00") @NotNull @Positive(message = "O preço deve ser maior que zero") private Double preco;
}
