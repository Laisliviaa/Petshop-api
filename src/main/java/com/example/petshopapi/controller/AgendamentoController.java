package com.example.petshopapi.controller;

import com.example.petshopapi.assemblers.AgendamentoModelAssembler;
import com.example.petshopapi.dto.request.AgendamentoRequest;
import com.example.petshopapi.dto.response.AgendamentoResponse;
import com.example.petshopapi.exception.ApiErrorResponse;
import com.example.petshopapi.exception.RecursoNaoEncontradoException;
import com.example.petshopapi.model.*;
import com.example.petshopapi.service.AgendamentoService;
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
@Tag(name = "Agendamentos", description = """
        Gerenciamento de agendamentos de serviços para pets. Demonstra relacionamentos:
        - **Many-to-One** com Pet, Serviço e Unidade

        **Status possíveis:** `PENDENTE` (padrão), `CONCLUIDO`, `CANCELADO`

        **Autenticação:**
        - `GET` → público, não exige X-API-Key
        - `POST` / `PUT` → requer role **FUNCIONARIO** ou **ADMIN**
        - `DELETE` → requer role **ADMIN**
        """)
@RequestMapping("/api/v1/agendamentos")
public class AgendamentoController {

    private final AgendamentoService service;
    private final AgendamentoModelAssembler assembler;
    private final PagedResourcesAssembler<AgendamentoResponse> pagedAssembler;

