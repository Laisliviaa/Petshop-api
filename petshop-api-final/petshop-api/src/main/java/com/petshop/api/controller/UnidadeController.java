package com.petshop.api.controller;

import com.petshop.api.assembler.UnidadeModelAssembler;
import com.petshop.api.dto.request.UnidadeRequest;
import com.petshop.api.dto.response.UnidadeResponse;
import com.petshop.api.service.UnidadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "Unidades",
     description = "Gerenciamento das filiais/unidades físicas do PetShop.")
@RestController
@RequestMapping("/api/v1/unidades")
@RequiredArgsConstructor
public class UnidadeController {

    private final UnidadeService service;
    private final UnidadeModelAssembler assembler;

    @Operation(summary = "Lista todas as unidades", description = "Use `?apenasAtivas=true` para filtrar somente unidades ativas.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<PagedModel<UnidadeResponse>> listar(
            @ParameterObject Pageable pageable,
            @RequestParam(required = false, defaultValue = "false") boolean apenasAtivas,
            PagedResourcesAssembler<UnidadeResponse> pagedAssembler) {

        Page<UnidadeResponse> page = apenasAtivas
                ? service.listarAtivas(pageable)
                : service.listarTodos(pageable);

        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Busca unidade por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unidade encontrada"),
            @ApiResponse(responseCode = "404", description = "Unidade não encontrada",
                         content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UnidadeResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.buscarPorId(id)));
    }

    @Operation(summary = "Cadastra uma nova unidade")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Unidade cadastrada",
                         headers = @Header(name = "Location", description = "URI do novo recurso")),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<UnidadeResponse> criar(@Valid @RequestBody UnidadeRequest request) {
        UnidadeResponse criada = service.criar(request);
        URI location = linkTo(methodOn(UnidadeController.class).buscar(criada.getId())).toUri();
        return ResponseEntity.created(location).body(assembler.toModel(criada));
    }

    @Operation(summary = "Atualiza uma unidade existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unidade atualizada"),
            @ApiResponse(responseCode = "404", description = "Unidade não encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UnidadeResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody UnidadeRequest request) {
        return ResponseEntity.ok(assembler.toModel(service.atualizar(id, request)));
    }

    @Operation(summary = "Remove uma unidade pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Unidade removida"),
            @ApiResponse(responseCode = "404", description = "Unidade não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
