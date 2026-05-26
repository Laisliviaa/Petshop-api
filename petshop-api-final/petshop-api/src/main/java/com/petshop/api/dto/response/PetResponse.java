package com.petshop.api.dto.response;

import com.petshop.api.model.PortePet;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@Getter
@AllArgsConstructor
@Schema(description = "Dados do pet retornados pela API")
public class PetResponse extends RepresentationModel<PetResponse> {

    @Schema(description = "ID do pet", example = "1")
    private Long id;

    @Schema(description = "Nome do pet", example = "Thor")
    private String nome;

    @Schema(description = "Espécie", example = "Cachorro")
    private String especie;

    @Schema(description = "Raça", example = "Labrador")
    private String raca;

    @Schema(description = "Porte do pet", example = "GRANDE")
    private PortePet porte;

    @Schema(description = "Nome do tutor", example = "Maria Oliveira")
    private String nomeCliente;

    @Schema(description = "Quantidade de serviços contratados", example = "3")
    private int totalServicos;
}
