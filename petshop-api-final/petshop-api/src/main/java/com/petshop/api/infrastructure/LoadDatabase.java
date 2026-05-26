package com.petshop.api.infrastructure;

import com.petshop.api.apikey.ApiKey;
import com.petshop.api.apikey.ApiKeyRepository;
import com.petshop.api.model.*;
import com.petshop.api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Popula o banco H2 em memória com dados realistas ao iniciar a aplicação.
 * <p>
 * Dados criados:
 * <ul>
 *   <li>1 chave de API de demonstração</li>
 *   <li>3 unidades PetShop</li>
 *   <li>3 gerentes (1 por unidade)</li>
 *   <li>4 clientes (tutores)</li>
 *   <li>6 pets</li>
 *   <li>6 serviços</li>
 *   <li>5 agendamentos em diferentes status</li>
 * </ul>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class LoadDatabase {

    @Bean
    CommandLineRunner initDatabase(
            UnidadeRepository unidadeRepo,
            GerenteRepository gerenteRepo,
            ClienteRepository clienteRepo,
            PetRepository petRepo,
            ServicoRepository servicoRepo,
            AgendamentoRepository agendamentoRepo,
            ApiKeyRepository apiKeyRepo) {

        return args -> {
            log.info("═══════════════════════════════════════════════");
            log.info("   🐾  Iniciando carga de dados — PetShop API  ");
            log.info("═══════════════════════════════════════════════");

            // ── API Key demo ──────────────────────────────────────────────────
            ApiKey demoKey = new ApiKey();
            demoKey.setChave("pk_demo_petshop_senac_2025");
            demoKey.setNomeCliente("Demonstração Senac TSI");
            demoKey.setAtiva(true);
            apiKeyRepo.save(demoKey);
            log.info("✅  Chave de API demo: pk_demo_petshop_senac_2025");

            // ── Unidades ─────────────────────────────────────────────────────
            Unidade u1 = unidade("PetShop Centro",    "Av. Paulista, 1000 — Bela Vista, São Paulo/SP", "(11) 3333-1001");
            Unidade u2 = unidade("PetShop Vila Madalena", "R. Harmonia, 250 — Vila Madalena, São Paulo/SP", "(11) 3333-1002");
            Unidade u3 = unidade("PetShop Moema",     "Av. Ibirapuera, 400 — Moema, São Paulo/SP",     "(11) 3333-1003");
            unidadeRepo.saveAll(List.of(u1, u2, u3));

            // ── Gerentes ─────────────────────────────────────────────────────
            gerenteRepo.saveAll(List.of(
                    gerente("Carlos Eduardo Ferreira", "111.222.333-44", "carlos@petshop.com.br", u1),
                    gerente("Fernanda Lima Sousa",     "555.666.777-88", "fernanda@petshop.com.br", u2),
                    gerente("Ricardo Alves Mendes",    "999.000.111-22", "ricardo@petshop.com.br", u3)
            ));

            // ── Clientes ─────────────────────────────────────────────────────
            Cliente c1 = cliente("Maria Aparecida Oliveira", "123.456.789-00", "maria@email.com", "(11) 98888-0001");
            Cliente c2 = cliente("João Pedro Souza",         "234.567.890-11", "joao@email.com",  "(11) 98888-0002");
            Cliente c3 = cliente("Ana Clara Rodrigues",      "345.678.901-22", "ana@email.com",   "(11) 98888-0003");
            Cliente c4 = cliente("Lucas Martins Santos",     "456.789.012-33", "lucas@email.com", "(11) 98888-0004");
            clienteRepo.saveAll(List.of(c1, c2, c3, c4));

            // ── Pets ─────────────────────────────────────────────────────────
            Pet p1 = pet("Thor",    "Cachorro", "Labrador Retriever",  PortePet.GRANDE,   c1);
            Pet p2 = pet("Mia",     "Gato",     "Persa",               PortePet.PEQUENO,  c1);
            Pet p3 = pet("Bolinha", "Cachorro", "Poodle",              PortePet.MINI,     c2);
            Pet p4 = pet("Zeus",    "Cachorro", "Rottweiler",          PortePet.GIGANTE,  c3);
            Pet p5 = pet("Mel",     "Gato",     "Siamês",              PortePet.PEQUENO,  c3);
            Pet p6 = pet("Rex",     "Cachorro", "Golden Retriever",    PortePet.GRANDE,   c4);
            petRepo.saveAll(List.of(p1, p2, p3, p4, p5, p6));

            // ── Serviços ─────────────────────────────────────────────────────
            Servico s1 = servico("Banho Simples",         45.00,  "Higiene");
            Servico s2 = servico("Banho e Tosa Completa", 85.00,  "Higiene");
            Servico s3 = servico("Tosa na Tesoura",       70.00,  "Higiene");
            Servico s4 = servico("Consulta Veterinária",  120.00, "Veterinário");
            Servico s5 = servico("Vacinação Anual",       90.00,  "Veterinário");
            Servico s6 = servico("Creche Diária",         60.00,  "Hospedagem");
            servicoRepo.saveAll(List.of(s1, s2, s3, s4, s5, s6));

            // ── Agendamentos ─────────────────────────────────────────────────
            agendamentoRepo.saveAll(List.of(
                    agendamento(p1, s2, u1, LocalDateTime.now().plusDays(1),  StatusAgendamento.CONFIRMADO,
                            "Cliente solicitou shampoo hipoalergênico"),
                    agendamento(p2, s1, u1, LocalDateTime.now().plusDays(2),  StatusAgendamento.PENDENTE,
                            null),
                    agendamento(p3, s3, u2, LocalDateTime.now().plusDays(3),  StatusAgendamento.PENDENTE,
                            "Tosa estilo ursinho"),
                    agendamento(p4, s4, u3, LocalDateTime.now().minusDays(1), StatusAgendamento.CONCLUIDO,
                            "Check-up anual realizado"),
                    agendamento(p6, s5, u2, LocalDateTime.now().plusDays(5),  StatusAgendamento.CONFIRMADO,
                            "Vacina V10 + antirrábica")
            ));

            log.info("═══════════════════════════════════════════════");
            log.info("   ✅  Dados carregados com sucesso!           ");
            log.info("   📖  Swagger UI: http://localhost:8080/swagger-ui.html");
            log.info("   🗄️   H2 Console: http://localhost:8080/h2-console");
            log.info("   🔑  API Key demo: pk_demo_petshop_senac_2025");
            log.info("═══════════════════════════════════════════════");
        };
    }

    // ── builders ──────────────────────────────────────────────────────────────

    private Unidade unidade(String nome, String endereco, String telefone) {
        Unidade u = new Unidade();
        u.setNome(nome);
        u.setEndereco(endereco);
        u.setTelefone(telefone);
        u.setAtiva(true);
        return u;
    }

    private Gerente gerente(String nome, String cpf, String email, Unidade unidade) {
        Gerente g = new Gerente();
        g.setNome(nome);
        g.setCpf(cpf);
        g.setEmail(email);
        g.setUnidade(unidade);
        return g;
    }

    private Cliente cliente(String nome, String cpf, String email, String telefone) {
        Cliente c = new Cliente();
        c.setNome(nome);
        c.setCpf(cpf);
        c.setEmail(email);
        c.setTelefone(telefone);
        return c;
    }

    private Pet pet(String nome, String especie, String raca, PortePet porte, Cliente cliente) {
        Pet p = new Pet();
        p.setNome(nome);
        p.setEspecie(especie);
        p.setRaca(raca);
        p.setPorte(porte);
        p.setCliente(cliente);
        return p;
    }

    private Servico servico(String descricao, Double preco, String categoria) {
        Servico s = new Servico();
        s.setDescricao(descricao);
        s.setPreco(preco);
        s.setCategoria(categoria);
        return s;
    }

    private Agendamento agendamento(Pet pet, Servico servico, Unidade unidade,
                                    LocalDateTime dataHora, StatusAgendamento status,
                                    String observacoes) {
        Agendamento a = new Agendamento();
        a.setPet(pet);
        a.setServico(servico);
        a.setUnidade(unidade);
        a.setDataHora(dataHora);
        a.setStatus(status);
        a.setObservacoes(observacoes);
        return a;
    }
}
