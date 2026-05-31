package com.example.petshopapi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
@Schema(description = "Dados de resposta de um gerente")
public class GerenteResponse extends RepresentationModel<GerenteResponse> {
    @Schema(example = "1")              private Long   id;
    @Schema(example = "Maria Souza")    private String nome;
    @Schema(example = "1")             private Long   unidadeId;
    @Schema(example = "Unidade Centro") private String nomeUnidade;
}
