package com.example.petshopapi.controller;

import com.example.petshopapi.exception.RecursoNaoEncontradoException;
import com.example.petshopapi.model.Servico;
import com.example.petshopapi.repository.ServicoRepository;
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
@Tag(name = "Serviços")
@RequestMapping("/api/v1/servicos")
public class ServicoController {

    private final ServicoRepository repository;
    private final PagedResourcesAssembler<Servico> assembler;

    @Autowired
    public ServicoController(ServicoRepository repository, PagedResourcesAssembler<Servico> assembler) {
        this.repository = repository;
        this.assembler = assembler;
    }

    @Operation(summary = "Lista todos os serviços", description = "Retorna uma lista paginada com links HATEOAS")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @GetMapping
    public PagedModel<EntityModel<Servico>> listar(Pageable pageable) {
        Page<Servico> servicos = repository.findAll(pageable);
        return assembler.toModel(servicos,
                s -> EntityModel.of(s, linkTo(methodOn(ServicoController.class).buscar(s.getId())).withSelfRel()));
    }

    @Operation(summary = "Cadastra um novo serviço", description = "Cria um novo serviço disponível no petshop")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Serviço cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<EntityModel<Servico>> criar(@Valid @RequestBody Servico servico) {
        Servico novo = repository.save(servico);
        EntityModel<Servico> model = EntityModel.of(novo,
                linkTo(methodOn(ServicoController.class).buscar(novo.getId())).withSelfRel(),
                linkTo(methodOn(ServicoController.class).listar(null)).withRel("lista"));
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @Operation(summary = "Busca serviço por ID", description = "Retorna os detalhes de um serviço específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    @GetMapping("/{id}")
    public EntityModel<Servico> buscar(@PathVariable Long id) {
        Servico s = repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(id));
        return EntityModel.of(s,
                linkTo(methodOn(ServicoController.class).buscar(id)).withSelfRel(),
                linkTo(methodOn(ServicoController.class).listar(null)).withRel("lista"));
    }

    @Operation(summary = "Atualiza um serviço", description = "Permite alterar descrição ou preço de um serviço")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    @PutMapping("/{id}")
    public EntityModel<Servico> atualizar(@PathVariable Long id, @Valid @RequestBody Servico novo) {
        return repository.findById(id).map(s -> {
            s.setDescricao(novo.getDescricao());
            s.setPreco(novo.getPreco());
            return EntityModel.of(repository.save(s),
                    linkTo(methodOn(ServicoController.class).buscar(id)).withSelfRel());
        }).orElseThrow(() -> new RecursoNaoEncontradoException(id));
    }

    @Operation(summary = "Deleta um serviço", description = "Remove permanentemente o serviço do sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Serviço removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Busca serviços por descrição", description = "Consulta personalizada para buscar serviços pelo nome")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso")
    })
    @GetMapping("/busca")
    public CollectionModel<EntityModel<Servico>> buscarPorDescricao(@RequestParam String descricao) {
        List<EntityModel<Servico>> servicos = repository.findByDescricaoContainingIgnoreCase(descricao).stream()
                .map(s -> EntityModel.of(s, linkTo(methodOn(ServicoController.class).buscar(s.getId())).withSelfRel()))
                .collect(Collectors.toList());
        return CollectionModel.of(servicos);
    }
}
