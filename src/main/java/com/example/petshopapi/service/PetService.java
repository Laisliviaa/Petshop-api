package com.example.petshopapi.service;

import com.example.petshopapi.exception.RecursoNaoEncontradoException;
import com.example.petshopapi.model.Cliente;
import com.example.petshopapi.model.Pet;
import com.example.petshopapi.repository.ClienteRepository;
import com.example.petshopapi.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final ClienteRepository clienteRepository;

    public Page<Pet> listarTodos(Pageable pageable) {
        return petRepository.findAll(pageable);
    }

    public Optional<Pet> buscarPorId(Long id) {
        return petRepository.findById(id);
    }

    public List<Pet> buscarPorNome(String nome) {
        return petRepository.findByNomeContainingIgnoreCase(nome);
    }

    public Pet salvar(Pet pet) {
        return petRepository.save(pet);
    }

    public Cliente buscarCliente(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", clienteId));
    }

    public void deletar(Long id) {
        petRepository.deleteById(id);
    }
}
