package com.petshop.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Dados para cadastrar ou atualizar um gerente")
public class GerenteRequest {

    @Schema(description = "Nome do gerente", example = "Carlos Ferreira")
    @NotBlank(message = "O nome do gerente é obrigatório")
    @Size(min = 2, max = 100)
    private String nome;

    @Schema(description = "CPF do gerente", example = "987.654.321-00")
    @NotBlank(message = "O CPF é obrigatório")
    @Size(min = 11, max = 14)
    private String cpf;

    @Schema(description = "E-mail corporativo", example = "carlos@petshop.com.br")
    @Email(message = "E-mail inválido")
    private String email;

    @Schema(description = "ID da unidade que este gerente irá administrar", example = "1")
    private Long unidadeId;
}
