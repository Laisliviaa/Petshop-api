package com.example.petshopapi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@Getter @AllArgsConstructor
public class ClienteResponse extends RepresentationModel<ClienteResponse> {
    @Schema(example = "1") private Long id;
    @Schema(example = "Carlos Lima") private String nome;
    @Schema(example = "12345678901") private String cpf;
    @Schema(example = "2") private int totalPets;
}
