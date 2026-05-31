package com.example.petshopapi.service;

import com.example.petshopapi.model.Unidade;
import com.example.petshopapi.repository.UnidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UnidadeService {

    private final UnidadeRepository repository;

    public Page<Unidade> listarTodos(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Optional<Unidade> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public Unidade salvar(Unidade unidade) {
        return repository.save(unidade);
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }
}
