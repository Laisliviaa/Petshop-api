package com.example.petshopapi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@Getter @AllArgsConstructor
public class PetResponse extends RepresentationModel<PetResponse> {
    @Schema(example = "1") private Long id;
    @Schema(example = "Rex") private String nome;
    @Schema(example = "Cachorro") private String especie;
    @Schema(example = "Carlos Lima") private String nomeCliente;
    @Schema(example = "3") private int totalServicos;
}
