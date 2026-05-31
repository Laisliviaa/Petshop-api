package com.example.petshopapi.service;

import com.example.petshopapi.exception.ConflictException;
import com.example.petshopapi.exception.RecursoNaoEncontradoException;
import com.example.petshopapi.model.Cliente;
import com.example.petshopapi.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository repository;

    public Page<Cliente> listarTodos(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Optional<Cliente> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public Optional<Cliente> buscarPorCpf(String cpf) {
        return repository.findByCpf(cpf);
    }

    public List<Cliente> buscarPorNome(String nome) {
        return repository.findByNomeContainingIgnoreCase(nome);
    }

    public Cliente salvar(Cliente cliente) {
        if (cliente.getId() == null) {
            if (repository.existsByCpf(cliente.getCpf()))
                throw new ConflictException("CPF '" + cliente.getCpf() + "' já está cadastrado.");
        } else {
            if (repository.existsByCpfAndIdNot(cliente.getCpf(), cliente.getId()))
                throw new ConflictException(
                        "CPF '" + cliente.getCpf() + "' já pertence a outro cliente.");
        }
        return repository.save(cliente);
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }
}
