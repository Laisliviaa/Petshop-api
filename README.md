# 🐾 PetShop API

**Status do Projeto:** Local (rodar com IntelliJ ou Maven)  
**Documentação Oficial:** [Swagger UI](http://localhost:8080/swagger-ui.html)  
**Frontend:** `src/main/resources/static/index.html`

API desenvolvida para gestão de um PetShop, com foco nos requisitos do projeto final: REST, relacionamentos JPA, HATEOAS, validação, documentação OpenAPI, autenticação por API Key, idempotência, rate limiting, CORS e versionamento.

**Autora:** Lais

---

## Como Executar

### Pré-requisitos
- Java 17 ou superior
- IntelliJ IDEA (recomendado) ou Maven instalado

### Rodando com IntelliJ
1. Abra a pasta `Petshop-api-main` no IntelliJ
2. Clique no botão ▶ (Run)
3. Aguarde a mensagem `Started PetshopApiApplication`
4. Acesse: `http://localhost:8080/index.html`

### Rodando com Maven
```bash
./mvnw spring-boot:run
```

---

## Links

| Recurso | URL |
|---|---|
| Frontend | http://localhost:8080/index.html |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| H2 Console | http://localhost:8080/h2-console |
| API v1 | http://localhost:8080/api/v1/clientes |
| API v2 | http://localhost:8080/api/v2/clientes |

**H2 Console:** JDBC URL: `jdbc:h2:mem:petshopdb` | User: `sa` | Senha: *(vazio)*

---

## Checklist Parte I

| Requisito | Como foi atendido |
|---|---|
| Projeto Maven | Spring Boot com `pom.xml` e Maven Wrapper |
| API REST | Controllers REST para clientes, pets, agendamentos, servicos, unidades e gerentes |
| CRUD | Todos os recursos possuem criacao, consulta, atualizacao e exclusao |
| HATEOAS | Respostas usam `EntityModel`, `CollectionModel` e `PagedModel` com links |
| Paginacao | Listagens usam `Pageable` e `PagedResourcesAssembler` |
| One-to-One | `Gerente` possui uma `Unidade` |
| One-to-Many | `Cliente` possui varios `Pet`; `Pet` possui varios `Agendamento` |
| Many-to-One | `Pet` pertence a um `Cliente`; `Agendamento` pertence a um `Pet` |
| Many-to-Many | `Pet` possui varios `Servico` e vice-versa |
| Enum | `StatusAgendamento` com valores PENDENTE, CONCLUIDO e CANCELADO |
| Validacao | Entidades usam `@NotBlank`, `@NotNull`, `@Pattern`, `@Positive`, `@Future` |
| Consultas personalizadas | Busca por CPF em Clientes, por status em Agendamentos, por nome em Unidades e Gerentes |
| Tratamento de erros | `RestExceptionHandler` com `@ControllerAdvice` padroniza respostas de erro |
| Swagger/OpenAPI | Documentacao disponivel em `/swagger-ui.html` com `@Operation`, `@ApiResponse` e `@Tag` |

---

## Checklist Parte II

| Requisito | Como foi atendido |
|---|---|
| HTTP 401 Unauthorized | Endpoints protegidos exigem `X-API-Key`; chave ausente retorna 401 |
| HTTP 403 Forbidden | Chave invalida ou inativa retorna 403 |
| API Key | Endpoint publico `POST /api/v1/apikeys` gera chaves; suporta roles USER e ADMIN |
| HTTP 429 Too Many Requests | Rate limit de 60 req/min por IP com bloqueio e header `Retry-After` |
| Idempotencia | `POST /api/v1/clientes` aceita `X-Idempotency-Key` e evita duplicidade |
| CORS | Permite chamadas web com headers customizados e metodos REST configurados |
| Versionamento | `/api/v1/clientes` e `/api/v2/clientes` com contratos diferentes; suporta header `X-API-Version` |

---

## Autenticacao com Chave de API

Operacoes nos endpoints protegidos exigem o header `X-API-Key`.

**Chaves criadas automaticamente na inicializacao:**

| Chave | Role |
|---|---|
| `petshop-admin-key-2026` | ADMIN |
| `petshop-user-key-2026` | USER |

**Fluxo para gerar uma nova chave:**
1. Chame `POST /api/v1/apikeys?clientName=SeuNome&role=USER` (endpoint publico)
2. Copie o campo `keyValue` da resposta
3. Use no header: `X-API-Key: sua-chave`

Se a chave estiver ausente, a API retorna `HTTP 401 Unauthorized`.  
Se a chave for invalida, a API retorna `HTTP 403 Forbidden`.

---

## Idempotencia

Operacoes `POST` aceitam o header `X-Idempotency-Key`. A chave identifica uma tentativa de escrita e evita duplicidade quando a mesma requisicao e enviada mais de uma vez.

**Comportamento:**
- Chave nova: a API processa normalmente
- Mesma chave enviada novamente: retorna o resultado ja processado sem criar duplicata

**Exemplo:**
```
X-Idempotency-Key: minha-chave-unica-001
```

---

## Rate Limiting

A API limita cada IP a **60 requisicoes por minuto**. Se o limite for excedido, retorna `HTTP 429 Too Many Requests`.

**Headers retornados:**

| Header | Descricao |
|---|---|
| `X-RateLimit-Limit` | Limite total da janela |
| `X-RateLimit-Remaining` | Requisicoes restantes |
| `X-RateLimit-Reset` | Segundos para reset da janela |
| `Retry-After` | Segundos para tentar novamente |

---

## CORS

A API permite requisicoes cross-origin com a seguinte configuracao:

- **Origens permitidas:** qualquer origem
- **Metodos permitidos:** GET, POST, PUT, DELETE, PATCH, OPTIONS
- **Headers permitidos:** Content-Type, Authorization, X-API-Key, X-Idempotency-Key, X-API-Version
- **Headers expostos:** X-RateLimit-Limit, X-RateLimit-Remaining, X-RateLimit-Reset, Retry-After

---

## Versionamento

A API possui duas versoes do endpoint de clientes:

| Versao | URL | Diferenca |
|---|---|---|
| v1 | `/api/v1/clientes` | Resposta padrao com HATEOAS |
| v2 | `/api/v2/clientes` | Inclui campos `apiVersion` e `totalPets` |

Tambem pode ser acessado via header: `X-API-Version: 2`

---

## Modelagem de Dados

| Entidade | Descricao |
|---|---|
| `Cliente` | Dono dos pets; possui CPF unico (11 digitos) |
| `Pet` | Animal do cliente; pertence a um Cliente |
| `Agendamento` | Marcacao de servico para um Pet; possui status (enum) |
| `Servico` | Tipo de servico oferecido (banho, tosa etc) |
| `Unidade` | Filial do PetShop |
| `Gerente` | Responsavel por uma Unidade (One-to-One) |

**Relacionamentos:**
- `Cliente` → `Pet`: One-to-Many
- `Pet` → `Agendamento`: One-to-Many
- `Pet` ↔ `Servico`: Many-to-Many
- `Gerente` → `Unidade`: One-to-One

---

## Principais Endpoints

| Recurso | Endpoint | Observacao |
|---|---|---|
| Clientes | `GET /api/v1/clientes` | Lista paginada com HATEOAS |
| Clientes | `GET /api/v1/clientes/cpf/{cpf}` | Consulta personalizada por CPF |
| Pets | `GET /api/v1/pets` | Lista paginada com HATEOAS |
| Agendamentos | `GET /api/v1/agendamentos` | Lista paginada com HATEOAS |
| Agendamentos | `GET /api/v1/agendamentos/status/{status}` | Consulta por status |
| Servicos | `GET /api/v1/servicos` | Lista paginada com HATEOAS |
| Unidades | `GET /api/v1/unidades` | Lista paginada com HATEOAS |
| Gerentes | `GET /api/v1/gerentes` | Lista paginada com HATEOAS |
| API Keys | `POST /api/v1/apikeys` | Gera chave (endpoint publico) |
| Versionamento | `GET /api/v2/clientes` | Versao 2 com campos extras |

---

## Tecnologias

- Java 17
- Spring Boot 3.2.4
- Maven
- Spring Web
- Spring Data JPA / Hibernate
- H2 Database (em memoria)
- Bean Validation (Jakarta)
- Spring HATEOAS
- Springdoc OpenAPI 2.5 / Swagger UI
- Lombok

---

## Roteiro Para Demonstracao

1. Rodar a API no IntelliJ e abrir `http://localhost:8080/index.html`
2. Clicar em **Conectar** e mostrar o dashboard
3. Abrir `http://localhost:8080/swagger-ui.html` e mostrar a documentacao
4. Executar `GET /api/v1/clientes?page=0&size=5` e mostrar paginacao e links HATEOAS
5. Executar uma consulta personalizada: `GET /api/v1/clientes/cpf/12345678900`
6. Tentar criar um cliente sem `X-API-Key` e demonstrar o erro 401
7. Repetir com `X-API-Key` e demonstrar sucesso
8. Enviar duas requisicoes com a mesma `X-Idempotency-Key` e mostrar que nao duplica
9. Enviar mais de 60 requisicoes seguidas para demonstrar 429 e o header `Retry-After`
10. Mostrar o preflight OPTIONS para demonstrar CORS
11. Comparar `GET /api/v1/clientes` com `GET /api/v2/clientes` para demonstrar versionamento
