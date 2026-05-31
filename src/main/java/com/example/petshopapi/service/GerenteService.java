package com.example.petshopapi.service;

import com.example.petshopapi.exception.ConflictException;
import com.example.petshopapi.exception.RecursoNaoEncontradoException;
import com.example.petshopapi.model.Gerente;
import com.example.petshopapi.model.Unidade;
import com.example.petshopapi.repository.GerenteRepository;
import com.example.petshopapi.repository.UnidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GerenteService {

    private final GerenteRepository gerenteRepository;
    private final UnidadeRepository unidadeRepository;

    public Page<Gerente> listarTodos(Pageable pageable) {
        return gerenteRepository.findAll(pageable);
    }

    public Optional<Gerente> buscarPorId(Long id) {
        return gerenteRepository.findById(id);
    }

    public Unidade buscarUnidade(Long unidadeId) {
        return unidadeRepository.findById(unidadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Unidade", unidadeId));
    }

    public Gerente salvar(Gerente gerente) {
        // CORRIGIDO: validação do relacionamento One-to-One
        // Impede que uma unidade tenha mais de um gerente
        if (gerente.getId() == null) {
            // Criação: unidade não pode ter gerente existente
            if (gerente.getUnidade() != null
                    && gerenteRepository.existsByUnidadeId(gerente.getUnidade().getId())) {
                throw new ConflictException(
                        "A unidade '" + gerente.getUnidade().getNome()
                        + "' já possui um gerente cadastrado. "
                        + "Remova o gerente atual antes de atribuir um novo.");
            }
        } else {
            // Atualização: verifica se outra instância de Gerente já usa essa unidade
            gerenteRepository.findByUnidadeId(gerente.getUnidade().getId())
                    .ifPresent(existente -> {
                        if (!existente.getId().equals(gerente.getId())) {
                            throw new ConflictException(
                                    "A unidade '" + gerente.getUnidade().getNome()
                                    + "' já está associada ao gerente '"
                                    + existente.getNome() + "'.");
                        }
                    });
        }
        return gerenteRepository.save(gerente);
    }

    public void deletar(Long id) {
        gerenteRepository.deleteById(id);
    }
}
