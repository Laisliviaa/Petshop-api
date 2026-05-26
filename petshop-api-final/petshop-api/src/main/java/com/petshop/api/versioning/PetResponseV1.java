package com.petshop.api.versioning;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Resposta simplificada dos pets para a API {@code v1}.
 * Retorna apenas os campos essenciais: id, nome e espécie.
 */
@Getter
@AllArgsConstructor
@Schema(description = "Resposta simplificada (v1): apenas id, nome e espécie")
public class PetResponseV1 {

    @Schema(description = "ID do pet", example = "1")
    private Long id;

    @Schema(description = "Nome do pet", example = "Thor")
    private String nome;

    @Schema(description = "Espécie do pet", example = "Cachorro")
    private String especie;
}
