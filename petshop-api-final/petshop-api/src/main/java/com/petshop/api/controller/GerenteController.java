package com.petshop.api.controller;

import com.petshop.api.assembler.GerenteModelAssembler;
import com.petshop.api.dto.request.GerenteRequest;
import com.petshop.api.dto.response.GerenteResponse;
import com.petshop.api.service.GerenteService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "Gerentes",
     description = "Gerenciamento dos gerentes responsáveis pelas unidades do PetShop. "
             + "Relacionamento 1:1 com Unidade.")
@RestController
@RequestMapping("/api/v1/gerentes")
@RequiredArgsConstructor
public class GerenteController {

    private final GerenteService service;
    private final GerenteModelAssembler assembler;

    @Operation(summary = "Lista todos os gerentes")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<PagedModel<GerenteResponse>> listar(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<GerenteResponse> pagedAssembler) {
        return ResponseEntity.ok(
                pagedAssembler.toModel(service.listarTodos(pageable), assembler));
    }

    @Operation(summary = "Busca gerente por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Gerente encontrado"),
            @ApiResponse(responseCode = "404", description = "Gerente não encontrado",
                         content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<GerenteResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.buscarPorId(id)));
    }

    @Operation(summary = "Cadastra um novo gerente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Gerente cadastrado",
                         headers = @Header(name = "Location", description = "URI do novo recurso")),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "CPF já cadastrado")
    })
    @PostMapping
    public ResponseEntity<GerenteResponse> criar(@Valid @RequestBody GerenteRequest request) {
        GerenteResponse criado = service.criar(request);
        URI location = linkTo(methodOn(GerenteController.class).buscar(criado.getId())).toUri();
        return ResponseEntity.created(location).body(assembler.toModel(criado));
    }

    @Operation(summary = "Atualiza um gerente existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Gerente atualizado"),
            @ApiResponse(responseCode = "404", description = "Gerente não encontrado"),
            @ApiResponse(responseCode = "409", description = "CPF já pertence a outro gerente")
    })
    @PutMapping("/{id}")
    public ResponseEntity<GerenteResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody GerenteRequest request) {
        return ResponseEntity.ok(assembler.toModel(service.atualizar(id, request)));
    }

    @Operation(summary = "Remove um gerente pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Gerente removido"),
            @ApiResponse(responseCode = "404", description = "Gerente não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
