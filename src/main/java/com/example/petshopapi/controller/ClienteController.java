package com.example.petshopapi.controller;

import com.example.petshopapi.exception.RecursoNaoEncontradoException;
import com.example.petshopapi.model.Cliente;
import com.example.petshopapi.service.ClienteService;
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
@Tag(name = "Clientes")
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ClienteService service;
    private final PagedResourcesAssembler<Cliente> assembler;

    @Autowired
    public ClienteController(ClienteService service, PagedResourcesAssembler<Cliente> assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @Operation(summary = "Lista todos os clientes", description = "Retorna uma lista paginada com links HATEOAS")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Cliente>>> listar(Pageable pageable) {
        Page<Cliente> clientes = service.listarTodos(pageable);
        return ResponseEntity.ok(assembler.toModel(clientes,
                c -> EntityModel.of(c, linkTo(methodOn(ClienteController.class).buscar(c.getId())).withSelfRel())));
    }

    @Operation(summary = "Cadastra um novo cliente", description = "Cria um novo cliente no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<EntityModel<Cliente>> criar(@Valid @RequestBody Cliente cliente) {
        Cliente novo = service.salvar(cliente);
        EntityModel<Cliente> model = EntityModel.of(novo,
                linkTo(methodOn(ClienteController.class).buscar(novo.getId())).withSelfRel(),
                linkTo(methodOn(ClienteController.class).listar(null)).withRel("lista"));
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @Operation(summary = "Busca cliente por ID", description = "Retorna os detalhes de um cliente específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    @GetMapping("/{id}")
    public EntityModel<Cliente> buscar(@PathVariable Long id) {
        Cliente cliente = service.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(id));
        return EntityModel.of(cliente,
                linkTo(methodOn(ClienteController.class).buscar(id)).withSelfRel(),
                linkTo(methodOn(ClienteController.class).listar(null)).withRel("lista"));
    }

    @Operation(summary = "Atualiza dados do cliente", description = "Altera nome ou CPF de um cliente cadastrado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    @PutMapping("/{id}")
    public EntityModel<Cliente> atualizar(@PathVariable Long id, @Valid @RequestBody Cliente novo) {
        return service.buscarPorId(id).map(c -> {
            c.setNome(novo.getNome());
            c.setCpf(novo.getCpf());
            return EntityModel.of(service.salvar(c),
                    linkTo(methodOn(ClienteController.class).buscar(id)).withSelfRel());
        }).orElseThrow(() -> new RecursoNaoEncontradoException(id));
    }

    @Operation(summary = "Deleta um cliente", description = "Remove permanentemente o cliente do sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        if (!service.buscarPorId(id).isPresent()) return ResponseEntity.notFound().build();
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Busca cliente por CPF", description = "Consulta personalizada para buscar cliente pelo CPF")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<EntityModel<Cliente>> buscarPorCpf(@PathVariable String cpf) {
        Cliente c = service.buscarPorCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com CPF: " + cpf));
        return ResponseEntity.ok(EntityModel.of(c,
                linkTo(methodOn(ClienteController.class).buscar(c.getId())).withSelfRel()));
    }
}
