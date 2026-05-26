package com.petshop.api.service;

import com.petshop.api.dto.request.ClienteRequest;
import com.petshop.api.dto.response.ClienteResponse;
import com.petshop.api.exception.ConflictException;
import com.petshop.api.exception.RecursoNaoEncontradoException;
import com.petshop.api.model.Cliente;
import com.petshop.api.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository repository;

    @Transactional(readOnly = true)
    public Page<ClienteResponse> listarTodos(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ClienteResponse> buscarPorNome(String nome, Pageable pageable) {
        return repository.findByNomeContainingIgnoreCase(nome, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public ClienteResponse criar(ClienteRequest request) {
        if (repository.existsByCpf(request.getCpf())) {
            throw new ConflictException("Já existe um cliente cadastrado com o CPF: " + request.getCpf());
        }
        Cliente entity = toEntity(request);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest request) {
        Cliente existente = findOrThrow(id);
        // Verifica conflito de CPF apenas se mudou
        if (!existente.getCpf().equals(request.getCpf()) && repository.existsByCpf(request.getCpf())) {
            throw new ConflictException("Já existe um cliente cadastrado com o CPF: " + request.getCpf());
        }
        existente.setNome(request.getNome());
        existente.setCpf(request.getCpf());
        existente.setEmail(request.getEmail());
        existente.setTelefone(request.getTelefone());
        return toResponse(repository.save(existente));
    }

    @Transactional
    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Cliente", id);
        }
        repository.deleteById(id);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Cliente findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
    }

    private Cliente toEntity(ClienteRequest r) {
        Cliente c = new Cliente();
        c.setNome(r.getNome());
        c.setCpf(r.getCpf());
        c.setEmail(r.getEmail());
        c.setTelefone(r.getTelefone());
        return c;
    }

    public ClienteResponse toResponse(Cliente c) {
        return new ClienteResponse(
                c.getId(),
                c.getNome(),
                c.getCpf(),
                c.getEmail(),
                c.getTelefone(),
                c.getPets() != null ? c.getPets().size() : 0
        );
    }
}
