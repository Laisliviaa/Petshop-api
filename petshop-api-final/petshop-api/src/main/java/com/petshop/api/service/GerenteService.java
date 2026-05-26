package com.petshop.api.service;

import com.petshop.api.dto.request.GerenteRequest;
import com.petshop.api.dto.response.GerenteResponse;
import com.petshop.api.exception.ConflictException;
import com.petshop.api.exception.RecursoNaoEncontradoException;
import com.petshop.api.model.Gerente;
import com.petshop.api.model.Unidade;
import com.petshop.api.repository.GerenteRepository;
import com.petshop.api.repository.UnidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GerenteService {

    private final GerenteRepository repository;
    private final UnidadeRepository unidadeRepository;

    @Transactional(readOnly = true)
    public Page<GerenteResponse> listarTodos(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public GerenteResponse buscarPorId(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public GerenteResponse criar(GerenteRequest request) {
        if (repository.existsByCpf(request.getCpf())) {
            throw new ConflictException("Já existe um gerente cadastrado com o CPF: " + request.getCpf());
        }
        Gerente g = toEntity(request);
        return toResponse(repository.save(g));
    }

    @Transactional
    public GerenteResponse atualizar(Long id, GerenteRequest request) {
        Gerente existente = findOrThrow(id);
        if (!existente.getCpf().equals(request.getCpf()) && repository.existsByCpf(request.getCpf())) {
            throw new ConflictException("Já existe um gerente cadastrado com o CPF: " + request.getCpf());
        }
        existente.setNome(request.getNome());
        existente.setCpf(request.getCpf());
        existente.setEmail(request.getEmail());
        if (request.getUnidadeId() != null) {
            Unidade u = unidadeRepository.findById(request.getUnidadeId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Unidade", request.getUnidadeId()));
            existente.setUnidade(u);
        }
        return toResponse(repository.save(existente));
    }

    @Transactional
    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Gerente", id);
        }
        repository.deleteById(id);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Gerente findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Gerente", id));
    }

    private Gerente toEntity(GerenteRequest r) {
        Gerente g = new Gerente();
        g.setNome(r.getNome());
        g.setCpf(r.getCpf());
        g.setEmail(r.getEmail());
        if (r.getUnidadeId() != null) {
            unidadeRepository.findById(r.getUnidadeId()).ifPresent(g::setUnidade);
        }
        return g;
    }

    public GerenteResponse toResponse(Gerente g) {
        return new GerenteResponse(
                g.getId(),
                g.getNome(),
                g.getCpf(),
                g.getEmail(),
                g.getUnidade() != null ? g.getUnidade().getNome() : null
        );
    }
}
