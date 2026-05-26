package com.petshop.api.controller;

import com.petshop.api.assembler.ClienteModelAssembler;
import com.petshop.api.dto.request.ClienteRequest;
import com.petshop.api.dto.response.ClienteResponse;
import com.petshop.api.service.ClienteService;
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

@Tag(name = "Clientes",
     description = "Gerenciamento dos tutores (clientes) do PetShop. "
             + "Cada cliente pode ter múltiplos pets cadastrados.")
@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService service;
    private final ClienteModelAssembler assembler;

    // ── Idempotência ─────────────────────────────────────────────────────────
    private final Map<String, ClienteResponse> idempotencyCache = new ConcurrentHashMap<>();

    // ── GET all ───────────────────────────────────────────────────────────────

    @Operation(summary = "Lista todos os clientes",
               description = "Retorna página de clientes com links HATEOAS. "
                       + "Use `?nome=` para filtrar por nome.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "429", description = "Limite de requisições excedido")
    })
    @GetMapping
    public ResponseEntity<PagedModel<ClienteResponse>> listar(
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) String nome,
            PagedResourcesAssembler<ClienteResponse> pagedAssembler) {

        Page<ClienteResponse> page = nome != null && !nome.isBlank()
                ? service.buscarPorNome(nome, pageable)
                : service.listarTodos(pageable);

        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    // ── GET by id ─────────────────────────────────────────────────────────────

    @Operation(summary = "Busca cliente por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                         content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.buscarPorId(id)));
    }

    // ── POST ──────────────────────────────────────────────────────────────────

    @Operation(summary = "Cadastra um novo cliente",
               description = "Suporta idempotência via cabeçalho `X-Idempotency-Key`. "
                       + "Envie o mesmo valor em retentativas para evitar duplicações.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente cadastrado",
                         headers = @Header(name = "Location", description = "URI do novo recurso")),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "CPF já cadastrado")
    })
    @PostMapping
    public ResponseEntity<ClienteResponse> criar(
            @Valid @RequestBody ClienteRequest request,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {

        // Idempotência: retorna resultado anterior se chave já foi processada
        if (idempotencyKey != null && idempotencyCache.containsKey(idempotencyKey)) {
            ClienteResponse cached = idempotencyCache.get(idempotencyKey);
            return ResponseEntity.created(
                    linkTo(methodOn(ClienteController.class).buscar(cached.getId())).toUri()
            ).body(assembler.toModel(cached));
        }

        ClienteResponse criado = service.criar(request);
        ClienteResponse model  = assembler.toModel(criado);

        if (idempotencyKey != null) {
            idempotencyCache.put(idempotencyKey, criado);
        }

        URI location = linkTo(methodOn(ClienteController.class).buscar(criado.getId())).toUri();
        return ResponseEntity.created(location).body(model);
    }

    // ── PUT ───────────────────────────────────────────────────────────────────

    @Operation(summary = "Atualiza um cliente existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente atualizado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
            @ApiResponse(responseCode = "409", description = "CPF já pertence a outro cliente")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(assembler.toModel(service.atualizar(id, request)));
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Operation(summary = "Remove um cliente pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente removido"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
