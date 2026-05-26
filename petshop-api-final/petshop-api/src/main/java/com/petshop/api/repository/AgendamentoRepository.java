package com.petshop.api.repository;

import com.petshop.api.model.Agendamento;
import com.petshop.api.model.StatusAgendamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    Page<Agendamento> findByPetId(Long petId, Pageable pageable);
    Page<Agendamento> findByStatus(StatusAgendamento status, Pageable pageable);
    Page<Agendamento> findByUnidadeId(Long unidadeId, Pageable pageable);
}
