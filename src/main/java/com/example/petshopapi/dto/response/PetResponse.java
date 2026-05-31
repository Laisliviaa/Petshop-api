package com.example.petshopapi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
@Schema(description = "Dados de resposta de um pet")
public class PetResponse extends RepresentationModel<PetResponse> {
    @Schema(example = "1")            private Long   id;
    @Schema(example = "Rex")          private String nome;
    @Schema(example = "Cachorro")     private String especie;
    @Schema(example = "1")            private Long   clienteId;
    @Schema(example = "Carlos Lima")  private String nomeCliente;
    @Schema(example = "3")            private int    totalServicos;
}
