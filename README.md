# ApiLari - Backend

API RESTful desenvolvida com **Java** e **Spring Boot** para gerenciamento de histórias e publicações (Stories), com persistência no **MongoDB** e autenticação/autorização via **Firebase Admin SDK**.

---

## 🛠️ Tecnologias Utilizadas

- **Java 17+**
- **Spring Boot** (Spring Web, Spring Data MongoDB, Validation)
- **MongoDB** (MongoDB Atlas)
- **Firebase Admin SDK** (Autenticação e verificação de JWT)
- **Maven** (Gerenciamento de dependências e build)
- **Lombok**
- **JUnit 5 / Mockito** (Testes unitários e de integração)

---

## 🔒 Recursos de Segurança

- **Autenticação Firebase:** Validação de tokens JWT Bearer nos endpoints protegidos.
- **Controle de Acesso (RBAC/Whitelist):** Apenas e-mails/UIDs autorizados têm permissão para criar, editar ou excluir registros.
- **Proteção contra Information Disclosure:** Tratamento global de exceções sem vazamento de stack traces ou detalhes sensíveis internos.
- **Security Headers Filter:** Headers de segurança HTTP (`X-Content-Type-Options`, `X-Frame-Options`, `Content-Security-Policy`, etc.).
- **CORS Parametrizável:** Restrição de domínios permitidos via variável de ambiente.

---

## 🚀 Como Executar Localmente

### 1. Pré-requisitos
- JDK 17 ou superior instalado
- Maven instalado (ou use o `./mvnw` incluso)
- Instância do MongoDB rodando localmente ou cluster no MongoDB Atlas

### 2. Configuração de Variáveis de Ambiente
Copie o arquivo de exemplo para criar seu `.env`:

```bash
cp .env.example .env
```

Preencha as variáveis de ambiente necessárias:
- `MONGODB_URI`: String de conexão do MongoDB.
- `ADMIN_EMAILS`: E-mails autorizados para operações de escrita.
- `CORS_ALLOWED_ORIGINS`: Domínios autorizados a consumir a API.

### 3. Compilar e Executar

```bash
# Executar testes
./mvnw test

# Iniciar a aplicação
./mvnw spring-boot:run
```

A API estará disponível por padrão em `http://localhost:8080`.

---

## 📡 Endpoints da API

### **Histórias / Stories (`/api/stories`)**

| Método | Endpoint | Descrição | Acesso |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/stories` | Lista todas as histórias | Público |
| `GET` | `/api/stories/{id}` | Busca história por ID | Público |
| `POST` | `/api/stories` | Cria uma nova história | Protegido (Admin) |
| `PUT` | `/api/stories/{id}` | Atualiza uma história existente | Protegido (Admin) |
| `DELETE` | `/api/stories/{id}` | Remove uma história | Protegido (Admin) |

> **Nota:** Para endpoints protegidos, envie o header `Authorization: Bearer <FIREBASE_ID_TOKEN>`.
