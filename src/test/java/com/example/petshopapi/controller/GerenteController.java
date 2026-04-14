package com.example.petshopapi.controller;

import com.example.petshopapi.exception.RecursoNaoEncontradoException;
import com.example.petshopapi.model.Gerente;
import com.example.petshopapi.repository.GerenteRepository;
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
@Tag(name = "Gerentes")
@RequestMapping("/api/v1/gerentes")
public class GerenteController {

    private final GerenteRepository repository;
    private final PagedResourcesAssembler<Gerente> assembler;

    @Autowired
    public GerenteController(GerenteRepository repository, PagedResourcesAssembler<Gerente> assembler) {
        this.repository = repository;
        this.assembler = assembler;
    }

    @Operation(summary = "Lista todos os gerentes", description = "Retorna uma lista paginada com links HATEOAS")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @GetMapping
    public PagedModel<EntityModel<Gerente>> listar(Pageable pageable) {
        Page<Gerente> gerentes = repository.findAll(pageable);
        return assembler.toModel(gerentes,
                g -> EntityModel.of(g, linkTo(methodOn(GerenteController.class).buscar(g.getId())).withSelfRel()));
    }

    @Operation(summary = "Cadastra um novo gerente", description = "Cria um novo gerente vinculado a uma unidade")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Gerente cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<EntityModel<Gerente>> criar(@Valid @RequestBody Gerente gerente) {
        Gerente novo = repository.save(gerente);
        EntityModel<Gerente> model = EntityModel.of(novo,
                linkTo(methodOn(GerenteController.class).buscar(novo.getId())).withSelfRel(),
                linkTo(methodOn(GerenteController.class).listar(null)).withRel("lista"));
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @Operation(summary = "Busca gerente por ID", description = "Retorna os detalhes de um gerente específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Gerente não encontrado")
    })
    @GetMapping("/{id}")
    public EntityModel<Gerente> buscar(@PathVariable Long id) {
        Gerente g = repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(id));
        return EntityModel.of(g,
                linkTo(methodOn(GerenteController.class).buscar(id)).withSelfRel(),
                linkTo(methodOn(GerenteController.class).listar(null)).withRel("lista"));
    }

    @Operation(summary = "Atualiza um gerente", description = "Permite alterar nome ou unidade de um gerente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Gerente não encontrado")
    })
    @PutMapping("/{id}")
    public EntityModel<Gerente> atualizar(@PathVariable Long id, @Valid @RequestBody Gerente novo) {
        return repository.findById(id).map(g -> {
            g.setNome(novo.getNome());
            g.setUnidade(novo.getUnidade());
            return EntityModel.of(repository.save(g),
                    linkTo(methodOn(GerenteController.class).buscar(id)).withSelfRel());
        }).orElseThrow(() -> new RecursoNaoEncontradoException(id));
    }

    @Operation(summary = "Deleta um gerente", description = "Remove permanentemente o gerente do sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Gerente removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Gerente não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Busca gerentes por nome", description = "Consulta personalizada para buscar gerentes pelo nome")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso")
    })
    @GetMapping("/busca")
    public CollectionModel<EntityModel<Gerente>> buscarPorNome(@RequestParam String nome) {
        List<EntityModel<Gerente>> gerentes = repository.findByNomeContainingIgnoreCase(nome).stream()
                .map(g -> EntityModel.of(g, linkTo(methodOn(GerenteController.class).buscar(g.getId())).withSelfRel()))
                .collect(Collectors.toList());
        return CollectionModel.of(gerentes);
    }
}
