package com.example.petshopapi.assemblers;

import com.example.petshopapi.controller.PetController;
import com.example.petshopapi.dto.response.PetResponse;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class PetModelAssembler
        extends RepresentationModelAssemblerSupport<PetResponse, PetResponse> {

    public PetModelAssembler() { super(PetController.class, PetResponse.class); }

    @Override
    public PetResponse toModel(PetResponse r) {
        r.add(linkTo(methodOn(PetController.class).buscar(r.getId())).withSelfRel());
        r.add(linkTo(methodOn(PetController.class).atualizar(null, r.getId(), null)).withRel("update"));
        r.add(linkTo(methodOn(PetController.class).deletar(null, r.getId())).withRel("delete"));
        r.add(linkTo(methodOn(PetController.class).listar(null)).withRel("collection"));
        return r;
    }
}
