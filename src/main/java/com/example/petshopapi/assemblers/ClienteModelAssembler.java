package com.example.petshopapi.assemblers;

import com.example.petshopapi.controller.ClienteController;
import com.example.petshopapi.dto.response.ClienteResponse;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class ClienteModelAssembler
        extends RepresentationModelAssemblerSupport<ClienteResponse, ClienteResponse> {

    public ClienteModelAssembler() { super(ClienteController.class, ClienteResponse.class); }

    @Override
    public ClienteResponse toModel(ClienteResponse r) {
        r.add(linkTo(methodOn(ClienteController.class).buscar(r.getId(), "1")).withSelfRel());
        r.add(linkTo(methodOn(ClienteController.class).atualizar(null, r.getId(), null)).withRel("update"));
        r.add(linkTo(methodOn(ClienteController.class).deletar(null, r.getId())).withRel("delete"));
        r.add(linkTo(methodOn(ClienteController.class).listar("1", null)).withRel("collection"));
        return r;
    }
}
