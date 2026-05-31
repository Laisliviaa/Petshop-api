package com.example.petshopapi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@Getter @AllArgsConstructor
public class UnidadeResponse extends RepresentationModel<UnidadeResponse> {
    @Schema(example = "1") private Long id;
    @Schema(example = "Unidade Centro") private String nome;
    @Schema(example = "Rua das Flores, 100") private String endereco;
}
