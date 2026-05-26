package com.petshop.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa uma unidade física do PetShop.
 * Relacionamento 1:1 com Gerente — cada unidade tem no máximo um gerente responsável.
 */
@Entity
@Table(name = "tb_unidades")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Unidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "O nome da unidade é obrigatório")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "O endereço é obrigatório")
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String endereco;

    @Size(max = 20)
    @Column(length = 20)
    private String telefone;

    @Column(name = "ativa")
    private Boolean ativa = true;
}
