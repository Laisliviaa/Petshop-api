# 🐾 Petshop API

API REST desenvolvida para a disciplina de **Desenvolvimento de Web Services** — Senac TSI.
Gerenciamento completo de um petshop: clientes, pets, serviços, unidades, gerentes e agendamentos.

> 🌐 **Swagger UI:** `http://localhost:8080/swagger-ui.html`
> 🗄️ **H2 Console:** `http://localhost:8080/h2-console`
> 📄 **API Docs:** `http://localhost:8080/v3/api-docs`

---

## Tecnologias

| Tecnologia | Uso |
|---|---|
| Java 17 | Linguagem principal |
| Spring Boot 3.2.4 | Framework web |
| Spring Data JPA | Persistência de dados |
| Spring HATEOAS | Links de navegação nas respostas |
| Spring Validation | Validação de campos |
| Bucket4j 8.10.1 | Rate limiting profissional |
| Springdoc OpenAPI 2.5.0 | Documentação automática (Swagger UI) |
| H2 Database | Banco em memória |
| Lombok | Redução de boilerplate |
| Docker | Containerização |

---

## Arquitetura

```
src/main/java/com/example/petshopapi/
├── apikey/          → Entidade, controller, filtro e repositório de API Keys
├── assemblers/      → ModelAssemblers para HATEOAS (um por recurso)
├── config/          → CORS e OpenAPI/Swagger
├── controller/      → Endpoints REST de cada recurso
├── dto/
│   ├── request/     → Objetos de entrada (o que o cliente envia)
│   └── response/    → Objetos de saída (o que a API retorna)
├── exception/       → Exceções customizadas e handler global de erros
├── filter/          → Filtro de idempotência (X-Idempotency-Key)
├── infrastructure/  → DataInitializer — dados carregados na inicialização
├── model/           → Entidades JPA
├── ratelimit/       → Rate limiting por IP com Bucket4j
├── repository/      → Interfaces JpaRepository
├── service/         → Camada de negócio (uma por recurso)
└── versioning/      → Interceptor de versionamento por header X-API-Version
```

---

## Relacionamentos implementados

| Relacionamento | Entidades |
|---|---|
| One-to-Many | Cliente → Pets |
| Many-to-One | Pet → Cliente |
| Many-to-One | Agendamento → Pet, Serviço, Unidade |
| Many-to-Many | Pet ↔ Serviço (tabela `pet_servicos`) |
| One-to-One | Gerente → Unidade (validado no service — cada unidade admite no máximo 1 gerente) |

---

## Endpoints

| Recurso | Base | Consultas extras |
|---|---|---|
| API Keys | `/api/v1/apikeys` | — |
| Clientes | `/api/v1/clientes` | `/busca?nome=`, `/cpf/{cpf}` |
| Pets | `/api/v1/pets` | `/busca?nome=` |
| Serviços | `/api/v1/servicos` | `/busca?descricao=` |
| Unidades | `/api/v1/unidades` | — |
| Gerentes | `/api/v1/gerentes` | — |
| Agendamentos | `/api/v1/agendamentos` | `/status/{status}`, `/pet/{id}`, `/unidade/{id}` |

Todos os endpoints possuem CRUD completo (GET, POST, PUT, DELETE).

---

## Autenticação — API Keys

**GET é sempre público** — não exige chave.

**Chaves pré-carregadas:**

| Chave | Role | Permissões |
|---|---|---|
| `petshop-admin-key-2026` | ADMIN | GET + POST + PUT + DELETE + revogar chaves |
| `petshop-funcionario-key-2026` | FUNCIONARIO | GET + POST + PUT |
| `petshop-visitante-key-2026` | VISITANTE | somente GET |

Use no header: `X-API-Key: petshop-admin-key-2026`

O header de resposta `X-API-Key-Role` informa a role da chave utilizada em cada requisição autenticada.

---

## Versionamento — Header Versioning

Esta API utiliza **versionamento por header** (`X-API-Version`).

> ✅ **Por que header e não URL?**
> O versionamento por URL (`/v1/`, `/v2/`) mistura a versão com o endereço do recurso,
> quebrando o princípio de que uma URL deve identificar um recurso de forma estável.
> O header versioning mantém a URL limpa (`/api/v1/clientes` para sempre) e deixa o
> cliente negociar a versão de representação que deseja receber — exatamente como
> funciona o content negotiation HTTP.

| Header | Comportamento |
|---|---|
| `X-API-Version: 1` (ou ausente) | Resposta padrão com HATEOAS |
| `X-API-Version: 2` | Resposta enriquecida com metadados de paginação (disponível em `GET /api/v1/clientes`) |
| Qualquer outro valor | HTTP 400 Bad Request |

O interceptor valida o header em todas as rotas `/api/**`. A resposta diferenciada de v2 (com `totalElements`, `totalPages`, `page`, `size`, `content`, `apiVersion`) está disponível no endpoint `GET /api/v1/clientes`.

```http
GET /api/v1/clientes HTTP/1.1
X-API-Version: 2
```

---

## Rate Limiting

| Tipo | Limite |
|---|---|
| GET (leitura) | 10 req / 30s por IP |
| POST / PUT / DELETE (escrita) | 5 req / 30s por IP |

