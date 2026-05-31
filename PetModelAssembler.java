package com.example.petshopapi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Schema(description = "Dados para cadastrar ou atualizar um cliente")
public class ClienteRequest {
    @Schema(example = "Carlos Lima") @NotBlank(message = "O nome é obrigatório") private String nome;
    @Schema(example = "12345678901") @NotBlank(message = "O CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "CPF deve ter 11 dígitos numéricos (sem pontos ou traço)")
    private String cpf;
}
