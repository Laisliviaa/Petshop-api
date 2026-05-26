package com.petshop.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Dados para cadastrar ou atualizar um cliente")
public class ClienteRequest {

    @Schema(description = "Nome completo do tutor", example = "Maria Oliveira")
    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 2, max = 100)
    private String nome;

    @Schema(description = "CPF do cliente (somente números ou com máscara)", example = "123.456.789-00")
    @NotBlank(message = "O CPF é obrigatório")
    @Size(min = 11, max = 14)
    private String cpf;

    @Schema(description = "E-mail do cliente", example = "maria@email.com")
    @Email(message = "E-mail inválido")
    private String email;

    @Schema(description = "Telefone de contato", example = "(11) 99999-0000")
    @Size(max = 20)
    private String telefone;
}