Ao exceder: **HTTP 429** com header `Retry-After` informando segundos até o reset do bucket.

**Modelo de recarga:** o bucket recarrega em **lote** ao final da janela de 30 segundos (`refillIntervally`). Isso significa que, após esgotar os tokens, o cliente deve aguardar o `Retry-After` completo para que todos os tokens sejam restaurados de uma só vez.

Toda resposta bem-sucedida inclui o header **`X-RateLimit-Remaining`** com o número de tokens restantes no bucket atual — use-o para monitorar o consumo antes de atingir o limite.

---

## Idempotência

Endpoints de escrita **POST, PUT e PATCH** suportam o header `X-Idempotency-Key` (UUID recomendado):

| Cenário | Comportamento |
|---|---|
| Chave nova | Processa normalmente e armazena resultado |
| Mesma chave + mesmo payload | Retorna resposta original (sem reprocessar) |
| Mesma chave + payload diferente | **HTTP 409 Conflict** |
| Sem chave | Processa normalmente (idempotência opcional) |

> **DELETE** não utiliza chave de idempotência pois já é semanticamente idempotente pelo protocolo HTTP (excluir um recurso já excluído deve retornar o mesmo resultado).

```http
POST /api/v1/clientes HTTP/1.1
X-API-Key: petshop-funcionario-key-2026
X-Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000
Content-Type: application/json
```

---

## CORS

A API aceita requisições de qualquer origem (`*`). Headers liberados para envio:

- `Content-Type`, `X-API-Key`, `X-Idempotency-Key`, `X-API-Version`, `Accept`

Headers expostos para leitura pelo cliente (ex: JavaScript):

- `X-RateLimit-Remaining`, `Retry-After`, `X-API-Key-Role`, `X-API-Version`, `Location`

---

## Formato de Erros

Todos os erros (4xx e 5xx) retornam o mesmo JSON padronizado via `ApiErrorResponse`:

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

O campo `detalhes` é preenchido apenas em erros de validação (HTTP 400), listando cada campo inválido:

```json
{
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Validação falhou. Verifique os campos obrigatórios.",
  "detalhes": [
    "nome: O nome é obrigatório",
    "cpf: CPF deve ter 11 dígitos numéricos (sem pontos ou traço)"
  ]
}
```

---

## HATEOAS

Todas as respostas de recurso individual incluem links HAL para navegação:

```json
{
  "id": 1,
  "nome": "Carlos Lima",
  "_links": {
    "self":       { "href": "/api/v1/clientes/1" },
    "update":     { "href": "/api/v1/clientes/1" },
    "delete":     { "href": "/api/v1/clientes/1" },
    "collection": { "href": "/api/v1/clientes" }
  }
}
```

Os links `self`, `update`, `delete` e `collection` estão presentes em todos os recursos: Cliente, Pet, Serviço, Unidade, Gerente e Agendamento.

---

## Regras de Negócio

| Regra | Recurso | Erro |
|---|---|---|
| CPF deve ser único por cliente | Cliente | 409 Conflict |
| Cada unidade admite no máximo 1 gerente | Gerente | 409 Conflict |
| Agendamento não pode ser criado com data no passado | Agendamento | 422 Unprocessable Entity |
| Agendamento cancelado não pode ser reativado | Agendamento | 422 Unprocessable Entity |

---

## Status HTTP utilizados

| Código | Quando | Formato de Resposta |
|---|---|---|
| 200 OK | GET e PUT com sucesso | JSON do recurso |
| 201 Created | POST com sucesso (inclui header `Location`) | JSON do recurso criado |
| 204 No Content | DELETE com sucesso | Sem corpo |
| 400 Bad Request | Dados inválidos, parâmetro ausente, versão de API inválida | `ApiErrorResponse` |
| 401 Unauthorized | X-API-Key ausente ou inválida | JSON do ApiKeyFilter |
| 403 Forbidden | Role insuficiente | JSON do ApiKeyFilter |
| 404 Not Found | Recurso não encontrado | `ApiErrorResponse` |
| 409 Conflict | CPF duplicado, unidade já com gerente, chave idempotente com payload diferente, violação de integridade referencial | `ApiErrorResponse` |
| 415 Unsupported Media Type | Content-Type não suportado (use `application/json`) | `ApiErrorResponse` |
| 422 Unprocessable Entity | Violação de regra de negócio (ex: agendamento com data no passado) | `ApiErrorResponse` |
| 429 Too Many Requests | Rate limit excedido | JSON do RateLimitFilter (com `Retry-After`) |
| 500 Internal Server Error | Erro interno inesperado | `ApiErrorResponse` |

> **Nota sobre 401 e 403:** esses erros são gerados pelo `ApiKeyFilter` antes de chegar ao handler, portanto o corpo JSON segue o mesmo padrão estrutural (`timestamp`, `status`, `erro`, `mensagem`, `caminho`, `metodo`), mas não passa pelo `RestExceptionHandler`.

---

## Como rodar

### Localmente

```bash
./mvnw spring-boot:run
```

Acesse: `http://localhost:8080/swagger-ui.html`

### Docker

```bash
docker build -t petshop-api .
docker run -p 8080:8080 petshop-api
```

### Testes

```bash
./mvnw test
```
