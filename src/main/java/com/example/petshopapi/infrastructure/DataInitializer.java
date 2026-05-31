package com.example.petshopapi.infrastructure;

import com.example.petshopapi.apikey.ApiKey;
import com.example.petshopapi.apikey.ApiKeyRepository;
import com.example.petshopapi.model.*;
import com.example.petshopapi.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(
            ApiKeyRepository      apiKeyRepo,
            ClienteRepository     clienteRepo,
            PetRepository         petRepo,
            ServicoRepository     servicoRepo,
            UnidadeRepository     unidadeRepo,
            GerenteRepository     gerenteRepo,
            AgendamentoRepository agendamentoRepo) {

        return args -> {

            // ── API Keys ─────────────────────────────────────────────────────
            ApiKey admin = new ApiKey();
            admin.setKeyValue("petshop-admin-key-2026");
            admin.setClientName("Admin Geral");
            admin.setRole(ApiKey.Role.ADMIN);

            ApiKey func = new ApiKey();
            func.setKeyValue("petshop-funcionario-key-2026");
            func.setClientName("Funcionário Padrão");
            func.setRole(ApiKey.Role.FUNCIONARIO);

            ApiKey visit = new ApiKey();
            visit.setKeyValue("petshop-visitante-key-2026");
            visit.setClientName("Visitante Padrão");
            visit.setRole(ApiKey.Role.VISITANTE);

            apiKeyRepo.saveAll(List.of(admin, func, visit));

            // ── Unidades ─────────────────────────────────────────────────────
            Unidade u1 = new Unidade(); u1.setNome("Unidade Centro");     u1.setEndereco("Rua das Flores, 100 — Centro");
            Unidade u2 = new Unidade(); u2.setNome("Unidade Zona Sul");   u2.setEndereco("Av. Paulista, 500 — Bela Vista");
            Unidade u3 = new Unidade(); u3.setNome("Unidade Zona Norte"); u3.setEndereco("Rua dos Pinheiros, 200 — Santana");
            u1 = unidadeRepo.save(u1); u2 = unidadeRepo.save(u2); u3 = unidadeRepo.save(u3);

            // ── Gerentes (One-to-One com Unidade) ────────────────────────────
            Gerente g1 = new Gerente(); g1.setNome("Maria Souza");  g1.setUnidade(u1);
            Gerente g2 = new Gerente(); g2.setNome("João Pereira"); g2.setUnidade(u2);
            Gerente g3 = new Gerente(); g3.setNome("Ana Ferreira"); g3.setUnidade(u3);
            gerenteRepo.saveAll(List.of(g1, g2, g3));

            // ── Serviços ─────────────────────────────────────────────────────
            Servico s1 = new Servico(); s1.setDescricao("Banho e Tosa");         s1.setPreco(80.0);
            Servico s2 = new Servico(); s2.setDescricao("Consulta Veterinária"); s2.setPreco(150.0);
            Servico s3 = new Servico(); s3.setDescricao("Vacinação");             s3.setPreco(60.0);
            Servico s4 = new Servico(); s4.setDescricao("Adestramento");          s4.setPreco(200.0);
            Servico s5 = new Servico(); s5.setDescricao("Hospedagem Diária");     s5.setPreco(120.0);
            s1 = servicoRepo.save(s1); s2 = servicoRepo.save(s2); s3 = servicoRepo.save(s3);
            s4 = servicoRepo.save(s4); s5 = servicoRepo.save(s5);

            // ── Clientes ─────────────────────────────────────────────────────
            Cliente c1 = new Cliente(); c1.setNome("Carlos Lima");    c1.setCpf("11122233344");
            Cliente c2 = new Cliente(); c2.setNome("Fernanda Costa"); c2.setCpf("22233344455");
            Cliente c3 = new Cliente(); c3.setNome("Roberto Alves");  c3.setCpf("33344455566");
            Cliente c4 = new Cliente(); c4.setNome("Patrícia Nunes"); c4.setCpf("44455566677");
            c1 = clienteRepo.save(c1); c2 = clienteRepo.save(c2);
            c3 = clienteRepo.save(c3); c4 = clienteRepo.save(c4);

            // ── Pets (Many-to-One com Cliente; Many-to-Many com Serviços) ────
            Pet p1 = new Pet(); p1.setNome("Rex");     p1.setEspecie("Cachorro"); p1.setCliente(c1); p1.setServicos(List.of(s1, s2));
            Pet p2 = new Pet(); p2.setNome("Mimi");    p2.setEspecie("Gato");     p2.setCliente(c1); p2.setServicos(List.of(s3));
            Pet p3 = new Pet(); p3.setNome("Bolinha"); p3.setEspecie("Cachorro"); p3.setCliente(c2); p3.setServicos(List.of(s1, s4));
            Pet p4 = new Pet(); p4.setNome("Thor");    p4.setEspecie("Cachorro"); p4.setCliente(c3); p4.setServicos(List.of(s2, s5));
            Pet p5 = new Pet(); p5.setNome("Luna");    p5.setEspecie("Gato");     p5.setCliente(c4); p5.setServicos(List.of(s3));
            Pet p6 = new Pet(); p6.setNome("Bob");     p6.setEspecie("Cachorro"); p6.setCliente(c4); p6.setServicos(List.of(s1));
            p1 = petRepo.save(p1); p2 = petRepo.save(p2); p3 = petRepo.save(p3);
            p4 = petRepo.save(p4); p5 = petRepo.save(p5); p6 = petRepo.save(p6);

            // ── Agendamentos (Many-to-One com Pet, Serviço e Unidade) ─────────
            Agendamento a1 = new Agendamento();
            a1.setPet(p1); a1.setServico(s1); a1.setUnidade(u1);
            a1.setDataHora(LocalDateTime.of(2026, 6, 10, 9, 0));
            a1.setStatus(StatusAgendamento.PENDENTE);
            a1.setObservacoes("Rex é agitado na tosa");

            Agendamento a2 = new Agendamento();
            a2.setPet(p2); a2.setServico(s3); a2.setUnidade(u2);
            a2.setDataHora(LocalDateTime.of(2026, 6, 11, 14, 0));
            a2.setStatus(StatusAgendamento.PENDENTE);

            Agendamento a3 = new Agendamento();
            a3.setPet(p3); a3.setServico(s4); a3.setUnidade(u1);
            a3.setDataHora(LocalDateTime.of(2026, 6, 9, 10, 30));
            a3.setStatus(StatusAgendamento.CONCLUIDO);

            Agendamento a4 = new Agendamento();
            a4.setPet(p4); a4.setServico(s2); a4.setUnidade(u3);
            a4.setDataHora(LocalDateTime.of(2026, 6, 12, 8, 0));
            a4.setStatus(StatusAgendamento.PENDENTE);
            a4.setObservacoes("Primeira consulta veterinária");

            Agendamento a5 = new Agendamento();
            a5.setPet(p5); a5.setServico(s3); a5.setUnidade(u2);
            a5.setDataHora(LocalDateTime.of(2026, 6, 8, 16, 0));
            a5.setStatus(StatusAgendamento.CANCELADO);
            a5.setObservacoes("Cliente cancelou — reagendar");

            agendamentoRepo.saveAll(List.of(a1, a2, a3, a4, a5));

            log.info("=================================================");
            log.info("  🐾 Petshop API iniciada com sucesso!");
            log.info("  Swagger UI : http://localhost:8080/swagger-ui.html");
            log.info("  H2 Console : http://localhost:8080/h2-console");
            log.info("  API Docs   : http://localhost:8080/v3/api-docs");
            log.info("─────────────────────────────────────────────────");
            log.info("  Chaves de acesso:");
            log.info("    ADMIN       → petshop-admin-key-2026");
            log.info("    FUNCIONARIO → petshop-funcionario-key-2026");
            log.info("    VISITANTE   → petshop-visitante-key-2026");
            log.info("─────────────────────────────────────────────────");
            log.info("  Versionamento: header X-API-Version: 1 ou 2");
            log.info("=================================================");
        };
    }
}
