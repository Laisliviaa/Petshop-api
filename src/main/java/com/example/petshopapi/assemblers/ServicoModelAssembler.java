package com.example.petshopapi.assemblers;

import com.example.petshopapi.controller.ServicoController;
import com.example.petshopapi.dto.response.ServicoResponse;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class ServicoModelAssembler
        extends RepresentationModelAssemblerSupport<ServicoResponse, ServicoResponse> {

    public ServicoModelAssembler() {
        super(ServicoController.class, ServicoResponse.class);
    }

    @Override
    public ServicoResponse toModel(ServicoResponse r) {
        r.add(linkTo(methodOn(ServicoController.class).buscar(r.getId())).withSelfRel());
        r.add(linkTo(methodOn(ServicoController.class)
                .atualizar(null, r.getId(), null)).withRel("update"));
        r.add(linkTo(methodOn(ServicoController.class)
                .deletar(null, r.getId())).withRel("delete"));
        r.add(linkTo(methodOn(ServicoController.class).listar(null)).withRel("collection"));
        return r;
    }
}
