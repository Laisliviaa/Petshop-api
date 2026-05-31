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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("GerenteController — testes de integração (One-to-One)")
class GerenteControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private static final String ADMIN_KEY = "petshop-admin-key-2026";
    private static final String FUNC_KEY  = "petshop-funcionario-key-2026";

    @Test
    @DisplayName("GET /gerentes → 200")
    void listar_deveRetornar200() throws Exception {
        mvc.perform(get("/api/v1/gerentes"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /gerentes com unidade já ocupada → 409 One-to-One")
    void criar_unidadeJaTemGerente_deveRetornar409() throws Exception {
        // Unidade 1 já tem gerente no DataInitializer
        var body = Map.of("nome", "Gerente Duplicado", "unidadeId", 1);
        mvc.perform(post("/api/v1/gerentes")
                .header("X-API-Key", FUNC_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value(containsString("gerente")));
    }

    @Test
    @DisplayName("POST /gerentes com unidade livre → 201")
    void criar_unidadeLivre_deveRetornar201() throws Exception {
        // Criar nova unidade sem gerente
        var unidade = Map.of("nome", "Unidade Teste", "endereco", "Rua Teste, 1");
        var resUnidade = mvc.perform(post("/api/v1/unidades")
                .header("X-API-Key", FUNC_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(unidade)))
                .andExpect(status().isCreated())
                .andReturn();

        var unidadeId = mapper.readTree(
                resUnidade.getResponse().getContentAsString()).get("id").asLong();

        var body = Map.of("nome", "Novo Gerente", "unidadeId", unidadeId);
        mvc.perform(post("/api/v1/gerentes")
                .header("X-API-Key", FUNC_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Novo Gerente"));
    }

    @Test
    @DisplayName("DELETE /gerentes/9999 → 404")
    void deletar_naoExistente_deveRetornar404() throws Exception {
        mvc.perform(delete("/api/v1/gerentes/9999")
                .header("X-API-Key", ADMIN_KEY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
