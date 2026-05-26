package com.petshop.api.dto.request;

import com.petshop.api.model.PortePet;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Dados para cadastrar ou atualizar um pet")
public class PetRequest {

    @Schema(description = "Nome do pet", example = "Thor")
    @NotBlank(message = "O nome do pet é obrigatório")
    @Size(min = 1, max = 80)
    private String nome;

    @Schema(description = "Espécie do pet", example = "Cachorro")
    @NotBlank(message = "A espécie é obrigatória")
    @Size(max = 50)
    private String especie;

    @Schema(description = "Raça do pet", example = "Labrador")
    @Size(max = 80)
    private String raca;

    @Schema(description = "Porte do pet: MINI, PEQUENO, MEDIO, GRANDE ou GIGANTE", example = "GRANDE")
    private PortePet porte;

    @Schema(description = "ID do cliente (tutor) dono do pet", example = "1")
    @NotNull(message = "O ID do cliente é obrigatório")
    private Long clienteId;
}
