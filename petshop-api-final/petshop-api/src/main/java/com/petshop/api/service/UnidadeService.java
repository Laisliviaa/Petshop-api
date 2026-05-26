package com.petshop.api.service;

import com.petshop.api.dto.request.UnidadeRequest;
import com.petshop.api.dto.response.UnidadeResponse;
import com.petshop.api.exception.RecursoNaoEncontradoException;
import com.petshop.api.model.Unidade;
import com.petshop.api.repository.UnidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UnidadeService {

    private final UnidadeRepository repository;

    @Transactional(readOnly = true)
    public Page<UnidadeResponse> listarTodos(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<UnidadeResponse> listarAtivas(Pageable pageable) {
        return repository.findByAtivaTrue(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public UnidadeResponse buscarPorId(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public UnidadeResponse criar(UnidadeRequest request) {
        return toResponse(repository.save(toEntity(request)));
    }

    @Transactional
    public UnidadeResponse atualizar(Long id, UnidadeRequest request) {
        Unidade existente = findOrThrow(id);
        existente.setNome(request.getNome());
        existente.setEndereco(request.getEndereco());
        existente.setTelefone(request.getTelefone());
        if (request.getAtiva() != null) existente.setAtiva(request.getAtiva());
        return toResponse(repository.save(existente));
    }

    @Transactional
    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Unidade", id);
        }
        repository.deleteById(id);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    public Unidade findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Unidade", id));
    }

    private Unidade toEntity(UnidadeRequest r) {
        Unidade u = new Unidade();
        u.setNome(r.getNome());
        u.setEndereco(r.getEndereco());
        u.setTelefone(r.getTelefone());
        u.setAtiva(r.getAtiva() != null ? r.getAtiva() : true);
        return u;
    }

    public UnidadeResponse toResponse(Unidade u) {
        return new UnidadeResponse(
                u.getId(),
                u.getNome(),
                u.getEndereco(),
                u.getTelefone(),
                u.getAtiva()
        );
    }
}
