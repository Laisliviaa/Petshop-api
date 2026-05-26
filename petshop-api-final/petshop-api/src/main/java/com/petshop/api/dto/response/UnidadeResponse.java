package com.petshop.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@Getter
@AllArgsConstructor
@Schema(description = "Dados da unidade retornados pela API")
public class UnidadeResponse extends RepresentationModel<UnidadeResponse> {

    @Schema(description = "ID da unidade", example = "1")
    private Long id;

    @Schema(description = "Nome da unidade", example = "PetShop Centro")
    private String nome;

    @Schema(description = "Endereço", example = "Rua das Flores, 100 — Centro, São Paulo/SP")
    private String endereco;

    @Schema(description = "Telefone", example = "(11) 3333-4444")
    private String telefone;

    @Schema(description = "Unidade ativa?", example = "true")
    private Boolean ativa;
}
