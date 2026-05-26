package com.petshop.api.repository;

import com.petshop.api.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByCpf(String cpf);
    Page<Cliente> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    boolean existsByCpf(String cpf);
}
