package com.petshop.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Serviço oferecido pelo PetShop (ex.: banho, tosa, consulta veterinária).
 * Relacionamento N:N com Pet via tabela de junção.
 */
@Entity
@Table(name = "tb_servicos")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "A descrição do serviço é obrigatória")
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
    private String descricao;

    @NotNull(message = "O preço é obrigatório")
    @Positive(message = "O preço deve ser maior que zero")
    @Column(nullable = false)
    private Double preco;

    @Size(max = 50)
    @Column(length = 50)
    private String categoria;

    /**
     * N:N — um serviço pode ser contratado por vários pets.
     * Lado não-dono — mapeado pelo campo "servicos" em Pet.
     */
    @ManyToMany(mappedBy = "servicos", fetch = FetchType.LAZY)
    private List<Pet> pets = new ArrayList<>();
}
