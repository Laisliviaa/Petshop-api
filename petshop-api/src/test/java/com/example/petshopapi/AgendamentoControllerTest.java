package com.example.petshopapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("AgendamentoController — testes de integração")
class AgendamentoControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private static final String ADMIN_KEY = "petshop-admin-key-2026";
    private static final String FUNC_KEY  = "petshop-funcionario-key-2026";

    @Test
    @DisplayName("GET /agendamentos → 200 paginado")
    void listar_deveRetornar200() throws Exception {
        mvc.perform(get("/api/v1/agendamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.agendamentoResponseList").isArray());
    }

    @Test
    @DisplayName("GET /agendamentos/status/PENDENTE → 200 com lista")
    void buscarPorStatus_pendente_deveRetornar200() throws Exception {
        mvc.perform(get("/api/v1/agendamentos/status/PENDENTE"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /agendamentos/status/INVALIDO → 400")
    void buscarPorStatus_invalido_deveRetornar400() throws Exception {
        mvc.perform(get("/api/v1/agendamentos/status/INVALIDO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /agendamentos/unidade/1 → 200")
    void buscarPorUnidade_existente_deveRetornar200() throws Exception {
        mvc.perform(get("/api/v1/agendamentos/unidade/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /agendamentos/unidade/9999 → 404")
    void buscarPorUnidade_inexistente_deveRetornar404() throws Exception {
        mvc.perform(get("/api/v1/agendamentos/unidade/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /agendamentos/pet/9999 → 404")
    void buscarPorPet_inexistente_deveRetornar404() throws Exception {
        mvc.perform(get("/api/v1/agendamentos/pet/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /agendamentos → 201 com HATEOAS links")
    void criar_deveRetornar201() throws Exception {
        var body = Map.of(
                "petId", 1, "servicoId", 1, "unidadeId", 1,
                "dataHora", "2026-08-01T10:00:00", "status", "PENDENTE");
        mvc.perform(post("/api/v1/agendamentos")
                .header("X-API-Key", FUNC_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.update").exists())
                .andExpect(jsonPath("$._links.delete").exists());
    }

    @Test
    @DisplayName("POST /agendamentos com petId inexistente → 404")
    void criar_petInexistente_deveRetornar404() throws Exception {
        var body = Map.of(
                "petId", 9999, "servicoId", 1, "unidadeId", 1,
                "dataHora", "2026-08-01T10:00:00");
        mvc.perform(post("/api/v1/agendamentos")
                .header("X-API-Key", FUNC_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("DELETE /agendamentos/{id} com role FUNCIONARIO → 403")
    void deletar_roleFunc_deveRetornar403() throws Exception {
        mvc.perform(delete("/api/v1/agendamentos/1")
                .header("X-API-Key", FUNC_KEY))
                .andExpect(status().isForbidden());
    }
}
