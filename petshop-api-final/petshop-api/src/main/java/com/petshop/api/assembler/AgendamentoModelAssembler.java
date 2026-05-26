package com.petshop.api.assembler;

import com.petshop.api.controller.AgendamentoController;
import com.petshop.api.dto.response.AgendamentoResponse;
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
        r.add(
                linkTo(methodOn(AgendamentoController.class).buscar(r.getId())).withSelfRel(),
                linkTo(methodOn(AgendamentoController.class).atualizarStatus(r.getId(), null)).withRel("update-status"),
                linkTo(methodOn(AgendamentoController.class).cancelar(r.getId())).withRel("cancelar"),
                linkTo(methodOn(AgendamentoController.class).listar(null, null, null)).withRel("collection")
        );
        return r;
    }
}
