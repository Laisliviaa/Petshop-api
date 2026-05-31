package com.example.petshopapi.assemblers;

import com.example.petshopapi.controller.UnidadeController;
import com.example.petshopapi.dto.response.UnidadeResponse;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class UnidadeModelAssembler
        extends RepresentationModelAssemblerSupport<UnidadeResponse, UnidadeResponse> {

    public UnidadeModelAssembler() {
        super(UnidadeController.class, UnidadeResponse.class);
    }

    @Override
    public UnidadeResponse toModel(UnidadeResponse r) {
        r.add(linkTo(methodOn(UnidadeController.class).buscar(r.getId())).withSelfRel());
        r.add(linkTo(methodOn(UnidadeController.class)
                .atualizar(null, r.getId(), null)).withRel("update"));
        r.add(linkTo(methodOn(UnidadeController.class)
                .deletar(null, r.getId())).withRel("delete"));
        r.add(linkTo(methodOn(UnidadeController.class).listar(null)).withRel("collection"));
        return r;
    }
}
