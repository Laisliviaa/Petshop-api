package com.petshop.api.repository;

import com.petshop.api.model.Servico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {
    Page<Servico> findByCategoriaIgnoreCase(String categoria, Pageable pageable);
    Page<Servico> findByDescricaoContainingIgnoreCase(String descricao, Pageable pageable);
    boolean existsByDescricaoIgnoreCase(String descricao);
}
