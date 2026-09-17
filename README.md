# Larissa Hub API

API REST criada para sustentar o conteúdo editorial do [Larissa Hub](https://github.com/NaoNerfaMeuQ/ProjetoLarissa). O projeto combina Java 21, Spring Boot, MongoDB e Firebase Admin para oferecer leitura pública e operações administrativas autenticadas.

## Diferenciais técnicos

- API em camadas com DTOs, validação de entrada e tratamento global de exceções.
- Persistência MongoDB com ordenação, filtro por categoria e paginação limitada.
- Verificação de JWT Firebase com checagem de revogação.
- Autorização administrativa por UID, e-mail ou custom claim `admin`/`role`.
- Política `fail closed`: uma configuração ausente nunca libera escrita.
- Sanitização server-side de HTML com Jsoup antes da persistência.
- CORS por allowlist exata e headers HTTP defensivos.
- Testes unitários para validação, autorização e sanitização.
- Build reproduzível com Maven Wrapper e imagem Docker para deploy.

## Arquitetura

```text
Controller -> validação/autorização -> Service -> Repository -> MongoDB
                        |
                        `-> Firebase Admin SDK
```

## Endpoints

| Método | Endpoint | Descrição | Acesso |
| --- | --- | --- | --- |
| `GET` | `/api/stories` | Lista histórias; aceita `category`, `page` e `size` | Público |
| `GET` | `/api/stories/{id}` | Retorna uma história | Público |
| `POST` | `/api/stories` | Cria uma história | Administrador |
| `PUT` | `/api/stories/{id}` | Atualiza uma história | Administrador |
| `DELETE` | `/api/stories/{id}` | Exclui uma história | Administrador |
| `GET` | `/api/health` | Verifica disponibilidade | Público |

Nos endpoints protegidos, envie `Authorization: Bearer <FIREBASE_ID_TOKEN>`.

## Execução local

### Pré-requisitos

- JDK 21
- Docker ou uma instância MongoDB local
- Projeto Firebase para testar autenticação administrativa

Configure as variáveis descritas em `.env.example` no seu ambiente. O Spring Boot não carrega arquivos `.env` automaticamente.

```bash
./mvnw test
./mvnw spring-boot:run
```

A API ficará disponível em `http://localhost:8080`.

## Variáveis de ambiente

| Variável | Finalidade |
| --- | --- |
| `MONGODB_URI` | Conexão MongoDB, preferencialmente com usuário de privilégio mínimo |
| `MONGODB_DATABASE` | Nome do banco; padrão `portfoliodb` |
| `FIREBASE_CONFIG_JSON` | Service account JSON ou Base64, nunca versionada |
| `FIREBASE_CONFIG_PATH` | Alternativa local para a credencial Firebase |
| `ADMIN_EMAILS` | Allowlist de e-mails administrativos |
| `ADMIN_UIDS` | Allowlist de UIDs administrativos |
| `CORS_ALLOWED_ORIGINS` | Origens exatas, separadas por vírgula |

Mesmo sem allowlists, somente uma custom claim administrativa explícita concede acesso. Usuários apenas autenticados não recebem permissão de escrita.

## Testes e build

```bash
./mvnw clean test
./mvnw package
docker build -t larissa-hub-api .
```

## Segurança

- Não versione `.env`, service accounts, URIs com senha ou chaves privadas.
- Rotacione imediatamente qualquer credencial que tenha aparecido no histórico Git.
- Configure no frontend as regras versionadas em `storage.rules`.
- Em produção, defina somente a URL real do frontend em `CORS_ALLOWED_ORIGINS`.
