package com.petshop.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@Getter
@AllArgsConstructor
@Schema(description = "Dados do cliente retornados pela API")
public class ClienteResponse extends RepresentationModel<ClienteResponse> {

    @Schema(description = "ID do cliente", example = "1")
    private Long id;

    @Schema(description = "Nome completo", example = "Maria Oliveira")
    private String nome;

    @Schema(description = "CPF", example = "123.456.789-00")
    private String cpf;

    @Schema(description = "E-mail", example = "maria@email.com")
    private String email;

    @Schema(description = "Telefone", example = "(11) 99999-0000")
    private String telefone;

    @Schema(description = "Quantidade de pets cadastrados", example = "2")
    private int totalPets;
}
