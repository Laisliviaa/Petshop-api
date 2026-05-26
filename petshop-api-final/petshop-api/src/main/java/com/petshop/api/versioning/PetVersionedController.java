package com.petshop.api.versioning;

import com.petshop.api.assembler.PetModelAssembler;
import com.petshop.api.dto.response.PetResponse;
import com.petshop.api.exception.RecursoNaoEncontradoException;
import com.petshop.api.model.Pet;
import com.petshop.api.repository.PetRepository;
import com.petshop.api.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Demonstração de versionamento via cabeçalho {@code X-API-Version}.
 *
 * <pre>
 * v1 → /api/versioned/v1/pets  (resposta simplificada: id, nome, espécie)
 * v2 → /api/versioned/v2/pets  (resposta completa com HATEOAS)
 * </pre>
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Pets — Versionamento",
     description = "Endpoints versionados para demonstração de X-API-Version. "
             + "**v1** retorna resposta simplificada · **v2** retorna resposta completa com HATEOAS. "
             + "Prefixo `/versioned` evita conflito com os endpoints principais.")
public class PetVersionedController {

    private final PetRepository     petRepository;
    private final PetService        petService;
    private final PetModelAssembler assembler;

    // ── v1 — simplificado ────────────────────────────────────────────────────

    @Operation(
        summary     = "[v1] Listar Pets — resposta simplificada",
        description = "Retorna apenas `id`, `nome` e `espécie`. Envie `X-API-Version: v1`.",
        parameters  = @Parameter(name = "X-API-Version", in = ParameterIn.HEADER,
                                 description = "Versão da API", example = "v1", required = false)
    )
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping("/api/versioned/v1/pets")
    public ResponseEntity<Page<PetResponseV1>> listV1(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok()
                .header("X-API-Version", "v1")
                .body(petRepository.findAll(pageable)
                        .map(p -> new PetResponseV1(p.getId(), p.getNome(), p.getEspecie())));
    }

    @Operation(
        summary     = "[v1] Buscar Pet por ID — resposta simplificada",
        description = "Retorna apenas `id`, `nome` e `espécie`. Envie `X-API-Version: v1`."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pet encontrado"),
        @ApiResponse(responseCode = "404", description = "Pet não encontrado",
                     content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/api/versioned/v1/pets/{id}")
    public ResponseEntity<PetResponseV1> getByIdV1(@PathVariable Long id) {
        Pet p = petRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pet", id));
        return ResponseEntity.ok()
                .header("X-API-Version", "v1")
                .body(new PetResponseV1(p.getId(), p.getNome(), p.getEspecie()));
    }

    // ── v2 — completo com HATEOAS ─────────────────────────────────────────────

    @Operation(
        summary     = "[v2] Listar Pets — resposta completa com HATEOAS",
        description = "Retorna todos os campos + links HATEOAS. Envie `X-API-Version: v2`."
    )
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping("/api/versioned/v2/pets")
    public ResponseEntity<PagedModel<PetResponse>> listV2(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<PetResponse> pagedAssembler) {

        Page<PetResponse> page = petRepository.findAll(pageable)
                .map(petService::toResponse);
        return ResponseEntity.ok()
                .header("X-API-Version", "v2")
                .body(pagedAssembler.toModel(page, assembler));
    }

    @Operation(
        summary     = "[v2] Buscar Pet por ID — resposta completa com HATEOAS",
        description = "Retorna todos os campos + links HATEOAS. Envie `X-API-Version: v2`."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pet encontrado"),
        @ApiResponse(responseCode = "404", description = "Pet não encontrado",
                     content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/api/versioned/v2/pets/{id}")
    public ResponseEntity<PetResponse> getByIdV2(@PathVariable Long id) {
        Pet p = petRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pet", id));
        return ResponseEntity.ok()
                .header("X-API-Version", "v2")
                .body(assembler.toModel(petService.toResponse(p)));
    }
}
