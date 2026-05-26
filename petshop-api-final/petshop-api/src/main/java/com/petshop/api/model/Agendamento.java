package com.petshop.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Agendamento de um serviço para um pet em uma unidade do PetShop.
 * <ul>
 *   <li>N:1 com Pet</li>
 *   <li>N:1 com Servico</li>
 *   <li>N:1 com Unidade</li>
 * </ul>
 */
@Entity
@Table(name = "tb_agendamentos")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull(message = "A data e hora do agendamento são obrigatórias")
    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusAgendamento status = StatusAgendamento.PENDENTE;

    /** N:1 — o pet que será atendido neste agendamento. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id")
    private Pet pet;

    /** N:1 — serviço contratado neste agendamento. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_id")
    private Servico servico;

    /** N:1 — unidade onde o atendimento ocorrerá. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id")
    private Unidade unidade;

    @Column(length = 300)
    private String observacoes;
}
