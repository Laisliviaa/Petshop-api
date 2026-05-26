package com.petshop.api.controller;

import com.petshop.api.assembler.ServicoModelAssembler;
import com.petshop.api.dto.request.ServicoRequest;
import com.petshop.api.dto.response.ServicoResponse;
import com.petshop.api.service.ServicoService;
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

@Tag(name = "Serviços",
     description = "Catálogo de serviços oferecidos pelo PetShop: banho, tosa, consulta veterinária, etc.")
@RestController
@RequestMapping("/api/v1/servicos")
@RequiredArgsConstructor
public class ServicoController {

    private final ServicoService service;
    private final ServicoModelAssembler assembler;

    private final Map<String, ServicoResponse> idempotencyCache = new ConcurrentHashMap<>();

    @Operation(summary = "Lista todos os serviços", description = "Filtre por `categoria` (ex: Higiene, Veterinário).")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<PagedModel<ServicoResponse>> listar(
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) String categoria,
            PagedResourcesAssembler<ServicoResponse> pagedAssembler) {

        Page<ServicoResponse> page = categoria != null && !categoria.isBlank()
                ? service.listarPorCategoria(categoria, pageable)
                : service.listarTodos(pageable);

        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Busca serviço por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço encontrado"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado",
                         content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.buscarPorId(id)));
    }

    @Operation(summary = "Cadastra um novo serviço",
               description = "Suporta idempotência via `X-Idempotency-Key`.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Serviço cadastrado",
                         headers = @Header(name = "Location", description = "URI do novo recurso")),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Descrição já cadastrada")
    })
    @PostMapping
    public ResponseEntity<ServicoResponse> criar(
            @Valid @RequestBody ServicoRequest request,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {

        if (idempotencyKey != null && idempotencyCache.containsKey(idempotencyKey)) {
            ServicoResponse cached = idempotencyCache.get(idempotencyKey);
            return ResponseEntity.created(
                    linkTo(methodOn(ServicoController.class).buscar(cached.getId())).toUri()
            ).body(assembler.toModel(cached));
        }

        ServicoResponse criado = service.criar(request);
        ServicoResponse model  = assembler.toModel(criado);
        if (idempotencyKey != null) idempotencyCache.put(idempotencyKey, criado);

        URI location = linkTo(methodOn(ServicoController.class).buscar(criado.getId())).toUri();
        return ResponseEntity.created(location).body(model);
    }

    @Operation(summary = "Atualiza um serviço existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço atualizado"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ServicoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ServicoRequest request) {
        return ResponseEntity.ok(assembler.toModel(service.atualizar(id, request)));
    }

    @Operation(summary = "Remove um serviço pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Serviço removido"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
