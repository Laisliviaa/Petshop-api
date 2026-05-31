package com.example.petshopapi.controller;

import com.example.petshopapi.assemblers.UnidadeModelAssembler;
import com.example.petshopapi.dto.request.UnidadeRequest;
import com.example.petshopapi.dto.response.UnidadeResponse;
import com.example.petshopapi.exception.ApiErrorResponse;
import com.example.petshopapi.exception.RecursoNaoEncontradoException;
import com.example.petshopapi.model.Unidade;
import com.example.petshopapi.service.UnidadeService;
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
@Tag(name = "Unidades", description = """
        Gerenciamento das unidades do PetShop. Demonstra relacionamento **One-to-One** com Gerente.

        **Autenticação:**
        - `GET` → público, não exige X-API-Key
        - `POST` / `PUT` → requer role **FUNCIONARIO** ou **ADMIN**
        - `DELETE` → requer role **ADMIN**
        """)
@RequestMapping("/api/v1/unidades")
public class UnidadeController {

    private final UnidadeService service;
    private final UnidadeModelAssembler assembler;
    private final PagedResourcesAssembler<UnidadeResponse> pagedAssembler;

    @Operation(summary = "Lista todas as unidades (paginado)",
               description = "Público. Suporta parâmetros `page`, `size`, `sort`.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "429",
            description = "Limite de requisições excedido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PagedModel<UnidadeResponse>> listar(@ParameterObject Pageable pageable) {
        Page<UnidadeResponse> page = service.listarTodos(pageable).map(this::toResponse);
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Busca unidade por ID", description = "Público.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Unidade encontrada"),
        @ApiResponse(responseCode = "400",
            description = "ID inválido (não numérico)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404",
            description = "Unidade não encontrada",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "429",
            description = "Limite de requisições excedido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UnidadeResponse> buscar(
            @Parameter(description = "ID da unidade", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(toResponse(
                service.buscarPorId(id)
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Unidade", id)))));
    }

    @Operation(summary = "Cria nova unidade",
               description = "Requer role **FUNCIONARIO** ou **ADMIN**.")
    @ApiResponses({
        @ApiResponse(responseCode = "201",
            description = "Unidade criada com sucesso",
            headers = @Header(name = "Location", description = "URI do recurso criado")),
        @ApiResponse(responseCode = "400",
            description = "Dados inválidos (nome em branco, etc.)",
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
    public ResponseEntity<UnidadeResponse> criar(
            @Parameter(hidden = true) @RequestHeader("X-API-Key") String apiKey,
            @Valid @RequestBody UnidadeRequest req) {
        Unidade nova = service.salvar(fromRequest(req));
        return ResponseEntity.created(URI.create("/api/v1/unidades/" + nova.getId()))
                .body(assembler.toModel(toResponse(nova)));
    }

    // CORRIGIDO: PUT com @ApiResponses completo
    @Operation(summary = "Atualiza unidade",
               description = "Requer role **FUNCIONARIO** ou **ADMIN**.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Unidade atualizada com sucesso"),
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
            description = "Unidade não encontrada",
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
    public ResponseEntity<UnidadeResponse> atualizar(
            @Parameter(hidden = true) @RequestHeader("X-API-Key") String apiKey,
            @Parameter(description = "ID da unidade a atualizar", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UnidadeRequest req) {
        Unidade e = service.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Unidade", id));
        e.setNome(req.getNome());
        e.setEndereco(req.getEndereco());
        return ResponseEntity.ok(assembler.toModel(toResponse(service.salvar(e))));
    }

    // CORRIGIDO: DELETE com @ApiResponses completo
    @Operation(summary = "Remove unidade",
               description = "Requer role **ADMIN**. "
                           + "Não é possível remover unidade com gerente ou agendamentos vinculados (retorna 409).")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Unidade removida com sucesso (sem corpo)"),
        @ApiResponse(responseCode = "401",
            description = "Header X-API-Key ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "403",
            description = "Apenas ADMIN pode excluir unidades",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404",
            description = "Unidade não encontrada",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "409",
            description = "Unidade possui gerente ou agendamentos vinculados — remova os vínculos primeiro",
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
            @Parameter(description = "ID da unidade a remover", example = "1", required = true)
            @PathVariable Long id) {
        service.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Unidade", id));
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private Unidade fromRequest(UnidadeRequest req) {
        Unidade u = new Unidade();
        u.setNome(req.getNome());
        u.setEndereco(req.getEndereco());
        return u;
    }

    private UnidadeResponse toResponse(Unidade u) {
        return new UnidadeResponse(u.getId(), u.getNome(), u.getEndereco());
    }
}
