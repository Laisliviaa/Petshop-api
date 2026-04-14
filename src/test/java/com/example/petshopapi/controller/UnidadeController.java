package com.example.petshopapi.controller;

import com.example.petshopapi.exception.RecursoNaoEncontradoException;
import com.example.petshopapi.model.Unidade;
import com.example.petshopapi.repository.UnidadeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@Tag(name = "Unidades")
@RequestMapping("/api/v1/unidades")
public class UnidadeController {

    private final UnidadeRepository repository;
    private final PagedResourcesAssembler<Unidade> assembler;

    @Autowired
    public UnidadeController(UnidadeRepository repository, PagedResourcesAssembler<Unidade> assembler) {
        this.repository = repository;
        this.assembler = assembler;
    }

    @Operation(summary = "Lista todas as unidades", description = "Retorna uma lista paginada com links HATEOAS")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @GetMapping
    public PagedModel<EntityModel<Unidade>> listar(Pageable pageable) {
        Page<Unidade> unidades = repository.findAll(pageable);
        return assembler.toModel(unidades,
                u -> EntityModel.of(u, linkTo(methodOn(UnidadeController.class).buscar(u.getId())).withSelfRel()));
    }

    @Operation(summary = "Cadastra uma nova unidade", description = "Cria uma nova unidade do petshop")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Unidade cadastrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<EntityModel<Unidade>> criar(@Valid @RequestBody Unidade unidade) {
        Unidade nova = repository.save(unidade);
        EntityModel<Unidade> model = EntityModel.of(nova,
                linkTo(methodOn(UnidadeController.class).buscar(nova.getId())).withSelfRel(),
                linkTo(methodOn(UnidadeController.class).listar(null)).withRel("lista"));
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @Operation(summary = "Busca unidade por ID", description = "Retorna os detalhes de uma unidade específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Unidade não encontrada")
    })
    @GetMapping("/{id}")
    public EntityModel<Unidade> buscar(@PathVariable Long id) {
        Unidade u = repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(id));
        return EntityModel.of(u,
                linkTo(methodOn(UnidadeController.class).buscar(id)).withSelfRel(),
                linkTo(methodOn(UnidadeController.class).listar(null)).withRel("lista"));
    }

    @Operation(summary = "Atualiza uma unidade", description = "Permite alterar nome ou endereço de uma unidade")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Unidade não encontrada")
    })
    @PutMapping("/{id}")
    public EntityModel<Unidade> atualizar(@PathVariable Long id, @Valid @RequestBody Unidade novo) {
        return repository.findById(id).map(u -> {
            u.setNome(novo.getNome());
            u.setEndereco(novo.getEndereco());
            return EntityModel.of(repository.save(u),
                    linkTo(methodOn(UnidadeController.class).buscar(id)).withSelfRel());
        }).orElseThrow(() -> new RecursoNaoEncontradoException(id));
    }

    @Operation(summary = "Deleta uma unidade", description = "Remove permanentemente a unidade do sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Unidade removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Unidade não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Busca unidades por nome", description = "Consulta personalizada para buscar unidades pelo nome")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso")
    })
    @GetMapping("/busca")
    public CollectionModel<EntityModel<Unidade>> buscarPorNome(@RequestParam String nome) {
        List<EntityModel<Unidade>> unidades = repository.findByNomeContainingIgnoreCase(nome).stream()
                .map(u -> EntityModel.of(u, linkTo(methodOn(UnidadeController.class).buscar(u.getId())).withSelfRel()))
                .collect(Collectors.toList());
        return CollectionModel.of(unidades);
    }
}
