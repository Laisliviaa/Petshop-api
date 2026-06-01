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
                                API REST de gestão de PetShop — Projeto Senac TSI.

                                ---

                                ## Autenticação

                                Use o header `X-API-Key` em todas as operações de escrita (POST, PUT, DELETE).
                                Endpoints `GET` são públicos.

                                **Chaves pré-carregadas:**
                                | Chave | Role | Permissões |
                                |---|---|---|
                                | `petshop-admin-key-2026` | ADMIN | GET + POST + PUT + DELETE |
                                | `petshop-funcionario-key-2026` | FUNCIONARIO | GET + POST + PUT |
                                | `petshop-visitante-key-2026` | VISITANTE | somente GET |

                                ---

                                ## Versionamento por Header

                                Esta API utiliza **versionamento por header** (`X-API-Version`), que é a abordagem
                                recomendada para APIs REST pois mantém as URLs estáveis e desacopla a versão
                                do endereço do recurso.

                                | Valor do header | Comportamento |
                                |---|---|
                                | `1` ou ausente | Resposta padrão com HATEOAS (padrão) |
                                | `2` | Resposta enriquecida com metadados de paginação |
                                | Qualquer outro valor | HTTP 400 Bad Request |

                                ---

                                ## Rate Limiting

                                | Tipo | Limite |
                                |---|---|
                                | GET (leitura) | 10 req / 30s por IP |
                                | POST / PUT / DELETE (escrita) | 5 req / 30s por IP |

                                Ao exceder: HTTP 429 com header `Retry-After` informando segundos até reset.

                                ---

                                ## Idempotência

                                Endpoints de escrita suportam o header `X-Idempotency-Key` (UUID):

                                | Cenário | Comportamento |
                                |---|---|
                                | Chave nova | Processa normalmente e armazena resultado |
                                | Mesma chave + mesmo payload | Retorna resposta original (sem reprocessar) |
                                | Mesma chave + payload diferente | HTTP 409 Conflict |

                                ---

                                ## Formato de Erros

                                Todos os erros seguem o mesmo formato JSON (`ApiErrorResponse`):
```json
                                {
                                  "timestamp": "2026-06-10T09:00:00",
                                  "status": 404,
                                  "erro": "Not Found",
                                  "mensagem": "Cliente com ID 99 não encontrado.",
                                  "caminho": "/api/v1/clientes/99",
                                  "metodo": "GET",
                                  "detalhes": null
                                }
```
                                O campo `detalhes` é preenchido apenas em erros de validação (HTTP 400),
                                listando cada campo inválido.
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
