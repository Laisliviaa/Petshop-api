package com.petshop.api.assembler;

import com.petshop.api.controller.GerenteController;
import com.petshop.api.dto.response.GerenteResponse;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class GerenteModelAssembler
        extends RepresentationModelAssemblerSupport<GerenteResponse, GerenteResponse> {

    public GerenteModelAssembler() {
        super(GerenteController.class, GerenteResponse.class);
    }

    @Override
    public GerenteResponse toModel(GerenteResponse r) {
        r.add(
                linkTo(methodOn(GerenteController.class).buscar(r.getId())).withSelfRel(),
                linkTo(methodOn(GerenteController.class).atualizar(r.getId(), null)).withRel("update"),
                linkTo(methodOn(GerenteController.class).deletar(r.getId())).withRel("delete"),
                linkTo(methodOn(GerenteController.class).listar(null, null)).withRel("collection")
        );
        return r;
    }
}
