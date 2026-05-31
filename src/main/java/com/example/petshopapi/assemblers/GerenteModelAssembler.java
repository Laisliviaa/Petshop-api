package com.example.petshopapi.assemblers;

import com.example.petshopapi.controller.GerenteController;
import com.example.petshopapi.dto.response.GerenteResponse;
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
        r.add(linkTo(methodOn(GerenteController.class).buscar(r.getId())).withSelfRel());
        r.add(linkTo(methodOn(GerenteController.class)
                .atualizar(null, r.getId(), null)).withRel("update"));
        r.add(linkTo(methodOn(GerenteController.class)
                .deletar(null, r.getId())).withRel("delete"));
        r.add(linkTo(methodOn(GerenteController.class).listar(null)).withRel("collection"));
        return r;
    }
}
