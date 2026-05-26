package com.petshop.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Pet cadastrado no PetShop.
 * <ul>
 *   <li>N:1 com Cliente — um pet pertence a exatamente um tutor</li>
 *   <li>N:N com Servico — um pet pode contratar vários serviços</li>
 *   <li>1:N com Agendamento — um pet pode ter vários agendamentos</li>
 * </ul>
 */
@Entity
@Table(name = "tb_pets")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "O nome do pet é obrigatório")
    @Size(min = 1, max = 80)
    @Column(nullable = false, length = 80)
    private String nome;

    @NotBlank(message = "A espécie é obrigatória")
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String especie;

    @Size(max = 80)
    @Column(length = 80)
    private String raca;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private PortePet porte;

    /** N:1 — um pet pertence a um único cliente. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    /**
     * N:N — pet é dono da tabela de junção.
     * CascadeType omitido: um serviço existe independentemente do pet.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tb_pets_servicos",
            joinColumns        = @JoinColumn(name = "pet_id"),
            inverseJoinColumns = @JoinColumn(name = "servico_id")
    )
    private List<Servico> servicos = new ArrayList<>();

    /** 1:N — um pet pode ter vários agendamentos ao longo do tempo. */
    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Agendamento> agendamentos = new ArrayList<>();
}
