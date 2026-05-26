# 🐾 PetShop API

> **Projeto Final — Senac TSI · Desenvolvimento de Web Services**  
> API REST profissional desenvolvida com **Spring Boot 3.2.4** e **Java 17**.

---

## 📋 Sobre o Projeto

Sistema completo de gerenciamento para PetShop, contemplando:

| Recurso | Endpoints | Relacionamento |
|---|---|---|
| **Clientes** | GET, POST, PUT, DELETE | 1:N com Pets |
| **Pets** | GET, POST, PUT, DELETE | N:1 Cliente · N:N Serviços · 1:N Agendamentos |
| **Serviços** | GET, POST, PUT, DELETE | N:N com Pets |
| **Agendamentos** | GET, POST, PATCH status, DELETE | N:1 Pet · N:1 Serviço · N:1 Unidade |
| **Unidades** | GET, POST, PUT, DELETE | 1:1 com Gerente |
| **Gerentes** | GET, POST, PUT, DELETE | 1:1 com Unidade |
| **Auth (API Keys)** | GET, POST, DELETE | — |

---

## 🚀 Como executar

### Pré-requisitos
- Java 17+
- Maven 3.8+

### Passos

```bash
# 1. Clone ou descompacte o projeto
cd petshop-api

# 2. Compile e inicie
./mvnw spring-boot:run
# ou no Windows:
mvnw.cmd spring-boot:run
```

### URLs disponíveis

| Interface | URL |
|---|---|
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| **OpenAPI JSON** | http://localhost:8080/v3/api-docs |
| **H2 Console** | http://localhost:8080/h2-console |

### Credenciais H2 Console
- **JDBC URL:** `jdbc:h2:mem:petshopdb`
- **User:** `sa`
- **Password:** *(vazio)*

---

## 🔑 Autenticação

Todos os endpoints da API exigem o cabeçalho `X-API-Key`.

**Chave de demonstração (carregada automaticamente):**
```
X-API-Key: pk_demo_petshop_senac_2025
```

Para gerar novas chaves:
```http
POST /api/v1/auth/keys
Content-Type: application/json

{
  "nomeCliente": "Meu Sistema"
}
```

---

## ⚡ Funcionalidades Técnicas

### HATEOAS
Todas as respostas incluem links `_links` (HAL) para navegação hipermídia.

### Paginação
Todos os GETs de coleção suportam parâmetros:
```
?page=0&size=10&sort=nome,asc
```

### Rate Limiting
- **60 requisições por minuto** por IP
- Cabeçalhos: `X-RateLimit-Limit`, `X-RateLimit-Remaining`
- HTTP **429** com `Retry-After: 60` quando excedido

### Idempotência
POSTs suportam `X-Idempotency-Key` — reenvie com a mesma chave para evitar duplicação.

### Tratamento de Erros
Respostas de erro padronizadas em JSON:
```json
{
  "timestamp": "2026-05-22T14:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Pet com id 99 não encontrado(a).",
  "path": "/api/v1/pets/99"
}
```

---

## 🏗️ Arquitetura

```
src/main/java/com/petshop/api/
├── PetshopApiApplication.java
├── model/              ← Entidades JPA + Enums
├── dto/
│   ├── request/        ← Objetos de entrada (validados)
│   └── response/       ← Objetos de saída (HATEOAS)
├── assembler/          ← Montadores HATEOAS por entidade
├── repository/         ← Spring Data JPA
├── service/            ← Regras de negócio + mapeamento
├── controller/         ← REST controllers
├── exception/          ← GlobalExceptionHandler + ErrorResponse
├── apikey/             ← Autenticação via X-API-Key
├── ratelimit/          ← Rate Limiting com Bucket4j
└── infrastructure/     ← OpenAPI, CORS, H2, filtros, dados iniciais
```

### Relacionamentos entre entidades
```
Cliente 1──N Pet N──N Servico
                │
                └──1:N Agendamento N──1 Servico
                                   N──1 Unidade 1──1 Gerente
```

---

## 🛠️ Stack Tecnológica

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 17 | Linguagem |
| Spring Boot | 3.2.4 | Framework principal |
| Spring Web | — | REST API |
| Spring Data JPA | — | Persistência |
| Spring HATEOAS | — | Hipermídia |
| Spring Validation | — | Validação de entrada |
| Spring Security | — | Desabilitação CSRF / H2 Console |
| H2 Database | — | Banco em memória (dev) |
| Lombok | — | Redução de boilerplate |
| SpringDoc OpenAPI | 2.5.0 | Swagger UI |
| Bucket4j | 8.10.1 | Rate Limiting |

---

*Desenvolvido como projeto final de Web Services — Senac TSI.*
