# 🐾 Petshop API

API REST desenvolvida para a disciplina de **Desenvolvimento de Web Services** — Senac TSI.
Gerenciamento completo de um petshop: clientes, pets, serviços, unidades, gerentes e agendamentos.

> 🌐 **Swagger UI:** https://petshop-api-23cd.onrender.com/swagger-ui/index.html
> 🗄️ **H2 Console:** http://localhost:8080/h2-console (apenas local)
> 📄 **API Docs:** https://petshop-api-23cd.onrender.com/v3/api-docs

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
| One-to-One | Gerente → Unidade (cada unidade admite no máximo 1 gerente) |

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

| Chave | Role | Permissões |
|---|---|---|
| `petshop-admin-key-2026` | ADMIN | GET + POST + PUT + DELETE |
| `petshop-funcionario-key-2026` | FUNCIONARIO | GET + POST + PUT |
| `petshop-visitante-key-2026` | VISITANTE | somente GET |

Use no header: `X-API-Key: petshop-admin-key-2026`

---

## Versionamento

Esta API utiliza **versionamento por header** (`X-API-Version`).

| Header | Comportamento |
|---|---|
| `X-API-Version: 1` (ou ausente) | Resposta padrão com HATEOAS |
| `X-API-Version: 2` | Resposta enriquecida com metadados de paginação |
| Qualquer outro valor | HTTP 400 Bad Request |

---

## Rate Limiting

| Tipo | Limite |
|---|---|
| GET (leitura) | 10 req / 30s por IP |
| POST / PUT / DELETE (escrita) | 5 req / 30s por IP |

Ao exceder: **HTTP 429** com header `Retry-After` informando segundos até o reset.

---

## Idempotência

Endpoints de escrita suportam o header `X-Idempotency-Key` (UUID recomendado):

| Cenário | Comportamento |
|---|---|
| Chave nova | Processa normalmente e armazena resultado |
| Mesma chave + mesmo payload | Retorna resposta original (sem reprocessar) |
| Mesma chave + payload diferente | HTTP 409 Conflict |
| Sem chave | Processa normalmente (idempotência opcional) |

---

## Tratamento de Erros

Todos os erros retornam o mesmo formato JSON:

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

O campo `detalhes` é preenchido apenas em erros de validação (HTTP 400):

```json
{
  "status": 400,
  "mensagem": "Validação falhou. Verifique os campos obrigatórios.",
  "detalhes": [
    "nome: O nome é obrigatório",
    "cpf: CPF deve ter 11 dígitos numéricos"
  ]
}
```

Casos tratados: `400` validação, `401` chave ausente/inválida, `403` role insuficiente, `404` recurso não encontrado, `405` método não permitido, `409` conflito (CPF duplicado, integridade referencial, idempotência), `415` Content-Type inválido, `422` regra de negócio, `429` rate limit e `500` erro interno.

---

## Regras de Negócio

| Regra | Recurso | Erro |
|---|---|---|
| CPF deve ser único por cliente | Cliente | 409 Conflict |
| Cada unidade admite no máximo 1 gerente | Gerente | 409 Conflict |
| Agendamento não pode ser criado com data no passado | Agendamento | 422 Unprocessable Entity |
| Agendamento cancelado não pode ser reativado | Agendamento | 422 Unprocessable Entity |

---

## Como rodar

**Localmente:**
```bash
./mvnw spring-boot:run
```
Acesse: `http://localhost:8080/swagger-ui.html`

**Docker:**
```bash
docker build -t petshop-api .
docker run -p 8080:8080 petshop-api
```

**Testes:**
```bash
./mvnw test
```
