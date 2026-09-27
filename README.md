# PromoGamer

![Java](https://img.shields.io/badge/Java_21-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_4-6DB33F?style=flat&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=flat&logo=springsecurity&logoColor=white)
![Spring AI](https://img.shields.io/badge/Spring_AI-OpenAI-412991?style=flat&logo=openai&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=flat&logo=postgresql&logoColor=white)
![H2](https://img.shields.io/badge/H2-blue?style=flat)
![OpenFeign](https://img.shields.io/badge/OpenFeign-6DB33F?style=flat&logo=springboot&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-CC0200?style=flat&logo=flyway&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI-85EA2D?style=flat&logo=swagger&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white)
![AWS EC2](https://img.shields.io/badge/AWS-EC2-FF9900?style=flat&logo=amazonaws&logoColor=white)
![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=flat&logo=junit5&logoColor=white)

API REST que caça promoções de jogos com boa nota na Steam, gera uma descrição com IA e envia a mensagem pronta automaticamente para um grupo de WhatsApp.

> O fluxo completo (buscar → registrar → enriquecer → gerar descrição com IA → montar mensagem → enviar via WhatsApp) já roda de ponta a ponta, de forma automática e agendada, e está implantado em produção em uma instância EC2 na AWS via Docker.

## Sumário

- [O problema](#o-problema)
- [Como funciona](#como-funciona)
- [Exemplo de mensagem gerada](#exemplo-de-mensagem-gerada)
- [Stack](#stack)
- [Arquitetura](#arquitetura)
- [Estrutura](#estrutura)
- [Rodando localmente](#rodando-localmente)
- [Profiles e variáveis de ambiente](#profiles-e-variáveis-de-ambiente)
- [Deploy](#deploy)
- [Segurança](#segurança)
- [Endpoints](#endpoints)
- [Ciclo de vida de uma deal](#ciclo-de-vida-de-uma-deal)
- [Tratamento de erros](#tratamento-de-erros)
- [Testes](#testes)
- [Roadmap](#roadmap)


## O problema

Ficar de olho em promoção de jogo bom é trabalho manual: abrir site, comparar preço, checar nota, escrever o texto pra divulgar e mandar no grupo. Se depender de garimpar isso à mão, a promoção boa já passou até você postar. O PromoGamer automatiza essa rotina inteira:

1. busca promoções com nota alta na Steam através da API pública da CheapShark;
2. filtra o que já foi visto antes, pra nunca repetir uma deal;
3. consulta a API oficial da Steam pra pegar preço atualizado, desconto, imagem e descrição do jogo;
4. usa IA (OpenAI, via Spring AI) para reescrever a descrição do jogo de forma curta e chamativa;
5. monta uma mensagem já formatada (com emojis, preço convertido pra real, imagem e link da loja);
6. envia a mensagem automaticamente para um grupo de WhatsApp, através de uma instância própria da Evolution API.

Tudo isso roda sozinho, sem precisar abrir navegador nenhum nem disparar nada manualmente.


## Como funciona

```
                    ┌─────────────────────────┐
                    │   CheapShark API         │
                    │   (deals com desconto e  │
                    │   nota Steam ≥ 80%)      │
                    └────────────┬─────────────┘
                                 │
                                 ▼
                  filtra deals já existentes no banco
                                 │
                                 ▼
                  salva as novas deals (status PENDENTE)
                                 │
                                 ▼
                    ┌─────────────────────────┐
                    │      Steam API           │
                    │  tenta como jogo (app);  │
                    │  se falhar, tenta como   │
                    │  pacote (sub/package)    │
                    └────────────┬─────────────┘
                                 │
                                 ▼
              gera a descrição do jogo com IA (OpenAI)
                                 │
                                 ▼
              monta a mensagem (preço, desconto, imagem, link)
                                 │
                                 ▼
              persiste a Message (status PENDENTE de envio)
              e a deal passa de PENDENTE → PROCESSADO
                                 │
                                 ▼
                    ┌─────────────────────────┐
                    │     Evolution API        │
                    │  (WhatsApp self-hosted)  │
                    └────────────┬─────────────┘
                                 │
                                 ▼
              mensagem enviada ao grupo e marcada como ENVIADA
```

Dois jobs agendados cuidam de todo o ciclo, sem nenhuma chamada manual:

- **`DealScheduledService`** — roda **às 8h e 18h (horário de São Paulo)** e busca novas promoções na CheapShark para a loja Steam.
- **`MessageScheduledService`** — roda em janelas de envio, a cada 10 minutos, **entre 12h–13h50 e 18h–20h50 (horário de Recife)**, pega a próxima deal pendente, monta a mensagem (com a descrição gerada por IA) e a envia via Evolution API.


## Exemplo de mensagem gerada

A partir de uma deal processada, o `MessageTemplateBuilder` monta uma legenda pronta pra envio, combinando a descrição gerada por IA com o preço já convertido para real:

```
*🎮 Elden Ring*

Uma jornada épica em um mundo aberto brutal, criado em parceria
com George R. R. Martin. Prepare-se para desafios implacáveis!

🏷️ De R$ 249,90 por R$ 99,96 (60% OFF)

🛒 Ver oferta na Steam:
https://store.steampowered.com/app/1245620
```

Se a promoção for de um pacote (bundle) em vez de um jogo avulso, a mensagem é montada num formato equivalente, sinalizando que se trata de um pacote especial.

Caso a chamada à IA falhe (rate limit, timeout ou qualquer outro erro), o fluxo não é interrompido: a descrição original retornada pela Steam é usada como fallback.


## Stack

- **Java 21** + **Spring Boot 4**
- **Spring Data JPA** + Hibernate
- **Flyway** — versionamento e controle do schema do banco
- **PostgreSQL** (produção) / **H2** (testes e dev local)
- **OpenFeign** — clients declarativos para CheapShark API, Steam API e Evolution API
- **Spring AI** (starter OpenAI) — geração das descrições dos jogos via `ChatClient` (modelo `gpt-4o-mini`)
- **Evolution API** — API self-hosted de envio de mensagens via WhatsApp, orquestrada junto no `docker-compose`
- **Spring Security** — autenticação HTTP Basic nos endpoints da API
- **MapStruct** — mapeamento DTO ↔ entidade
- **Bean Validation** (Jakarta)
- **SpringDoc OpenAPI + Swagger UI** — documentação interativa dos endpoints
- **Spring Scheduling** — jobs automáticos de busca de deals e de envio de mensagens (cron)
- **JUnit 5 + Mockito** — testes unitários de serviço
- **Docker + Docker Compose** — build e orquestração da aplicação junto com Evolution API, PostgreSQL e Redis
- **Lombok**
- **Maven**


## Arquitetura

O fluxo é dividido em responsabilidades bem separadas, cada uma isolada em seu próprio serviço:

| Camada | Responsável | O que faz |
|---|---|---|
| Origem das deals | `CheapSharkApiService` / `CheapSharkDealService` | consulta a CheapShark, filtra deals sem `steamAppId` |
| Registro | `DealService` / `DealScheduledService` | evita duplicatas, persiste novas deals, roda no cron |
| Enriquecimento | `SteamApiService` / `SteamService` | busca detalhes reais na Steam (preço, imagem, nome, descrição) |
| Geração de texto | `AiConfig` (`ChatClient`) | gera a descrição chamativa do jogo/pacote via OpenAI |
| Mensagem | `MessageTemplateBuilder` / `MessageService` | monta o texto final e persiste a mensagem |
| Envio | `EvolutionApiService` / `MessageScheduledService` | envia a mensagem pronta para o WhatsApp e marca como enviada |
| Segurança | `SecurityConfiguration` | protege os endpoints da API com HTTP Basic |

A integração com APIs externas (CheapShark, Steam e Evolution API) é feita via **Feign**, o que mantém os clients declarativos e testáveis, sem código de HTTP manual espalhado pelos serviços.

Como nem todo `steamAppId` retornado pela CheapShark corresponde a um jogo (às vezes é um pacote/bundle), o `SteamService` tenta resolver primeiro como aplicativo e, se a Steam não reconhecer, tenta novamente como pacote antes de desistir da deal.


## Estrutura

```
src/main/java/com/PedroNunesDev/PromoGamer/
├── client/           # Feign clients
│   ├── CheapSharkApiService
│   ├── SteamApiService
│   └── EvolutionApiService
├── config/
│   └── AiConfig               # configuração do ChatClient (Spring AI / OpenAI)
├── security/
│   └── SecurityConfiguration  # HTTP Basic + liberação do Swagger/H2 console
├── controller/       # endpoints REST
│   ├── DealController
│   └── CheapSharkController
├── schedule/         # jobs agendados
│   ├── DealScheduledService     # busca novas deals (8h/18h)
│   └── MessageScheduledService  # monta e envia mensagens (janelas de envio)
├── enums/            # DealEnumStatus, DealSourceType, MessageStatus
├── service/          # lógica de negócio
│   ├── CheapSharkDealService      # busca e filtra deals na CheapShark
│   ├── DealService                # registra novas deals no banco
│   ├── SteamService                # resolve app/pacote na Steam
│   ├── MessageTemplateBuilder     # monta o texto da mensagem
│   └── MessageService              # gera descrição via IA e persiste a mensagem
├── repository/       # Spring Data JPA
├── model/            # entidades JPA (Deal, Message)
├── dto/              # DTOs de request/response e integrações externas
├── mapper/           # MapStruct
└── exception/        # tratamento global de erros

src/main/resources/
├── application.properties
├── application-test.properties
├── application-prod.properties
└── db/migration/     # scripts Flyway (V1...)

src/test/java/com/PedroNunesDev/PromoGamer/
└── service/          # testes unitários (JUnit 5 + Mockito)
```


## Rodando localmente

```bash
git clone <url-do-repositorio>
cd PromoGamer
./mvnw spring-boot:run
```

Por padrão a aplicação sobe com o profile `test`, usando banco H2 em memória.

Antes de rodar, defina pelo menos as variáveis de ambiente obrigatórias (veja a próxima seção). Sem elas, valores fake de desenvolvimento são usados no profile `test`, mas a geração de descrição via IA e o envio via WhatsApp não vão funcionar de verdade.

API em `http://localhost:8080` · Swagger em `http://localhost:8080/swagger-ui.html`

Como a API agora exige autenticação (ver [Segurança](#segurança)), use as credenciais definidas em `USERNAME_AUTHENTICATION` / `USERNAME_PASSWORD` (ou os valores padrão do profile `test`) para acessar as rotas protegidas.


## Profiles e variáveis de ambiente

| Profile | Banco | Uso |
|--------|-------|-----|
| `test` (padrão) | H2 em memória | Desenvolvimento local |
| `prod` | PostgreSQL | Produção |

O profile ativo é definido pela variável `PROFILE_ACTIVE` (padrão `test`).

Variáveis usadas pela aplicação:

```properties
# número do grupo de WhatsApp para onde a mensagem é enviada
NUMBER_GROUP=<numero-do-grupo>

# opcional — usado no User-Agent das chamadas à CheapShark (tem valor padrão)
MY_EMAIL=seu_email@gmail.com

# credenciais de autenticação HTTP Basic da API
USERNAME_AUTHENTICATION=<usuario>
USERNAME_PASSWORD=<senha>

# chave de API da instância da Evolution API usada para o envio via WhatsApp
EVOLUTION_API_KEY=<chave-da-evolution-api>

# chave de API da OpenAI, usada pelo Spring AI para gerar as descrições
OPENAI_API_KEY=<chave-da-openai>
```

Para rodar com o profile `prod`, defina também as credenciais de conexão com o PostgreSQL:

```properties
spring.profiles.active=prod
DB_HOST=<host>
DB_PORT=<porta>
DB_NAME=<database>
DB_USERNAME=<usuario>
DB_PASSWORD=<senha>
```


## Deploy

O projeto já está implantado e em execução em uma instância **EC2 da AWS**.

A aplicação sobe containerizada, junto com sua dependência de envio de mensagens:

- **`Dockerfile`** — build multi-stage (Maven + Eclipse Temurin 21) que gera a imagem da API.
- **`docker-compose.yml`** — orquestra, na mesma rede:
    - `promogamer-api` — a própria API (porta `8080`);
    - `evolution-api` — instância própria da [Evolution API](https://github.com/EvolutionAPI/evolution-api) para envio das mensagens via WhatsApp (porta `8082`);
    - `postgres` — banco de dados usado pela Evolution API;
    - `redis` — cache/fila usado pela Evolution API.

As variáveis de ambiente de produção (banco de dados, chaves de API, credenciais, etc.) são fornecidas via arquivo `.env`, referenciado pelo `docker-compose.yml` e não versionado no repositório.


## Segurança

Os endpoints da API são protegidos com **HTTP Basic Authentication** (`SecurityConfiguration`). Ficam liberados sem autenticação apenas:

- `/v3/api-docs/**`
- `/swagger-ui.html` e `/swagger-ui/**`
- `/h2-console/**`

Todas as demais rotas exigem as credenciais definidas em `USERNAME_AUTHENTICATION` / `USERNAME_PASSWORD`.


## Endpoints

| Método | Rota | O que faz |
|--------|------|-----------|
| `GET` | `/api/deals?storeId=&pageNumber=` | busca promoções direto na CheapShark, sem persistir |
| `POST` | `/deals?storeId=` | registra no banco as novas promoções encontradas para uma loja |
| `GET` | `/deals/status?statusType=` | lista promoções filtradas por status (`PENDENTE`, `PROCESSADO`, `CONCLUIDO`, `IGNORADO`) |

Documentação completa e interativa no Swagger depois de subir a API.

> A geração da descrição via IA, a montagem da mensagem final e o envio pelo WhatsApp não passam mais por um endpoint manual — todo esse trecho do fluxo é disparado automaticamente pelo `MessageScheduledService`.


## Ciclo de vida de uma deal

```
PENDENTE ──► PROCESSADO ──► CONCLUIDO
                  │
                  └──► IGNORADO
```

- **PENDENTE** — deal recém-registrada, ainda sem mensagem montada
- **PROCESSADO** — mensagem já foi construída e enviada a partir dessa deal
- **CONCLUIDO** — status previsto no domínio para o fluxo totalmente finalizado, ainda não atribuído automaticamente pelo código atual
- **IGNORADO** — deal descartada (ex: `steamAppId` inválido tanto como app quanto como pacote, ou desconto igual a 0)

A `Message`, por sua vez, tem seu próprio status (`MessageStatus`): `PENDENTE` → `ENVIADA`, controlado pelo método `markAsSent()` da entidade, chamado pelo `MessageScheduledService` após a confirmação do envio pela Evolution API. O status `FALHA` existe no domínio, mas hoje falhas de envio são apenas logadas, sem atualizar o status da mensagem.


## Tratamento de erros

Toda falha nas integrações externas é capturada centralmente pelo `GlobalExceptionHandler` e traduzida em uma resposta padronizada (`ErrorResponse` com timestamp, status, mensagem e path):

| Cenário | Status retornado |
|---|---|
| Recurso não encontrado na CheapShark | `404 Not Found` |
| Nenhuma deal/promoção pendente encontrada | `404 Not Found` |
| Rate limit da CheapShark atingido | `429 Too Many Requests` |
| Timeout na chamada externa | `504 Gateway Timeout` |
| Outro erro HTTP não mapeado do Feign | `502 Bad Gateway` |
| Argumento inválido (ex: status inexistente) | `400 Bad Request` |
| Erro inesperado | `500 Internal Server Error` |

Nos jobs agendados (`DealScheduledService` e `MessageScheduledService`), essas mesmas exceções são capturadas e apenas logadas, para que uma falha pontual não interrompa o agendamento.


## Testes

Os serviços centrais têm cobertura de testes unitários com **JUnit 5 + Mockito**, incluindo casos como:

- busca de deals retornando resultados, lista vazia, e validação de parâmetros nulos (`CheapSharkDealService`);
- registro de novas deals evitando duplicatas já existentes no banco, paginação automática quando a página atual só tem deals repetidas, e interrupção da busca após um limite de páginas sem novas deals (`DealService`);
- resolução de detalhes na Steam como jogo e como pacote, incluindo os casos de desconto zero, app/pacote inválido e parâmetros nulos (`SteamService`).

```bash
./mvnw test
```

> `MessageService` e os serviços de `schedule` (geração de descrição via IA e envio via WhatsApp) ainda não possuem testes unitários dedicados.



## Licença

Projeto privado — uso pessoal.

---

<div align="center">

### ⭐ Se este projeto foi útil para você, considere dar uma estrela!

**Desenvolvido com ☕ e ❤️ por Pedro Nunes**

</div>