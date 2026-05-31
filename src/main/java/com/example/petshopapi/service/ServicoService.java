package com.example.petshopapi.service;

import com.example.petshopapi.model.Servico;
import com.example.petshopapi.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository repository;

    public Page<Servico> listarTodos(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Optional<Servico> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public List<Servico> buscarPorDescricao(String descricao) {
        return repository.findByDescricaoContainingIgnoreCase(descricao);
    }

    public Servico salvar(Servico servico) {
        return repository.save(servico);
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }
}
