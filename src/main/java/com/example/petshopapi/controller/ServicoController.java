package com.example.petshopapi.controller;

import com.example.petshopapi.assemblers.ServicoModelAssembler;
import com.example.petshopapi.dto.request.ServicoRequest;
import com.example.petshopapi.dto.response.ServicoResponse;
import com.example.petshopapi.exception.ApiErrorResponse;
import com.example.petshopapi.exception.RecursoNaoEncontradoException;
import com.example.petshopapi.model.Servico;
import com.example.petshopapi.service.ServicoService;
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

@RestController
@RequiredArgsConstructor
@Tag(name = "Serviços", description = """
        Gerenciamento dos serviços oferecidos pelo PetShop. Demonstra relacionamento **Many-to-Many** com Pets.

        **Autenticação:**
        - `GET` → público, não exige X-API-Key
        - `POST` / `PUT` → requer role **FUNCIONARIO** ou **ADMIN**
        - `DELETE` → requer role **ADMIN**
        """)
@RequestMapping("/api/v1/servicos")
public class ServicoController {

    private final ServicoService service;
    private final ServicoModelAssembler assembler;
    private final PagedResourcesAssembler<ServicoResponse> pagedAssembler;

    @Operation(summary = "Lista todos os serviços (paginado)",
               description = "Público. Suporta parâmetros `page`, `size`, `sort`.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "429",
            description = "Limite de requisições excedido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PagedModel<ServicoResponse>> listar(@ParameterObject Pageable pageable) {
        Page<ServicoResponse> page = service.listarTodos(pageable).map(this::toResponse);
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Busca serviço por ID", description = "Público.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Serviço encontrado"),
        @ApiResponse(responseCode = "400",
            description = "ID inválido (não numérico)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404",
            description = "Serviço não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "429",
            description = "Limite de requisições excedido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponse> buscar(
            @Parameter(description = "ID do serviço", example = "1", required = true)
            @PathVariable Long id) {
        Servico s = service.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Servico", id));
        return ResponseEntity.ok(assembler.toModel(toResponse(s)));
    }

    @Operation(summary = "Busca serviços por descrição (parcial)",
               description = "Consulta personalizada. Busca case-insensitive. Público. "
                           + "Retorna lista vazia se nenhum serviço for encontrado.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada (pode ser vazia)"),
        @ApiResponse(responseCode = "400",
            description = "Parâmetro 'descricao' não informado na query string",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "429",
            description = "Limite de requisições excedido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/busca")
    public ResponseEntity<CollectionModel<ServicoResponse>> buscarPorDescricao(
            @Parameter(description = "Fragmento da descrição do serviço (case-insensitive)",
                       example = "Banho", required = true)
            @RequestParam String descricao) {
        List<ServicoResponse> lista = service.buscarPorDescricao(descricao).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(lista));
    }

    @Operation(summary = "Cria novo serviço",
               description = "Requer role **FUNCIONARIO** ou **ADMIN**. "
                           + "O preço deve ser maior que zero.")
    @ApiResponses({
        @ApiResponse(responseCode = "201",
            description = "Serviço criado com sucesso",
            headers = @Header(name = "Location", description = "URI do recurso criado")),
        @ApiResponse(responseCode = "400",
            description = "Dados inválidos (descrição em branco, preço inválido, etc.)",
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
    public ResponseEntity<ServicoResponse> criar(
            @Parameter(hidden = true) @RequestHeader("X-API-Key") String apiKey,
            @Valid @RequestBody ServicoRequest req) {
        Servico novo = service.salvar(fromRequest(req));
        return ResponseEntity.created(URI.create("/api/v1/servicos/" + novo.getId()))
                .body(assembler.toModel(toResponse(novo)));
    }

    // CORRIGIDO: PUT agora tem @ApiResponses completo
    @Operation(summary = "Atualiza serviço",
               description = "Requer role **FUNCIONARIO** ou **ADMIN**.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Serviço atualizado com sucesso"),
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
            description = "Serviço não encontrado",
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
    public ResponseEntity<ServicoResponse> atualizar(
            @Parameter(hidden = true) @RequestHeader("X-API-Key") String apiKey,
            @Parameter(description = "ID do serviço a atualizar", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ServicoRequest req) {
        Servico existente = service.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Servico", id));
        existente.setDescricao(req.getDescricao());
        existente.setPreco(req.getPreco());
        return ResponseEntity.ok(assembler.toModel(toResponse(service.salvar(existente))));
    }

    @Operation(summary = "Remove serviço",
               description = "Requer role **ADMIN**. "
                           + "Não é possível remover serviço vinculado a pets ou agendamentos (retorna 409).")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Serviço removido com sucesso (sem corpo)"),
        @ApiResponse(responseCode = "401",
            description = "Header X-API-Key ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "403",
            description = "Apenas ADMIN pode excluir serviços",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404",
            description = "Serviço não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "409",
            description = "Serviço vinculado a pets ou agendamentos — remova os vínculos primeiro",
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
            @Parameter(description = "ID do serviço a remover", example = "1", required = true)
            @PathVariable Long id) {
        service.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Servico", id));
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private Servico fromRequest(ServicoRequest req) {
        Servico s = new Servico();
        s.setDescricao(req.getDescricao());
        s.setPreco(req.getPreco());
        return s;
    }

    private ServicoResponse toResponse(Servico s) {
        return new ServicoResponse(s.getId(), s.getDescricao(), s.getPreco());
    }
}
