package com.example.petshopapi.repository;

import com.example.petshopapi.model.Gerente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GerenteRepository extends JpaRepository<Gerente, Long> {
    boolean existsByUnidadeId(Long unidadeId);
    Optional<Gerente> findByUnidadeId(Long unidadeId);
}
