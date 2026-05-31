package com.example.petshopapi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@Getter @AllArgsConstructor
public class GerenteResponse extends RepresentationModel<GerenteResponse> {
    @Schema(example = "1") private Long id;
    @Schema(example = "Maria Souza") private String nome;
    @Schema(example = "Unidade Centro") private String nomeUnidade;
}
