package com.petshop.api.repository;

import com.petshop.api.model.Pet;
import com.petshop.api.model.PortePet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    Page<Pet> findByClienteId(Long clienteId, Pageable pageable);
    Page<Pet> findByEspecieIgnoreCase(String especie, Pageable pageable);
    Page<Pet> findByPorte(PortePet porte, Pageable pageable);
    Page<Pet> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
