package com.example.petshopapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testa o RestExceptionHandler garantindo que TODOS os tipos de erro
 * retornam o formato padronizado ApiErrorResponse.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("RestExceptionHandler — testes de tratamento de erro")
class ErrorHandlerTest {

    @Autowired MockMvc mvc;

    @Test
    @DisplayName("JSON malformado → 400 com ApiErrorResponse")
    void jsonMalformado_deveRetornar400() throws Exception {
        mvc.perform(post("/api/v1/clientes")
                .header("X-API-Key", "petshop-funcionario-key-2026")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{nome: invalido}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erro").value("Bad Request"))
                .andExpect(jsonPath("$.caminho").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Query param obrigatório ausente → 400 com mensagem sobre o parâmetro")
    void queryParamAusente_deveRetornar400() throws Exception {
        mvc.perform(get("/api/v1/pets/busca"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value(containsString("nome")));
    }

    @Test
    @DisplayName("Path param com tipo errado → 400 com ApiErrorResponse")
    void pathParamTipoErrado_deveRetornar400() throws Exception {
        mvc.perform(get("/api/v1/clientes/nao-e-um-numero"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value(containsString("id")));
    }

    @Test
    @DisplayName("Enum inválido em path param → 400 com ApiErrorResponse")
    void enumInvalido_deveRetornar400() throws Exception {
        mvc.perform(get("/api/v1/agendamentos/status/STATUS_INEXISTENTE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Content-Type errado → 415 com ApiErrorResponse (não formato padrão do Spring)")
    void contentTypeErrado_deveRetornar415() throws Exception {
        mvc.perform(post("/api/v1/clientes")
                .header("X-API-Key", "petshop-funcionario-key-2026")
                .contentType(MediaType.TEXT_PLAIN)
                .content("qualquer coisa"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.erro").value("Unsupported Media Type"))
                .andExpect(jsonPath("$.mensagem").exists())
                .andExpect(jsonPath("$.caminho").exists());
    }

    @Test
    @DisplayName("Idempotency-Key: mesmo payload → resposta cacheada (201 na segunda chamada)")
    void idempotencia_mesmoPayload_deveRetornarCacheado() throws Exception {
        // Usar UUID único por execução para não colidir com outros testes
        String key = "test-cache-" + UUID.randomUUID();
        String body = "{\"nome\":\"Idempotency Cache Teste\",\"cpf\":\"88877766655\"}";

        mvc.perform(post("/api/v1/clientes")
                .header("X-API-Key", "petshop-funcionario-key-2026")
                .header("X-Idempotency-Key", key)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated());

        // Segunda chamada com mesmo body e chave → deve retornar 201 cacheado
        mvc.perform(post("/api/v1/clientes")
                .header("X-API-Key", "petshop-funcionario-key-2026")
                .header("X-Idempotency-Key", key)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Idempotency-Key: payload diferente → 409 Conflict")
    void idempotencia_payloadDiferente_deveRetornar409() throws Exception {
        String key = "test-conflict-" + UUID.randomUUID();
        String body1 = "{\"nome\":\"Primeiro\",\"cpf\":\"55544433322\"}";
        String body2 = "{\"nome\":\"Segundo Diferente\",\"cpf\":\"55544433311\"}";

        mvc.perform(post("/api/v1/clientes")
                .header("X-API-Key", "petshop-funcionario-key-2026")
                .header("X-Idempotency-Key", key)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body1))
                .andExpect(status().isCreated());

        // Segunda chamada com chave igual mas body diferente → 409
        mvc.perform(post("/api/v1/clientes")
                .header("X-API-Key", "petshop-funcionario-key-2026")
                .header("X-Idempotency-Key", key)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body2))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value(containsString(key)));
    }

    @Test
    @DisplayName("Agendamento com data no passado → 422 Unprocessable Entity")
    void agendamentoDataPassado_deveRetornar422() throws Exception {
        String body = """
                {"petId":1,"servicoId":1,"unidadeId":1,"dataHora":"2020-01-01T10:00:00","status":"PENDENTE"}
                """;
        mvc.perform(post("/api/v1/agendamentos")
                .header("X-API-Key", "petshop-funcionario-key-2026")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.mensagem").value(containsString("passado")));
    }
}
