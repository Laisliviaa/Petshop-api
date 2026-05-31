package com.example.petshopapi.assemblers;

import com.example.petshopapi.controller.AgendamentoController;
import com.example.petshopapi.dto.response.AgendamentoResponse;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class AgendamentoModelAssembler
        extends RepresentationModelAssemblerSupport<AgendamentoResponse, AgendamentoResponse> {

    public AgendamentoModelAssembler() {
        super(AgendamentoController.class, AgendamentoResponse.class);
    }

    @Override
    public AgendamentoResponse toModel(AgendamentoResponse r) {
        r.add(linkTo(methodOn(AgendamentoController.class).buscar(r.getId())).withSelfRel());
        r.add(linkTo(methodOn(AgendamentoController.class)
                .atualizar(null, r.getId(), null)).withRel("update"));
        r.add(linkTo(methodOn(AgendamentoController.class)
                .deletar(null, r.getId())).withRel("delete"));
        r.add(linkTo(methodOn(AgendamentoController.class)
                .listar(null)).withRel("collection"));
        return r;
    }
}
