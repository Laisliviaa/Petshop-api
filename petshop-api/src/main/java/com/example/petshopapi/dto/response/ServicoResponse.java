package com.example.petshopapi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@Getter @AllArgsConstructor
public class ServicoResponse extends RepresentationModel<ServicoResponse> {
    @Schema(example = "1") private Long id;
    @Schema(example = "Banho e Tosa") private String descricao;
    @Schema(example = "80.00") private Double preco;
}
