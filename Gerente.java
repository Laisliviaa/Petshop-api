package com.example.petshopapi;

import com.example.petshopapi.model.Cliente;
import com.example.petshopapi.repository.ClienteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
@DisplayName("ClienteController — testes de integração")
class ClienteControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired ClienteRepository clienteRepository;

    private static final String ADMIN_KEY = "petshop-admin-key-2026";
    private static final String FUNC_KEY  = "petshop-funcionario-key-2026";

    @BeforeEach
    void limparClientes() {
        // Remove clientes criados nos testes anteriores para evitar conflito de CPF
        clienteRepository.findByCpf("99988877766").ifPresent(clienteRepository::delete);
        clienteRepository.findByCpf("11111111111").ifPresent(clienteRepository::delete);
    }

    // ─── GET /api/v1/clientes ─────────────────────────────────────────────────

    @Test
    @DisplayName("GET /clientes → 200 com lista paginada (público)")
    void listarClientes_deveRetornar200() throws Exception {
        mvc.perform(get("/api/v1/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.clienteResponseList").isArray());
    }

    @Test
    @DisplayName("GET /clientes com X-API-Version: 2 → 200 com metadados extras")
    void listarClientesV2_deveRetornarMetadados() throws Exception {
        mvc.perform(get("/api/v1/clientes")
                .header("X-API-Version", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apiVersion").value("2"))
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /clientes com X-API-Version inválida → 400")
    void listarClientes_versaoInvalida_deveRetornar400() throws Exception {
        mvc.perform(get("/api/v1/clientes")
                .header("X-API-Version", "99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erro").value("Bad Request"))
                .andExpect(jsonPath("$.mensagem").exists())
                .andExpect(jsonPath("$.caminho").exists());
    }

    // ─── GET /api/v1/clientes/{id} ────────────────────────────────────────────

    @Test
    @DisplayName("GET /clientes/1 → 200 com HATEOAS links")
    void buscarClientePorId_deveRetornar200() throws Exception {
        mvc.perform(get("/api/v1/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.collection").exists());
    }

    @Test
    @DisplayName("GET /clientes/9999 → 404 com corpo ApiErrorResponse")
    void buscarClientePorId_naoExistente_deveRetornar404() throws Exception {
        mvc.perform(get("/api/v1/clientes/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Not Found"))
                .andExpect(jsonPath("$.mensagem").value(containsString("9999")))
                .andExpect(jsonPath("$.caminho").value("/api/v1/clientes/9999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("GET /clientes/abc → 400 (id não numérico)")
    void buscarClientePorId_idInvalido_deveRetornar400() throws Exception {
        mvc.perform(get("/api/v1/clientes/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ─── GET /api/v1/clientes/cpf/{cpf} ──────────────────────────────────────

    @Test
    @DisplayName("GET /clientes/cpf/11122233344 → 200")
    void buscarPorCpf_deveRetornar200() throws Exception {
        mvc.perform(get("/api/v1/clientes/cpf/11122233344"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("11122233344"));
    }

    @Test
    @DisplayName("GET /clientes/cpf/00000000000 → 404 CPF inexistente")
    void buscarPorCpf_cpfInexistente_deveRetornar404() throws Exception {
        mvc.perform(get("/api/v1/clientes/cpf/00000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ─── GET /api/v1/clientes/busca?nome= ────────────────────────────────────

    @Test
    @DisplayName("GET /clientes/busca?nome=Carlos → 200 com resultados")
    void buscarPorNome_deveRetornar200() throws Exception {
        mvc.perform(get("/api/v1/clientes/busca").param("nome", "Carlos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.clienteResponseList[0].nome",
                        containsStringIgnoringCase("Carlos")));
    }

    @Test
    @DisplayName("GET /clientes/busca sem parâmetro → 400 com ApiErrorResponse")
    void buscarPorNome_semParam_deveRetornar400() throws Exception {
        mvc.perform(get("/api/v1/clientes/busca"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value(containsString("nome")));
    }

    // ─── POST /api/v1/clientes ────────────────────────────────────────────────

    @Test
    @DisplayName("POST /clientes → 201 com Location e cliente criado")
    void criarCliente_deveRetornar201() throws Exception {
        var body = Map.of("nome", "Novo Cliente Teste", "cpf", "99988877766");
        mvc.perform(post("/api/v1/clientes")
                .header("X-API-Key", FUNC_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.nome").value("Novo Cliente Teste"))
                .andExpect(jsonPath("$.cpf").value("99988877766"));
    }

    @Test
    @DisplayName("POST /clientes sem X-API-Key → 401")
    void criarCliente_semApiKey_deveRetornar401() throws Exception {
        var body = Map.of("nome", "Teste", "cpf", "11111111111");
        mvc.perform(post("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /clientes com CPF duplicado → 409")
    void criarCliente_cpfDuplicado_deveRetornar409() throws Exception {
        var body = Map.of("nome", "Clone", "cpf", "11122233344");
        mvc.perform(post("/api/v1/clientes")
                .header("X-API-Key", FUNC_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value(containsString("11122233344")));
    }

    @Test
    @DisplayName("POST /clientes com dados inválidos → 400 com detalhes de campo")
    void criarCliente_dadosInvalidos_deveRetornar400() throws Exception {
        var body = Map.of("nome", "", "cpf", "123");
        mvc.perform(post("/api/v1/clientes")
                .header("X-API-Key", FUNC_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detalhes").isArray())
                .andExpect(jsonPath("$.detalhes", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("POST /clientes com role VISITANTE → 403")
    void criarCliente_roleInsuficiente_deveRetornar403() throws Exception {
        var body = Map.of("nome", "Teste", "cpf", "11111111111");
        mvc.perform(post("/api/v1/clientes")
                .header("X-API-Key", "petshop-visitante-key-2026")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    // ─── DELETE /api/v1/clientes/{id} ────────────────────────────────────────

    @Test
    @DisplayName("DELETE /clientes/{id} com FUNCIONARIO → 403 (só ADMIN pode deletar)")
    void deletarCliente_roleFunc_deveRetornar403() throws Exception {
        mvc.perform(delete("/api/v1/clientes/1")
                .header("X-API-Key", FUNC_KEY))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /clientes/9999 com ADMIN → 404")
    void deletarCliente_naoExistente_deveRetornar404() throws Exception {
        mvc.perform(delete("/api/v1/clientes/9999")
                .header("X-API-Key", ADMIN_KEY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
