package com.petshop.api.service;

import com.petshop.api.dto.request.AgendamentoRequest;
import com.petshop.api.dto.response.AgendamentoResponse;
import com.petshop.api.exception.BusinessException;
import com.petshop.api.exception.RecursoNaoEncontradoException;
import com.petshop.api.model.*;
import com.petshop.api.repository.AgendamentoRepository;
import com.petshop.api.repository.PetRepository;
import com.petshop.api.repository.ServicoRepository;
import com.petshop.api.repository.UnidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository repository;
    private final PetRepository petRepository;
    private final ServicoRepository servicoRepository;
    private final UnidadeRepository unidadeRepository;

    @Transactional(readOnly = true)
    public Page<AgendamentoResponse> listarTodos(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AgendamentoResponse> listarPorPet(Long petId, Pageable pageable) {
        return repository.findByPetId(petId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AgendamentoResponse> listarPorStatus(StatusAgendamento status, Pageable pageable) {
        return repository.findByStatus(status, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AgendamentoResponse buscarPorId(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public AgendamentoResponse criar(AgendamentoRequest request) {
        Pet pet = petRepository.findById(request.getPetId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pet", request.getPetId()));
        Servico servico = servicoRepository.findById(request.getServicoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço", request.getServicoId()));
        Unidade unidade = unidadeRepository.findById(request.getUnidadeId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Unidade", request.getUnidadeId()));

        if (!Boolean.TRUE.equals(unidade.getAtiva())) {
            throw new BusinessException("A unidade '" + unidade.getNome() + "' está inativa e não aceita agendamentos.");
        }

        Agendamento a = new Agendamento();
        a.setDataHora(request.getDataHora());
        a.setPet(pet);
        a.setServico(servico);
        a.setUnidade(unidade);
        a.setObservacoes(request.getObservacoes());
        a.setStatus(StatusAgendamento.PENDENTE);

        return toResponse(repository.save(a));
    }

    @Transactional
    public AgendamentoResponse atualizarStatus(Long id, StatusAgendamento novoStatus) {
        Agendamento a = findOrThrow(id);
        if (a.getStatus() == StatusAgendamento.CANCELADO) {
            throw new BusinessException("Não é possível alterar o status de um agendamento já cancelado.");
        }
        if (a.getStatus() == StatusAgendamento.CONCLUIDO) {
            throw new BusinessException("Não é possível alterar o status de um agendamento já concluído.");
        }
        a.setStatus(novoStatus);
        return toResponse(repository.save(a));
    }

    @Transactional
    public AgendamentoResponse cancelar(Long id) {
        return atualizarStatus(id, StatusAgendamento.CANCELADO);
    }

    @Transactional
    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Agendamento", id);
        }
        repository.deleteById(id);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Agendamento findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento", id));
    }

    public AgendamentoResponse toResponse(Agendamento a) {
        String nomePet    = a.getPet()     != null ? a.getPet().getNome()          : null;
        String nomeCliente= a.getPet()     != null && a.getPet().getCliente() != null
                            ? a.getPet().getCliente().getNome() : null;
        String servico    = a.getServico() != null ? a.getServico().getDescricao() : null;
        String unidade    = a.getUnidade() != null ? a.getUnidade().getNome()      : null;

        return new AgendamentoResponse(
                a.getId(),
                a.getDataHora(),
                a.getStatus(),
                nomePet,
                nomeCliente,
                servico,
                unidade,
                a.getObservacoes()
        );
    }
}
