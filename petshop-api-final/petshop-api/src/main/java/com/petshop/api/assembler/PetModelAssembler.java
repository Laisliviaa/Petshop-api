package com.petshop.api.assembler;

import com.petshop.api.controller.AgendamentoController;
import com.petshop.api.controller.PetController;
import com.petshop.api.dto.response.PetResponse;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class PetModelAssembler
        extends RepresentationModelAssemblerSupport<PetResponse, PetResponse> {

    public PetModelAssembler() {
        super(PetController.class, PetResponse.class);
    }

    @Override
    public PetResponse toModel(PetResponse r) {
        r.add(
                linkTo(methodOn(PetController.class).buscar(r.getId())).withSelfRel(),
                linkTo(methodOn(PetController.class).atualizar(r.getId(), null)).withRel("update"),
                linkTo(methodOn(PetController.class).deletar(r.getId())).withRel("delete"),
                linkTo(methodOn(PetController.class).listar(null, null, null, null)).withRel("collection"),
                linkTo(methodOn(AgendamentoController.class).listarPorPet(r.getId(), null, null)).withRel("agendamentos")
        );
        return r;
    }
}
