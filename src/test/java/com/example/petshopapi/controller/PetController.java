package com.example.petshopapi.controller;

import com.example.petshopapi.exception.RecursoNaoEncontradoException;
import com.example.petshopapi.model.Pet;
import com.example.petshopapi.repository.PetRepository;
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
@Tag(name = "Pets")
@RequestMapping("/api/v1/pets")
public class PetController {

    private final PetRepository repository;
    private final PagedResourcesAssembler<Pet> assembler;

    @Autowired
    public PetController(PetRepository repository, PagedResourcesAssembler<Pet> assembler) {
        this.repository = repository;
        this.assembler = assembler;
    }

    @Operation(summary = "Lista todos os pets", description = "Retorna uma lista paginada com links HATEOAS")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @GetMapping
    public PagedModel<EntityModel<Pet>> listar(Pageable pageable) {
        Page<Pet> pets = repository.findAll(pageable);
        return assembler.toModel(pets,
                pet -> EntityModel.of(pet, linkTo(methodOn(PetController.class).buscar(pet.getId())).withSelfRel()));
    }

    @Operation(summary = "Cadastra um novo pet", description = "Cria um novo pet vinculado a um cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pet cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<EntityModel<Pet>> criar(@Valid @RequestBody Pet pet) {
        Pet novo = repository.save(pet);
        EntityModel<Pet> model = EntityModel.of(novo,
                linkTo(methodOn(PetController.class).buscar(novo.getId())).withSelfRel(),
                linkTo(methodOn(PetController.class).listar(null)).withRel("lista"));
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @Operation(summary = "Busca pet por ID", description = "Retorna os detalhes de um pet específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pet não encontrado")
    })
    @GetMapping("/{id}")
    public EntityModel<Pet> buscar(@PathVariable Long id) {
        Pet pet = repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(id));
        return EntityModel.of(pet,
                linkTo(methodOn(PetController.class).buscar(id)).withSelfRel(),
                linkTo(methodOn(PetController.class).listar(null)).withRel("lista"));
    }

    @Operation(summary = "Atualiza dados do pet", description = "Altera nome ou espécie de um pet cadastrado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pet não encontrado")
    })
    @PutMapping("/{id}")
    public EntityModel<Pet> atualizar(@PathVariable Long id, @Valid @RequestBody Pet novo) {
        return repository.findById(id).map(pet -> {
            pet.setNome(novo.getNome());
            pet.setEspecie(novo.getEspecie());
            pet.setCliente(novo.getCliente());
            return EntityModel.of(repository.save(pet),
                    linkTo(methodOn(PetController.class).buscar(id)).withSelfRel());
        }).orElseThrow(() -> new RecursoNaoEncontradoException(id));
    }

    @Operation(summary = "Deleta um pet", description = "Remove permanentemente o pet do sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pet removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pet não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Busca pets por nome", description = "Consulta personalizada para buscar pets pelo nome")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso")
    })
    @GetMapping("/busca")
    public CollectionModel<EntityModel<Pet>> buscarPorNome(@RequestParam String nome) {
        List<EntityModel<Pet>> pets = repository.findByNomeContainingIgnoreCase(nome).stream()
                .map(pet -> EntityModel.of(pet,
                        linkTo(methodOn(PetController.class).buscar(pet.getId())).withSelfRel()))
                .collect(Collectors.toList());
        return CollectionModel.of(pets);
    }
}
