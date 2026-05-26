package com.petshop.api.repository;

import com.petshop.api.model.Gerente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GerenteRepository extends JpaRepository<Gerente, Long> {
    Page<Gerente> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    boolean existsByCpf(String cpf);
}
