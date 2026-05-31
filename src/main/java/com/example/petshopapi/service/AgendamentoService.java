package com.example.petshopapi.service;

import com.example.petshopapi.exception.BusinessException;
import com.example.petshopapi.exception.RecursoNaoEncontradoException;
import com.example.petshopapi.model.*;
import com.example.petshopapi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final PetRepository         petRepository;
    private final ServicoRepository     servicoRepository;
    private final UnidadeRepository     unidadeRepository;

    public Page<Agendamento> listarTodos(Pageable pageable) {
        return agendamentoRepository.findAll(pageable);
    }

    public Optional<Agendamento> buscarPorId(Long id) {
        return agendamentoRepository.findById(id);
    }

    public List<Agendamento> buscarPorStatus(StatusAgendamento status) {
        return agendamentoRepository.findByStatus(status);
    }

    public List<Agendamento> buscarPorUnidade(Long unidadeId) {
        if (!unidadeRepository.existsById(unidadeId))
            throw new RecursoNaoEncontradoException("Unidade", unidadeId);
        return agendamentoRepository.findByUnidadeId(unidadeId);
    }

    public List<Agendamento> buscarPorPet(Long petId) {
        if (!petRepository.existsById(petId))
            throw new RecursoNaoEncontradoException("Pet", petId);
        return agendamentoRepository.findByPetId(petId);
    }

    public Pet buscarPet(Long petId) {
        return petRepository.findById(petId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pet", petId));
    }

    public Servico buscarServico(Long servicoId) {
        return servicoRepository.findById(servicoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Servico", servicoId));
    }

    public Unidade buscarUnidade(Long unidadeId) {
        return unidadeRepository.findById(unidadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Unidade", unidadeId));
    }

    public Agendamento salvar(Agendamento agendamento) {
        // REGRA DE NEGÓCIO: agendamento não pode ser criado com data no passado
        if (agendamento.getId() == null && agendamento.getDataHora() != null
                && agendamento.getDataHora().isBefore(LocalDateTime.now())) {
            throw new BusinessException(
                    "Não é possível criar um agendamento com data no passado. "
                    + "Informe uma data e hora futuras.");
        }
        // REGRA DE NEGÓCIO: não é possível alterar status de CANCELADO para outro
        if (agendamento.getId() != null) {
            agendamentoRepository.findById(agendamento.getId()).ifPresent(existente -> {
                if (existente.getStatus() == StatusAgendamento.CANCELADO
                        && agendamento.getStatus() != StatusAgendamento.CANCELADO) {
                    throw new BusinessException(
                            "Não é possível reativar um agendamento já cancelado. "
                            + "Crie um novo agendamento.");
                }
            });
        }
        return agendamentoRepository.save(agendamento);
    }

    public void deletar(Long id) {
        agendamentoRepository.deleteById(id);
    }
}
