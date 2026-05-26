package com.petshop.api.assembler;

import com.petshop.api.controller.ClienteController;
import com.petshop.api.controller.PetController;
import com.petshop.api.dto.response.ClienteResponse;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class ClienteModelAssembler
        extends RepresentationModelAssemblerSupport<ClienteResponse, ClienteResponse> {

    public ClienteModelAssembler() {
        super(ClienteController.class, ClienteResponse.class);
    }

    @Override
    public ClienteResponse toModel(ClienteResponse r) {
        r.add(
                linkTo(methodOn(ClienteController.class).buscar(r.getId())).withSelfRel(),
                linkTo(methodOn(ClienteController.class).atualizar(r.getId(), null)).withRel("update"),
                linkTo(methodOn(ClienteController.class).deletar(r.getId())).withRel("delete"),
                linkTo(methodOn(ClienteController.class).listar(null, null, null)).withRel("collection"),
                linkTo(methodOn(PetController.class).listarPorCliente(r.getId(), null, null)).withRel("pets")
        );
        return r;
    }
}
