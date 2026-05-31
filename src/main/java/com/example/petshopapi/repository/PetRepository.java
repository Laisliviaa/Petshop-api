package com.example.petshopapi.repository;

import com.example.petshopapi.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findByNomeContainingIgnoreCase(String nome);
    List<Pet> findByClienteId(Long clienteId);
}
