package com.petshop.api.assembler;

import com.petshop.api.controller.UnidadeController;
import com.petshop.api.dto.response.UnidadeResponse;
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
        r.add(
                linkTo(methodOn(UnidadeController.class).buscar(r.getId())).withSelfRel(),
                linkTo(methodOn(UnidadeController.class).atualizar(r.getId(), null)).withRel("update"),
                linkTo(methodOn(UnidadeController.class).deletar(r.getId())).withRel("delete"),
                linkTo(methodOn(UnidadeController.class).listar(null, false, null)).withRel("collection")
        );
        return r;
    }
}
