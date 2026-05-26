package com.petshop.api.assembler;

import com.petshop.api.controller.ServicoController;
import com.petshop.api.dto.response.ServicoResponse;
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
        r.add(
                linkTo(methodOn(ServicoController.class).buscar(r.getId())).withSelfRel(),
                linkTo(methodOn(ServicoController.class).atualizar(r.getId(), null)).withRel("update"),
                linkTo(methodOn(ServicoController.class).deletar(r.getId())).withRel("delete"),
                linkTo(methodOn(ServicoController.class).listar(null, null, null)).withRel("collection")
        );
        return r;
    }
}
