package com.petshop.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@Getter
@AllArgsConstructor
@Schema(description = "Dados do serviço retornados pela API")
public class ServicoResponse extends RepresentationModel<ServicoResponse> {

    @Schema(description = "ID do serviço", example = "1")
    private Long id;

    @Schema(description = "Descrição do serviço", example = "Banho e Tosa Completa")
    private String descricao;

    @Schema(description = "Preço", example = "85.00")
    private Double preco;

    @Schema(description = "Categoria", example = "Higiene")
    private String categoria;

    @Schema(description = "Total de pets que já contrataram este serviço", example = "12")
    private int totalPets;
}
