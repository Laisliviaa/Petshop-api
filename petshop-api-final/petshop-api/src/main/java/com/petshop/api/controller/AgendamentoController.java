package com.petshop.api.controller;

import com.petshop.api.assembler.AgendamentoModelAssembler;
import com.petshop.api.dto.request.AgendamentoRequest;
import com.petshop.api.dto.response.AgendamentoResponse;
import com.petshop.api.model.StatusAgendamento;
import com.petshop.api.service.AgendamentoService;
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

@Tag(name = "Agendamentos",
     description = "Controle de agendamentos de serviços para os pets. "
             + "Suporta filtro por pet, status e unidade, além de atualização de status e cancelamento.")
@RestController
@RequestMapping("/api/v1/agendamentos")
@RequiredArgsConstructor
public class AgendamentoController {

    private final AgendamentoService service;
    private final AgendamentoModelAssembler assembler;

    private final Map<String, AgendamentoResponse> idempotencyCache = new ConcurrentHashMap<>();

    // ── GET all ───────────────────────────────────────────────────────────────

    @Operation(summary = "Lista todos os agendamentos",
               description = "Filtre por `status` (PENDENTE, CONFIRMADO, EM_ANDAMENTO, CONCLUIDO, CANCELADO).")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<PagedModel<AgendamentoResponse>> listar(
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) StatusAgendamento status,
            PagedResourcesAssembler<AgendamentoResponse> pagedAssembler) {

        Page<AgendamentoResponse> page = status != null
                ? service.listarPorStatus(status, pageable)
                : service.listarTodos(pageable);

        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    // ── GET por pet ───────────────────────────────────────────────────────────

    @Operation(summary = "Lista agendamentos de um pet específico")
    @ApiResponse(responseCode = "200", description = "Lista retornada")
    @GetMapping("/pet/{petId}")
    public ResponseEntity<PagedModel<AgendamentoResponse>> listarPorPet(
            @PathVariable Long petId,
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<AgendamentoResponse> pagedAssembler) {
        return ResponseEntity.ok(
                pagedAssembler.toModel(service.listarPorPet(petId, pageable), assembler));
    }

    // ── GET by id ─────────────────────────────────────────────────────────────

    @Operation(summary = "Busca agendamento por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agendamento encontrado"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado",
                         content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.buscarPorId(id)));
    }

    // ── POST ──────────────────────────────────────────────────────────────────

    @Operation(summary = "Cria um novo agendamento",
               description = "Suporta idempotência via `X-Idempotency-Key`. "
                       + "A unidade deve estar ativa para aceitar agendamentos.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Agendamento criado",
                         headers = @Header(name = "Location", description = "URI do novo recurso")),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Pet, serviço ou unidade não encontrado"),
            @ApiResponse(responseCode = "422", description = "Unidade inativa — não aceita agendamentos")
    })
    @PostMapping
    public ResponseEntity<AgendamentoResponse> criar(
            @Valid @RequestBody AgendamentoRequest request,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {

        if (idempotencyKey != null && idempotencyCache.containsKey(idempotencyKey)) {
            AgendamentoResponse cached = idempotencyCache.get(idempotencyKey);
            return ResponseEntity.created(
                    linkTo(methodOn(AgendamentoController.class).buscar(cached.getId())).toUri()
            ).body(assembler.toModel(cached));
        }

        AgendamentoResponse criado = service.criar(request);
        AgendamentoResponse model  = assembler.toModel(criado);
        if (idempotencyKey != null) idempotencyCache.put(idempotencyKey, criado);

        URI location = linkTo(methodOn(AgendamentoController.class).buscar(criado.getId())).toUri();
        return ResponseEntity.created(location).body(model);
    }

    // ── PATCH status ──────────────────────────────────────────────────────────

    @Operation(summary = "Atualiza o status de um agendamento",
               description = "Valores válidos: PENDENTE, CONFIRMADO, EM_ANDAMENTO, CONCLUIDO, CANCELADO. "
                       + "Agendamentos CANCELADOS ou CONCLUÍDOS não podem ser alterados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado"),
            @ApiResponse(responseCode = "422", description = "Transição de status inválida")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<AgendamentoResponse> atualizarStatus(
            @PathVariable Long id,
            @RequestParam StatusAgendamento novoStatus) {
        return ResponseEntity.ok(assembler.toModel(service.atualizarStatus(id, novoStatus)));
    }

    // ── PATCH cancelar ────────────────────────────────────────────────────────

    @Operation(summary = "Cancela um agendamento",
               description = "Atalho para PATCH /status com status=CANCELADO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agendamento cancelado"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado"),
            @ApiResponse(responseCode = "422", description = "Agendamento já concluído ou cancelado")
    })
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<AgendamentoResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.cancelar(id)));
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Operation(summary = "Remove um agendamento pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Agendamento removido"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
