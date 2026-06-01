package com.example.petshopapi.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🐾 Petshop API")
                        .description("""
                                API REST para gestão de PetShop — Projeto Senac TSI.
                                
                                **Autenticação:** Use o header `X-API-Key` em POST, PUT e DELETE. GETs são públicos.
                                
                                | Chave | Role |
                                |---|---|
                                | `petshop-admin-key-2026` | ADMIN |
                                | `petshop-funcionario-key-2026` | FUNCIONARIO |
                                | `petshop-visitante-key-2026` | VISITANTE |
                                
                                **Versionamento:** Header `X-API-Version: 1` (padrão) ou `2` (com metadados extras).
                                
                                **Rate Limiting:** 10 GET / 5 escritas por 30s por IP. Excedeu → HTTP 429.
                                
                                **Idempotência:** Use `X-Idempotency-Key` em POST e PUT para evitar duplicidade.
                                """)
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("Senac TSI — Desenvolvimento de Web Services")
                                .email("senac@example.com"))
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
                .addServersItem(new Server()
                        .url("https://petshop-api-23cd.onrender.com")
                        .description("Servidor Render (produção)"))
                .addServersItem(new Server()
                        .url("http://localhost:8080")
                        .description("Servidor de desenvolvimento local"))
                .externalDocs(new ExternalDocumentation()
                        .description("README completo do projeto")
                        .url("https://github.com/senac-tsi/petshop-api#readme"))
                .addSecurityItem(new SecurityRequirement().addList("X-API-Key"))
                .components(new Components()
                        .addSecuritySchemes("X-API-Key",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("X-API-Key")
                                        .description("Chave de autenticação. "
                                                + "Gere uma nova em `POST /api/v1/apikeys` "
                                                + "ou use uma das chaves pré-carregadas acima.")));
    }
}
