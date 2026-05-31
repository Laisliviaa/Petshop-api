package com.example.petshopapi.controller;

import com.example.petshopapi.assemblers.PetModelAssembler;
import com.example.petshopapi.dto.request.PetRequest;
import com.example.petshopapi.dto.response.PetResponse;
import com.example.petshopapi.exception.ApiErrorResponse;
import com.example.petshopapi.exception.RecursoNaoEncontradoException;
import com.example.petshopapi.model.Cliente;
import com.example.petshopapi.model.Pet;
import com.example.petshopapi.service.PetService;
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
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Pets", description = """
        Gerenciamento de pets dos clientes. Demonstra relacionamentos **Many-to-One** com Cliente
        e **Many-to-Many** com Serviços.

        **Autenticação:**
        - `GET` → público, não exige X-API-Key
        - `POST` / `PUT` → requer role **FUNCIONARIO** ou **ADMIN**
        - `DELETE` → requer role **ADMIN**
        """)
@RequestMapping("/api/v1/pets")
public class PetController {

    private final PetService service;
    private final PetModelAssembler assembler;
    private final PagedResourcesAssembler<PetResponse> pagedAssembler;

    // ─── GET /api/v1/pets ────────────────────────────────────────────────────

    @Operation(summary = "Lista todos os pets (paginado)",
               description = "Público. Suporta parâmetros `page`, `size`, `sort`.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "429",
            description = "Limite de requisições excedido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PagedModel<PetResponse>> listar(@ParameterObject Pageable pageable) {
        Page<PetResponse> page = service.listarTodos(pageable).map(this::toResponse);
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    // ─── GET /api/v1/pets/{id} ───────────────────────────────────────────────

    @Operation(summary = "Busca pet por ID", description = "Público.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pet encontrado"),
        @ApiResponse(responseCode = "400",
            description = "ID inválido (não numérico)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404",
            description = "Pet não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "429",
            description = "Limite de requisições excedido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<PetResponse> buscar(
            @Parameter(description = "ID do pet", example = "1", required = true)
            @PathVariable Long id) {
        Pet p = service.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pet", id));
        return ResponseEntity.ok(assembler.toModel(toResponse(p)));
    }

    // ─── GET /api/v1/pets/busca?nome= ────────────────────────────────────────

    @Operation(summary = "Busca pets por nome (parcial)",
               description = "Consulta personalizada. Busca case-insensitive. Público. "
                           + "Retorna lista vazia se nenhum pet for encontrado.")
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
    public ResponseEntity<CollectionModel<PetResponse>> buscarPorNome(
            @Parameter(description = "Fragmento do nome do pet (case-insensitive)",
                       example = "Rex", required = true)
            @RequestParam String nome) {
        List<PetResponse> lista = service.buscarPorNome(nome).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(lista,
                linkTo(methodOn(PetController.class).listar(null)).withRel("collection")));
    }

    // ─── POST /api/v1/pets ───────────────────────────────────────────────────

    @Operation(summary = "Cadastra novo pet",
               description = "Requer role **FUNCIONARIO** ou **ADMIN**. "
                           + "O campo `clienteId` deve referenciar um cliente existente.")
    @ApiResponses({
        @ApiResponse(responseCode = "201",
            description = "Pet criado com sucesso",
            headers = @Header(name = "Location", description = "URI do recurso criado")),
        @ApiResponse(responseCode = "400",
            description = "Dados inválidos (nome em branco, clienteId nulo, etc.)",
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
        @ApiResponse(responseCode = "404",
            description = "Cliente não encontrado para o clienteId informado",
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
    public ResponseEntity<PetResponse> criar(
            @Parameter(hidden = true) @RequestHeader("X-API-Key") String apiKey,
            @Valid @RequestBody PetRequest req) {
        Cliente cliente = service.buscarCliente(req.getClienteId());
        Pet novo = service.salvar(fromRequest(req, cliente));
        return ResponseEntity.created(URI.create("/api/v1/pets/" + novo.getId()))
                .body(assembler.toModel(toResponse(novo)));
    }

    // ─── PUT /api/v1/pets/{id} ───────────────────────────────────────────────

    @Operation(summary = "Atualiza dados do pet",
               description = "Requer role **FUNCIONARIO** ou **ADMIN**.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pet atualizado com sucesso"),
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
            description = "Pet ou Cliente não encontrado",
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
    public ResponseEntity<PetResponse> atualizar(
            @Parameter(hidden = true) @RequestHeader("X-API-Key") String apiKey,
            @Parameter(description = "ID do pet a atualizar", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody PetRequest req) {
        Pet existente = service.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pet", id));
        Cliente cliente = service.buscarCliente(req.getClienteId());
        existente.setNome(req.getNome());
        existente.setEspecie(req.getEspecie());
        existente.setCliente(cliente);
        return ResponseEntity.ok(assembler.toModel(toResponse(service.salvar(existente))));
    }

    // ─── DELETE /api/v1/pets/{id} ────────────────────────────────────────────

    @Operation(summary = "Remove um pet",
               description = "Requer role **ADMIN**. "
                           + "Não é possível remover pet que possua agendamentos vinculados (retorna 409).")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Pet removido com sucesso (sem corpo)"),
        @ApiResponse(responseCode = "401",
            description = "Header X-API-Key ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "403",
            description = "Apenas ADMIN pode excluir pets",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404",
            description = "Pet não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "409",
            description = "Pet possui agendamentos vinculados — cancele os agendamentos primeiro",
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
            @Parameter(description = "ID do pet a remover", example = "1", required = true)
            @PathVariable Long id) {
        service.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Pet", id));
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private Pet fromRequest(PetRequest req, Cliente cliente) {
        Pet p = new Pet();
        p.setNome(req.getNome());
        p.setEspecie(req.getEspecie());
        p.setCliente(cliente);
        return p;
    }

    private PetResponse toResponse(Pet p) {
        return new PetResponse(
                p.getId(),
                p.getNome(),
                p.getEspecie(),
                p.getCliente() != null ? p.getCliente().getId()   : null,
                p.getCliente() != null ? p.getCliente().getNome() : null,
                p.getServicos() != null ? p.getServicos().size() : 0);
    }
}
