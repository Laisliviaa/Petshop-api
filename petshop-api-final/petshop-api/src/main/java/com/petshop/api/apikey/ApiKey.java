package com.petshop.api.apikey;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Chave de API para autenticação de clientes externos.
 */
@Entity
@Table(name = "tb_api_keys")
@Getter
@Setter
@NoArgsConstructor
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String chave;

    @Column(nullable = false, length = 100)
    private String nomeCliente;

    @Column(nullable = false)
    private Instant criadaEm = Instant.now();

    @Column(nullable = false)
    private boolean ativa = true;
}
