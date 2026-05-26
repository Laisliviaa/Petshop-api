package com.petshop.api.controller;

import com.petshop.api.assembler.PetModelAssembler;
import com.petshop.api.dto.request.PetRequest;
import com.petshop.api.dto.response.PetResponse;
import com.petshop.api.model.PortePet;
import com.petshop.api.service.PetService;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "Pets",
     description = "Gerenciamento dos pets cadastrados no PetShop. "
             + "Filtros disponíveis por espécie, porte e cliente.")
@RestController
@RequestMapping("/api/v1/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService service;
    private final PetModelAssembler assembler;

    private final Map<String, PetResponse> idempotencyCache = new ConcurrentHashMap<>();

    @Operation(summary = "Lista todos os pets", description = "Suporta filtro por `especie` ou `porte`.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<PagedModel<PetResponse>> listar(
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) String especie,
            @RequestParam(required = false) PortePet porte,
            PagedResourcesAssembler<PetResponse> pagedAssembler) {

        Page<PetResponse> page;
        if (especie != null && !especie.isBlank()) {
            page = service.listarPorEspecie(especie, pageable);
        } else if (porte != null) {
            page = service.listarPorPorte(porte, pageable);
        } else {
            page = service.listarTodos(pageable);
        }
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Lista pets de um cliente específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<PagedModel<PetResponse>> listarPorCliente(
            @PathVariable Long clienteId,
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<PetResponse> pagedAssembler) {
        Page<PetResponse> page = service.listarPorCliente(clienteId, pageable);
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Busca pet por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pet encontrado"),
            @ApiResponse(responseCode = "404", description = "Pet não encontrado",
                         content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<PetResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.buscarPorId(id)));
    }

    @Operation(summary = "Cadastra um novo pet",
               description = "Suporta idempotência via `X-Idempotency-Key`.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pet cadastrado",
                         headers = @Header(name = "Location", description = "URI do novo recurso")),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Cliente (tutor) não encontrado")
    })
    @PostMapping
    public ResponseEntity<PetResponse> criar(
            @Valid @RequestBody PetRequest request,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {

        if (idempotencyKey != null && idempotencyCache.containsKey(idempotencyKey)) {
            PetResponse cached = idempotencyCache.get(idempotencyKey);
            return ResponseEntity.created(
                    linkTo(methodOn(PetController.class).buscar(cached.getId())).toUri()
            ).body(assembler.toModel(cached));
        }

        PetResponse criado = service.criar(request);
        PetResponse model  = assembler.toModel(criado);
        if (idempotencyKey != null) idempotencyCache.put(idempotencyKey, criado);

        URI location = linkTo(methodOn(PetController.class).buscar(criado.getId())).toUri();
        return ResponseEntity.created(location).body(model);
    }

    @Operation(summary = "Atualiza um pet existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pet atualizado"),
            @ApiResponse(responseCode = "404", description = "Pet ou cliente não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PetResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody PetRequest request) {
        return ResponseEntity.ok(assembler.toModel(service.atualizar(id, request)));
    }

    @Operation(summary = "Remove um pet pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pet removido"),
            @ApiResponse(responseCode = "404", description = "Pet não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
