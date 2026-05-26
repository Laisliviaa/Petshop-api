package com.petshop.api.infrastructure;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração completa do Swagger/OpenAPI 3.0.
 *
 * <ul>
 *   <li>Segurança via cabeçalho {@code X-API-Key}</li>
 *   <li>Servidor local e de produção documentados</li>
 *   <li>Informações de contato e licença</li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    private static final String API_KEY_SCHEME = "ApiKeyAuth";

    @Bean
    public OpenAPI petshopOpenAPI() {
        return new OpenAPI()
                .info(info())
                .externalDocs(externalDocs())
                .servers(servers())
                .addSecurityItem(new SecurityRequirement().addList(API_KEY_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(API_KEY_SCHEME, apiKeyScheme()));
    }

    // ── Info ─────────────────────────────────────────────────────────────────

    private Info info() {
        return new Info()
                .title("🐾 PetShop API")
                .version("1.0.0")
                .description("""
                        ## API REST do Sistema PetShop
                        
                        API completa para gerenciamento de um PetShop, desenvolvida com **Spring Boot 3** \
                        como projeto final do curso de TSI — Senac.
                        
                        ### Recursos disponíveis
                        | Recurso       | Descrição                                      |
                        |---------------|------------------------------------------------|
                        | Clientes      | Tutores dos pets cadastrados no sistema        |
                        | Pets          | Animais atendidos no PetShop                   |
                        | Serviços      | Banho, tosa, consulta veterinária, etc.        |
                        | Agendamentos  | Controle de datas e status dos atendimentos    |
                        | Unidades      | Filiais físicas do PetShop                     |
                        | Gerentes      | Responsáveis por cada unidade                  |
                        
                        ### Autenticação
                        Todos os endpoints (exceto `/api/v1/auth/keys`) exigem o cabeçalho:
                        ```
                        X-API-Key: pk_<sua-chave>
                        ```
                        Gere sua chave em **POST /api/v1/auth/keys**.
                        
                        ### Rate Limiting
                        A API aplica limite de **60 requisições por minuto** por IP.  
                        O cabeçalho `X-RateLimit-Remaining` indica o saldo restante.
                        
                        ### Idempotência
                        Os endpoints de criação (POST) suportam o cabeçalho `X-Idempotency-Key`  
                        para garantir que requisições repetidas não gerem duplicações.
                        
                        ### HATEOAS
                        Todas as respostas incluem links `_links` para navegação hipermídia (HAL).
                        """)
                .contact(new Contact()
                        .name("Equipe PetShop — Senac TSI")
                        .email("petshop@senac.br")
                        .url("https://github.com/Laisliviaa/Petshop-api"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    // ── External docs ─────────────────────────────────────────────────────────

    private ExternalDocumentation externalDocs() {
        return new ExternalDocumentation()
                .description("Repositório no GitHub")
                .url("https://github.com/Laisliviaa/Petshop-api");
    }

    // ── Servers ───────────────────────────────────────────────────────────────

    private List<Server> servers() {
        return List.of(
                new Server().url("http://localhost:8080").description("Ambiente local"),
                new Server().url("https://petshop-api.onrender.com").description("Produção (deploy)")
        );
    }

    // ── Security scheme ───────────────────────────────────────────────────────

    private SecurityScheme apiKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-API-Key")
                .description("Chave de API gerada em POST /api/v1/auth/keys");
    }
}
