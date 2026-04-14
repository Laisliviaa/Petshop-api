package com.example.petshopapi.controller;

import com.example.petshopapi.exception.RecursoNaoEncontradoException;
import com.example.petshopapi.model.Agendamento;
import com.example.petshopapi.model.StatusAgendamento;
import com.example.petshopapi.repository.AgendamentoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@Tag(name = "Agendamentos")
@RequestMapping("/api/v1/agendamentos")
public class AgendamentoController {

    private final AgendamentoRepository repository;
    private final PagedResourcesAssembler<Agendamento> assembler;

    @Autowired
    public AgendamentoController(AgendamentoRepository repository, PagedResourcesAssembler<Agendamento> assembler) {
        this.repository = repository;
        this.assembler = assembler;
    }

    @Operation(summary = "Lista todos os agendamentos", description = "Retorna uma lista paginada com links HATEOAS")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @GetMapping
    public PagedModel<EntityModel<Agendamento>> listar(Pageable pageable) {
        Page<Agendamento> agendamentos = repository.findAll(pageable);
        return assembler.toModel(agendamentos,
                a -> EntityModel.of(a, linkTo(methodOn(AgendamentoController.class).buscar(a.getId())).withSelfRel()));
    }

    @Operation(summary = "Cria um novo agendamento", description = "Registra um agendamento de serviço para um pet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Agendamento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<EntityModel<Agendamento>> criar(@Valid @RequestBody Agendamento agendamento) {
        Agendamento novo = repository.save(agendamento);
        EntityModel<Agendamento> model = EntityModel.of(novo,
                linkTo(methodOn(AgendamentoController.class).buscar(novo.getId())).withSelfRel(),
                linkTo(methodOn(AgendamentoController.class).listar(null)).withRel("lista"));
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @Operation(summary = "Busca agendamento por ID", description = "Retorna os detalhes de um agendamento específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    })
    @GetMapping("/{id}")
    public EntityModel<Agendamento> buscar(@PathVariable Long id) {
        Agendamento a = repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(id));
        return EntityModel.of(a,
                linkTo(methodOn(AgendamentoController.class).buscar(id)).withSelfRel(),
                linkTo(methodOn(AgendamentoController.class).listar(null)).withRel("lista"));
    }

    @Operation(summary = "Atualiza um agendamento", description = "Permite alterar a data/hora ou status de um agendamento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    })
    @PutMapping("/{id}")
    public EntityModel<Agendamento> atualizar(@PathVariable Long id, @Valid @RequestBody Agendamento novo) {
        return repository.findById(id).map(a -> {
            a.setDataHora(novo.getDataHora());
            a.setStatus(novo.getStatus());
            a.setPet(novo.getPet());
            return EntityModel.of(repository.save(a),
                    linkTo(methodOn(AgendamentoController.class).buscar(id)).withSelfRel());
        }).orElseThrow(() -> new RecursoNaoEncontradoException(id));
    }

    @Operation(summary = "Deleta um agendamento", description = "Remove permanentemente o agendamento do sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Agendamento removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Busca agendamentos por status", description = "Consulta personalizada: PENDENTE, CONCLUIDO ou CANCELADO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<?> buscarPorStatus(@PathVariable StatusAgendamento status) {
        return ResponseEntity.ok(repository.findByStatus(status));
    }
}
