# 🐾 Petshop API

**Status do Projeto:** 🟢 LIVE (Ambiente de Desenvolvimento)  
**Documentação Oficial:** [Acesse o Swagger aqui](https://petshop-api-4.onrender.com/swagger-ui/index.html#/Pets/criar_2)
**Desenvolvedora:** Laís

---

## 📝 Sobre o Projeto
API desenvolvida para a **gestão completa de um Petshop**, focando na organização de cadastros, controle de animais e agendamentos de serviços.

---

## 📊 Modelagem de Dados (Entidades)
* **Cliente:** Gestão de proprietários com validação de CPF.
* **Pet:** Registro dos animais vinculados aos seus donos.
* **Agendamento:** Controle de horários e status (PENDENTE, CONCLUÍDO).
* **Serviço:** Catálogo de banhos, tosas e consultas.

---

## 🛠️ Tecnologias e Padrões
* **Java 17 & Spring Boot 3.2.4**
* **JPA/Hibernate & Spring Data JPA**
* **Banco de Dados H2** (In-memory)
* **Swagger (OpenAPI)** para documentação.

---

## 🚀 Exemplo de Resposta (JSON)
```json
{
  "id": 1,
  "nome": "Luky",
  "especie": "CACHORRO",
  "status": "PENDENTE",
  "cliente": {
    "nome": "Laís",
    "cpf": "123.456.789-00"
  }
}
