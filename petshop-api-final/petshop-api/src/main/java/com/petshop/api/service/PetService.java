package com.petshop.api.service;

import com.petshop.api.dto.request.PetRequest;
import com.petshop.api.dto.response.PetResponse;
import com.petshop.api.exception.RecursoNaoEncontradoException;
import com.petshop.api.model.Cliente;
import com.petshop.api.model.Pet;
import com.petshop.api.model.PortePet;
import com.petshop.api.repository.ClienteRepository;
import com.petshop.api.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final ClienteRepository clienteRepository;

    @Transactional(readOnly = true)
    public Page<PetResponse> listarTodos(Pageable pageable) {
        return petRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PetResponse> listarPorCliente(Long clienteId, Pageable pageable) {
        return petRepository.findByClienteId(clienteId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PetResponse> listarPorEspecie(String especie, Pageable pageable) {
        return petRepository.findByEspecieIgnoreCase(especie, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PetResponse> listarPorPorte(PortePet porte, Pageable pageable) {
        return petRepository.findByPorte(porte, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PetResponse buscarPorId(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public PetResponse criar(PetRequest request) {
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", request.getClienteId()));
        Pet pet = toEntity(request, cliente);
        return toResponse(petRepository.save(pet));
    }

    @Transactional
    public PetResponse atualizar(Long id, PetRequest request) {
        Pet existente = findOrThrow(id);
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", request.getClienteId()));
        existente.setNome(request.getNome());
        existente.setEspecie(request.getEspecie());
        existente.setRaca(request.getRaca());
        existente.setPorte(request.getPorte());
        existente.setCliente(cliente);
        return toResponse(petRepository.save(existente));
    }

    @Transactional
    public void deletar(Long id) {
        if (!petRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Pet", id);
        }
        petRepository.deleteById(id);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Pet findOrThrow(Long id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pet", id));
    }

    private Pet toEntity(PetRequest r, Cliente cliente) {
        Pet p = new Pet();
        p.setNome(r.getNome());
        p.setEspecie(r.getEspecie());
        p.setRaca(r.getRaca());
        p.setPorte(r.getPorte());
        p.setCliente(cliente);
        return p;
    }

    public PetResponse toResponse(Pet p) {
        return new PetResponse(
                p.getId(),
                p.getNome(),
                p.getEspecie(),
                p.getRaca(),
                p.getPorte(),
                p.getCliente() != null ? p.getCliente().getNome() : null,
                p.getServicos() != null ? p.getServicos().size() : 0
        );
    }
}
