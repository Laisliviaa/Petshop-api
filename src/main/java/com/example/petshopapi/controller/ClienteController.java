package com.example.petshopapi.controller;

import com.example.petshopapi.assemblers.ClienteModelAssembler;
import com.example.petshopapi.dto.request.ClienteRequest;
import com.example.petshopapi.dto.response.ClienteResponse;
import com.example.petshopapi.exception.ApiErrorResponse;
import com.example.petshopapi.exception.RecursoNaoEncontradoException;
import com.example.petshopapi.model.Cliente;
import com.example.petshopapi.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Clientes", description = """
        Gerenciamento de clientes do PetShop. Demonstra relacionamento **One-to-Many** com Pets.

        **Autenticação:**
        - `GET` → público, não exige X-API-Key
        - `POST` / `PUT` → requer role **FUNCIONARIO** ou **ADMIN**
        - `DELETE` → requer role **ADMIN**

        **Versionamento via header `X-API-Version`:**
        - `1` (padrão) → resposta com HATEOAS
        - `2` → resposta enriquecida com metadados de paginação
        """)
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ClienteService service;
    private final ClienteModelAssembler assembler;
    private final PagedResourcesAssembler<ClienteResponse> pagedAssembler;

    // ─── GET /api/v1/clientes ────────────────────────────────────────────────

    @Operation(
        summary = "Lista todos os clientes (paginado)",
        description = "Retorna lista paginada de clientes. Público — não exige X-API-Key. "
                    + "Suporta parâmetros de paginação: `page`, `size`, `sort` (ex: `sort=nome,asc`).",
        parameters = @Parameter(
            name = "X-API-Version", in = ParameterIn.HEADER,
            description = "Versão da API. `1` (padrão) retorna HATEOAS; `2` retorna metadados extras.",
            example = "1", required = false))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "400",
            description = "Versão de API inválida no header X-API-Version",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "429",
            description = "Limite de requisições excedido (10 GET por 30s por IP)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<?> listar(
            @RequestHeader(value = "X-API-Version", defaultValue = "1") String version,
            @ParameterObject Pageable pageable) {

        Page<ClienteResponse> page = service.listarTodos(pageable).map(this::toResponse);

        if ("2".equals(version)) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("apiVersion", "2");
            r.put("totalElements", page.getTotalElements());
            r.put("totalPages", page.getTotalPages());
            r.put("page", page.getNumber());
            r.put("size", page.getSize());
            r.put("content", page.getContent());
            return ResponseEntity.ok(r);
        }
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    // ─── GET /api/v1/clientes/{id} ───────────────────────────────────────────

    @Operation(summary = "Busca cliente por ID", description = "Público — não exige X-API-Key.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
        @ApiResponse(responseCode = "400",
            description = "ID inválido (não numérico)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404",
            description = "Cliente não encontrado para o ID informado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "429",
            description = "Limite de requisições excedido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscar(
            @Parameter(description = "ID do cliente", example = "1", required = true)
            @PathVariable Long id,
            @RequestHeader(value = "X-API-Version", defaultValue = "1") String version) {
        Cliente c = service.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
        return ResponseEntity.ok(assembler.toModel(toResponse(c)));
    }

    // ─── GET /api/v1/clientes/cpf/{cpf} ─────────────────────────────────────

    @Operation(summary = "Busca cliente por CPF",
               description = "Consulta personalizada. Público. Informe o CPF com 11 dígitos numéricos (sem pontos ou traço).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
        @ApiResponse(responseCode = "400",
            description = "Formato de CPF inválido (deve ter exatamente 11 dígitos)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404",
            description = "Nenhum cliente encontrado com o CPF informado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "429",
            description = "Limite de requisições excedido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<ClienteResponse> buscarPorCpf(
            @Parameter(description = "CPF do cliente (11 dígitos, sem pontos ou traço)",
                       example = "12345678901", required = true)
            @PathVariable String cpf) {
        Cliente c = service.buscarPorCpf(cpf)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente com CPF " + cpf));
        return ResponseEntity.ok(assembler.toModel(toResponse(c)));
    }

    // ─── GET /api/v1/clientes/busca?nome= ────────────────────────────────────

    @Operation(summary = "Busca clientes por nome (parcial)",
               description = "Consulta personalizada. Busca case-insensitive por nome contendo o termo informado. "
                           + "Público — não exige X-API-Key. Retorna lista vazia se nenhum resultado for encontrado.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada (pode ser vazia)"),
        @ApiResponse(responseCode = "400",
            description = "Parâmetro 'nome' não informado na query string",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "429",
            description = "Limite de requisições excedido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/busca")
    public ResponseEntity<CollectionModel<ClienteResponse>> buscarPorNome(
            @Parameter(description = "Fragmento do nome a pesquisar (case-insensitive)",
                       example = "Carlos", required = true)
            @RequestParam String nome) {
        List<ClienteResponse> lista = service.buscarPorNome(nome).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(lista,
                linkTo(methodOn(ClienteController.class).listar("1", null)).withRel("collection")));
    }

    // ─── POST /api/v1/clientes ───────────────────────────────────────────────

    @Operation(summary = "Cadastra novo cliente",
               description = "Requer role **FUNCIONARIO** ou **ADMIN**. "
                           + "O CPF deve ter 11 dígitos numéricos e ser único no sistema.")
    @ApiResponses({
        @ApiResponse(responseCode = "201",
            description = "Cliente criado com sucesso",
            headers = @Header(name = "Location",
                description = "URI do recurso criado, ex: /api/v1/clientes/7")),
        @ApiResponse(responseCode = "400",
            description = "Dados inválidos (nome em branco, CPF com formato incorreto, etc.)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401",
            description = "Header X-API-Key ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "403",
            description = "Role insuficiente (VISITANTE não pode criar)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "409",
            description = "CPF já cadastrado para outro cliente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "415",
            description = "Content-Type não suportado (use application/json)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "429",
            description = "Limite de requisições de escrita excedido (5 por 30s por IP)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ClienteResponse> criar(
            @Parameter(hidden = true) @RequestHeader("X-API-Key") String apiKey,
            @Valid @RequestBody ClienteRequest req) {
        Cliente novo = service.salvar(fromRequest(req));
        URI location = URI.create("/api/v1/clientes/" + novo.getId());
        return ResponseEntity.created(location).body(assembler.toModel(toResponse(novo)));
    }

    // ─── PUT /api/v1/clientes/{id} ───────────────────────────────────────────

    @Operation(summary = "Atualiza dados do cliente",
               description = "Requer role **FUNCIONARIO** ou **ADMIN**. "
                           + "Substitui todos os campos do cliente (operação completa).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso"),
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
            description = "Cliente não encontrado para o ID informado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "409",
            description = "CPF informado já pertence a outro cliente",
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
    public ResponseEntity<ClienteResponse> atualizar(
            @Parameter(hidden = true) @RequestHeader("X-API-Key") String apiKey,
            @Parameter(description = "ID do cliente a atualizar", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequest req) {
        Cliente existente = service.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
        existente.setNome(req.getNome());
        existente.setCpf(req.getCpf());
        return ResponseEntity.ok(assembler.toModel(toResponse(service.salvar(existente))));
    }

    // ─── DELETE /api/v1/clientes/{id} ────────────────────────────────────────

    @Operation(summary = "Remove um cliente",
               description = "Requer role **ADMIN**. "
                           + "Não é possível remover cliente que possua pets cadastrados (retorna 409).")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Cliente removido com sucesso (sem corpo)"),
        @ApiResponse(responseCode = "401",
            description = "Header X-API-Key ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "403",
            description = "Apenas ADMIN pode excluir clientes",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404",
            description = "Cliente não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "409",
            description = "Cliente possui pets vinculados — remova os pets primeiro",
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
            @Parameter(description = "ID do cliente a remover", example = "1", required = true)
            @PathVariable Long id) {
        service.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private Cliente fromRequest(ClienteRequest req) {
        Cliente c = new Cliente();
        c.setNome(req.getNome());
        c.setCpf(req.getCpf());
        return c;
    }

    private ClienteResponse toResponse(Cliente c) {
        return new ClienteResponse(c.getId(), c.getNome(), c.getCpf(),
                c.getPets() != null ? c.getPets().size() : 0);
    }
}
