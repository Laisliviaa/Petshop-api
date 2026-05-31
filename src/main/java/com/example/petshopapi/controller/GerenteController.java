package com.example.petshopapi.controller;

import com.example.petshopapi.assemblers.GerenteModelAssembler;
import com.example.petshopapi.dto.request.GerenteRequest;
import com.example.petshopapi.dto.response.GerenteResponse;
import com.example.petshopapi.exception.ApiErrorResponse;
import com.example.petshopapi.exception.RecursoNaoEncontradoException;
import com.example.petshopapi.model.Gerente;
import com.example.petshopapi.model.Unidade;
import com.example.petshopapi.service.GerenteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@RestController
@RequiredArgsConstructor
@Tag(name = "Gerentes", description = """
        Gerenciamento dos gerentes das unidades. Demonstra relacionamento **One-to-One** com Unidade.

        Cada unidade pode ter **no máximo um gerente**. Tentativas de associar dois gerentes à
        mesma unidade resultam em erro 409.

        **Autenticação:**
        - `GET` → público, não exige X-API-Key
        - `POST` / `PUT` → requer role **FUNCIONARIO** ou **ADMIN**
        - `DELETE` → requer role **ADMIN**
        """)
@RequestMapping("/api/v1/gerentes")
public class GerenteController {

    private final GerenteService service;
    private final GerenteModelAssembler assembler;
    private final PagedResourcesAssembler<GerenteResponse> pagedAssembler;

    @Operation(summary = "Lista todos os gerentes (paginado)",
               description = "Público. Suporta parâmetros `page`, `size`, `sort`.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "429",
            description = "Limite de requisições excedido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PagedModel<GerenteResponse>> listar(@ParameterObject Pageable pageable) {
        Page<GerenteResponse> page = service.listarTodos(pageable).map(this::toResponse);
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Busca gerente por ID", description = "Público.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Gerente encontrado"),
        @ApiResponse(responseCode = "400",
            description = "ID inválido (não numérico)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404",
            description = "Gerente não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "429",
            description = "Limite de requisições excedido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<GerenteResponse> buscar(
            @Parameter(description = "ID do gerente", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(toResponse(
                service.buscarPorId(id)
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Gerente", id)))));
    }

    @Operation(summary = "Cria novo gerente",
               description = "Requer role **FUNCIONARIO** ou **ADMIN**. "
                           + "Cada unidade admite **no máximo um gerente** (One-to-One).")
    @ApiResponses({
        @ApiResponse(responseCode = "201",
            description = "Gerente criado com sucesso",
            headers = @Header(name = "Location", description = "URI do recurso criado")),
        @ApiResponse(responseCode = "400",
            description = "Dados inválidos (nome em branco, unidadeId nulo, etc.)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401",
            description = "Header X-API-Key ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "403",
            description = "Role insuficiente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404",
            description = "Unidade não encontrada para o unidadeId informado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "409",
            description = "A unidade informada já possui um gerente cadastrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "415",
            description = "Content-Type não suportado (use application/json)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "429",
            description = "Limite de requisições de escrita excedido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<GerenteResponse> criar(
            @Parameter(hidden = true) @RequestHeader("X-API-Key") String apiKey,
            @Valid @RequestBody GerenteRequest req) {
        Unidade unidade = service.buscarUnidade(req.getUnidadeId());
        Gerente novo = service.salvar(fromRequest(req, unidade));
        return ResponseEntity.created(URI.create("/api/v1/gerentes/" + novo.getId()))
                .body(assembler.toModel(toResponse(novo)));
    }

    // CORRIGIDO: PUT com @ApiResponses completo
    @Operation(summary = "Atualiza gerente",
               description = "Requer role **FUNCIONARIO** ou **ADMIN**. "
                           + "Se alterar a unidade, a nova unidade não pode já ter outro gerente.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Gerente atualizado com sucesso"),
        @ApiResponse(responseCode = "400",
            description = "Dados inválidos",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401",
            description = "Header X-API-Key ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "403",
            description = "Role insuficiente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404",
            description = "Gerente ou Unidade não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "409",
            description = "A unidade informada já está associada a outro gerente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "415",
            description = "Content-Type não suportado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "429",
            description = "Limite de requisições excedido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<GerenteResponse> atualizar(
            @Parameter(hidden = true) @RequestHeader("X-API-Key") String apiKey,
            @Parameter(description = "ID do gerente a atualizar", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody GerenteRequest req) {
        Gerente e = service.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Gerente", id));
        Unidade unidade = service.buscarUnidade(req.getUnidadeId());
        e.setNome(req.getNome());
        e.setUnidade(unidade);
        return ResponseEntity.ok(assembler.toModel(toResponse(service.salvar(e))));
    }

    // CORRIGIDO: DELETE com @ApiResponses completo
    @Operation(summary = "Remove gerente", description = "Requer role **ADMIN**.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Gerente removido com sucesso (sem corpo)"),
        @ApiResponse(responseCode = "401",
            description = "Header X-API-Key ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "403",
            description = "Apenas ADMIN pode excluir gerentes",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404",
            description = "Gerente não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "429",
            description = "Limite de requisições excedido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @Parameter(hidden = true) @RequestHeader("X-API-Key") String apiKey,
            @Parameter(description = "ID do gerente a remover", example = "1", required = true)
            @PathVariable Long id) {
        service.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Gerente", id));
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private Gerente fromRequest(GerenteRequest req, Unidade u) {
        Gerente g = new Gerente();
        g.setNome(req.getNome());
        g.setUnidade(u);
        return g;
    }

    private GerenteResponse toResponse(Gerente g) {
        return new GerenteResponse(g.getId(), g.getNome(),
                g.getUnidade() != null ? g.getUnidade().getNome() : null);
    }
}
