package com.petshop.api.service;

import com.petshop.api.dto.request.ServicoRequest;
import com.petshop.api.dto.response.ServicoResponse;
import com.petshop.api.exception.ConflictException;
import com.petshop.api.exception.RecursoNaoEncontradoException;
import com.petshop.api.model.Servico;
import com.petshop.api.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository repository;

    @Transactional(readOnly = true)
    public Page<ServicoResponse> listarTodos(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ServicoResponse> listarPorCategoria(String categoria, Pageable pageable) {
        return repository.findByCategoriaIgnoreCase(categoria, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ServicoResponse buscarPorId(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public ServicoResponse criar(ServicoRequest request) {
        if (repository.existsByDescricaoIgnoreCase(request.getDescricao())) {
            throw new ConflictException("Já existe um serviço com a descrição: " + request.getDescricao());
        }
        return toResponse(repository.save(toEntity(request)));
    }

    @Transactional
    public ServicoResponse atualizar(Long id, ServicoRequest request) {
        Servico existente = findOrThrow(id);
        existente.setDescricao(request.getDescricao());
        existente.setPreco(request.getPreco());
        existente.setCategoria(request.getCategoria());
        return toResponse(repository.save(existente));
    }

    @Transactional
    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Serviço", id);
        }
        repository.deleteById(id);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Servico findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço", id));
    }

    private Servico toEntity(ServicoRequest r) {
        Servico s = new Servico();
        s.setDescricao(r.getDescricao());
        s.setPreco(r.getPreco());
        s.setCategoria(r.getCategoria());
        return s;
    }

    public ServicoResponse toResponse(Servico s) {
        return new ServicoResponse(
                s.getId(),
                s.getDescricao(),
                s.getPreco(),
                s.getCategoria(),
                s.getPets() != null ? s.getPets().size() : 0
        );
    }
}