    @Operation(summary = "Lista todos os agendamentos (paginado)",
               description = "Público. Suporta parâmetros `page`, `size`, `sort`.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "429",
            description = "Limite de requisições excedido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PagedModel<AgendamentoResponse>> listar(@ParameterObject Pageable pageable) {
        Page<AgendamentoResponse> page = service.listarTodos(pageable).map(this::toResponse);
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Busca agendamento por ID", description = "Público.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Agendamento encontrado"),
        @ApiResponse(responseCode = "400",
            description = "ID inválido (não numérico)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404",
            description = "Agendamento não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "429",
            description = "Limite de requisições excedido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoResponse> buscar(
            @Parameter(description = "ID do agendamento", example = "1", required = true)
            @PathVariable Long id) {
        Agendamento a = service.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento", id));
        return ResponseEntity.ok(assembler.toModel(toResponse(a)));
    }

    // CORRIGIDO: adicionado @ApiResponses com todos os erros possíveis
    @Operation(summary = "Busca agendamentos por status",
               description = "Consulta personalizada. Público. "
                           + "Valores aceitos: `PENDENTE`, `CONCLUIDO`, `CANCELADO`. "
                           + "Retorna lista vazia se nenhum agendamento tiver o status informado.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada (pode ser vazia)"),
        @ApiResponse(responseCode = "400",
            description = "Valor de status inválido (use PENDENTE, CONCLUIDO ou CANCELADO)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "429",
            description = "Limite de requisições excedido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<CollectionModel<AgendamentoResponse>> buscarPorStatus(
            @Parameter(description = "Status do agendamento",
                       example = "PENDENTE",
                       schema = @Schema(allowableValues = {"PENDENTE", "CONCLUIDO", "CANCELADO"}),
                       required = true)
            @PathVariable StatusAgendamento status) {
        List<AgendamentoResponse> lista = service.buscarPorStatus(status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(lista));
    }

    // CORRIGIDO: adicionado @ApiResponses
    @Operation(summary = "Busca agendamentos por unidade",
               description = "Consulta personalizada. Público. "
                           + "Retorna 404 se a unidade não existir.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada (pode ser vazia)"),
        @ApiResponse(responseCode = "400",
            description = "ID inválido (não numérico)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404",
            description = "Unidade não encontrada para o ID informado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "429",
            description = "Limite de requisições excedido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/unidade/{unidadeId}")
    public ResponseEntity<CollectionModel<AgendamentoResponse>> buscarPorUnidade(
            @Parameter(description = "ID da unidade", example = "1", required = true)
            @PathVariable Long unidadeId) {
        List<AgendamentoResponse> lista = service.buscarPorUnidade(unidadeId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(lista));
    }

    // CORRIGIDO: adicionado @ApiResponses
    @Operation(summary = "Busca agendamentos por pet",
               description = "Consulta personalizada. Público. "
                           + "Retorna 404 se o pet não existir.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada (pode ser vazia)"),
        @ApiResponse(responseCode = "400",
            description = "ID inválido (não numérico)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404",
            description = "Pet não encontrado para o ID informado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "429",
            description = "Limite de requisições excedido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/pet/{petId}")
    public ResponseEntity<CollectionModel<AgendamentoResponse>> buscarPorPet(
            @Parameter(description = "ID do pet", example = "1", required = true)
            @PathVariable Long petId) {
        List<AgendamentoResponse> lista = service.buscarPorPet(petId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(lista));
    }

    @Operation(summary = "Cria novo agendamento",
               description = """
                       Requer role **FUNCIONARIO** ou **ADMIN**.

                       Informe no body:
                       - `petId` — ID do pet
                       - `servicoId` — ID do serviço
                       - `unidadeId` — ID da unidade
                       - `dataHora` — formato ISO 8601: `2026-06-10T09:00:00`
                       - `status` — `PENDENTE` (padrão), `CONCLUIDO` ou `CANCELADO`
                       - `observacoes` — campo livre, opcional
                       """)
    @ApiResponses({
        @ApiResponse(responseCode = "201",
            description = "Agendamento criado com sucesso",
            headers = @Header(name = "Location", description = "URI do recurso criado")),
        @ApiResponse(responseCode = "400",
            description = "Dados inválidos (campos obrigatórios ausentes, formato de data incorreto, etc.)",
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
            description = "Pet, Serviço ou Unidade não encontrado para o ID informado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "415",
            description = "Content-Type não suportado (use application/json)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "422",
            description = "Violação de regra de negócio: data no passado ou reagendamento de cancelado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "429",
            description = "Limite de requisições de escrita excedido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<AgendamentoResponse> criar(
            @Parameter(hidden = true) @RequestHeader("X-API-Key") String apiKey,
            @Valid @RequestBody AgendamentoRequest req) {
        Pet pet         = service.buscarPet(req.getPetId());
        Servico servico = service.buscarServico(req.getServicoId());
        Unidade unidade = service.buscarUnidade(req.getUnidadeId());
        Agendamento novo = service.salvar(fromRequest(req, pet, servico, unidade));
        return ResponseEntity.created(URI.create("/api/v1/agendamentos/" + novo.getId()))
                .body(assembler.toModel(toResponse(novo)));
    }

    @Operation(summary = "Atualiza agendamento",
               description = "Requer role **FUNCIONARIO** ou **ADMIN**.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Agendamento atualizado com sucesso"),
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
            description = "Agendamento, Pet, Serviço ou Unidade não encontrado",
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
    public ResponseEntity<AgendamentoResponse> atualizar(
            @Parameter(hidden = true) @RequestHeader("X-API-Key") String apiKey,
            @Parameter(description = "ID do agendamento a atualizar", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody AgendamentoRequest req) {
        Agendamento existente = service.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento", id));
        existente.setPet(service.buscarPet(req.getPetId()));
        existente.setServico(service.buscarServico(req.getServicoId()));
        existente.setUnidade(service.buscarUnidade(req.getUnidadeId()));
        existente.setDataHora(req.getDataHora());
        existente.setStatus(req.getStatus() != null ? req.getStatus() : existente.getStatus());
        existente.setObservacoes(req.getObservacoes());
        return ResponseEntity.ok(assembler.toModel(toResponse(service.salvar(existente))));
    }

    @Operation(summary = "Remove agendamento", description = "Requer role **ADMIN**.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Agendamento removido com sucesso (sem corpo)"),
        @ApiResponse(responseCode = "401",
            description = "Header X-API-Key ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "403",
            description = "Apenas ADMIN pode excluir agendamentos",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404",
            description = "Agendamento não encontrado",
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
            @Parameter(description = "ID do agendamento a remover", example = "1", required = true)
            @PathVariable Long id) {
        service.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento", id));
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private Agendamento fromRequest(AgendamentoRequest req, Pet pet, Servico servico, Unidade unidade) {
        Agendamento a = new Agendamento();
        a.setPet(pet);
        a.setServico(servico);
        a.setUnidade(unidade);
        a.setDataHora(req.getDataHora());
        a.setStatus(req.getStatus() != null ? req.getStatus() : StatusAgendamento.PENDENTE);
        a.setObservacoes(req.getObservacoes());
        return a;
    }

    private AgendamentoResponse toResponse(Agendamento a) {
        return new AgendamentoResponse(
                a.getId(),
                a.getPet()     != null ? a.getPet().getNome()          : null,
                a.getServico() != null ? a.getServico().getDescricao() : null,
                a.getServico() != null ? a.getServico().getPreco()     : null,
                a.getUnidade() != null ? a.getUnidade().getNome()      : null,
                a.getDataHora(), a.getStatus(), a.getObservacoes());
    }
}
