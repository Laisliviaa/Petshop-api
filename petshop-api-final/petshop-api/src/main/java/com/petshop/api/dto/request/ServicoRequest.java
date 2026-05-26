package com.petshop.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Dados para cadastrar ou atualizar um serviço")
public class ServicoRequest {

    @Schema(description = "Descrição do serviço", example = "Banho e Tosa Completa")
    @NotBlank(message = "A descrição é obrigatória")
    @Size(min = 2, max = 100)
    private String descricao;

    @Schema(description = "Preço do serviço em reais", example = "85.00")
    @NotNull(message = "O preço é obrigatório")
    @Positive(message = "O preço deve ser maior que zero")
    private Double preco;

    @Schema(description = "Categoria do serviço", example = "Higiene")
    @Size(max = 50)
    private String categoria;
}
