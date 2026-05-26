package com.petshop.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@Getter
@AllArgsConstructor
@Schema(description = "Dados do gerente retornados pela API")
public class GerenteResponse extends RepresentationModel<GerenteResponse> {

    @Schema(description = "ID do gerente", example = "1")
    private Long id;

    @Schema(description = "Nome", example = "Carlos Ferreira")
    private String nome;

    @Schema(description = "CPF", example = "987.654.321-00")
    private String cpf;

    @Schema(description = "E-mail", example = "carlos@petshop.com.br")
    private String email;

    @Schema(description = "Nome da unidade gerenciada", example = "PetShop Centro")
    private String unidade;
}
