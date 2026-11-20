# Risk Mapping API

API REST (Java 21 + Spring Boot 3) do mapa de riscos.

Os dados ficam em um banco **MySQL 8.0.16+**:

Script em [`db/schema.sql`](db/schema.sql): só as tabelas e as categorias iniciais. Pode rodar mais de uma vez e nunca apaga dados.

### Tudo junto com Docker

```bash
cp .env.example .env     # preencha DB_ROOT_PASSWORD, JWT_SECRET e ADMIN_SENHA
docker compose up --build
```

Sobe a API em http://localhost:8080 e um MySQL 8.0 que roda o `db/schema.sql` na primeira vez.
Os dados ficam no volume `dados-mysql` (sobrevivem a `docker compose down`; para zerar, `docker compose down -v`).

## Como rodar

Precisa de Java 21, Maven e o MySQL com o banco criado:

```bash
mysql -u root -p < db/schema.sql

# conexão: em config/application.properties (fora do Git) ou nas variáveis de ambiente
#   DB_USUARIO, DB_SENHA e DB_NOME (padrão: root, vazia e risk_mapping, no MySQL local)
mvn spring-boot:run
```

A API não cria nem altera tabelas: o schema é o do script. Ao subir, ela só garante a
conta ADMIN (cria, ou sincroniza a senha com `app.admin.senha`).

- API: http://localhost:8080/api/v1
- Swagger: http://localhost:8080/swagger-ui.html

## Produção (Render + TiDB Cloud)

O banco de produção é o **TiDB Cloud Starter** (gratuito, compatível com MySQL), em `sa-east-1`.

1. No painel do TiDB (**Connect**), pegue host, porta (4000), usuário e senha.
2. Crie as tabelas, com o cliente MySQL **8.0** (o 9.x não tem o plugin `mysql_native_password`):
   ```bash
   MYSQL_PWD='...' /opt/homebrew/opt/mysql@8.0/bin/mysql -h HOST -P 4000 -u USUARIO \
     --ssl-mode=VERIFY_IDENTITY --ssl-ca=/etc/ssl/cert.pem < db/schema.sql
   ```
3. No painel do Render (Environment), defina:
   - `DB_USUARIO` (o usuário do TiDB tem um prefixo, ex: `abc123.root`), `DB_SENHA` e `DB_NOME`
   - e também `ADMIN_SENHA` (o `JWT_SECRET` o Render gera sozinho).

   O servidor (`DB_HOST`, `DB_PORTA`) e o TLS (`DB_SSL`) já vêm fixos no `render.yaml`. Se trocar de
   banco, é lá que se muda.

Observações do TiDB: as regras `CHECK` do script não são aplicadas (a API já valida
gravidade 1 a 5 e os status), e a primeira conexão após um tempo parado pode demorar.
Faça um `mysqldump` de vez em quando como backup.

## Camadas

```
controller/  recebe a requisição, valida (@Valid) e chama o service
service/     regras de negócio
repository/  acesso ao MySQL (JdbcClient, SQL puro)
model/       Categoria, Bairro, Risco, Comentario, Usuario + enums
dto/         o que a API recebe (requests)
exception/   erros -> respostas HTTP (404, 409, 400)
```

## Endpoints

| Método | Caminho | O que faz | Status |
|---|---|---|---|
| GET | /categorias, /categorias/{id} | lista / busca categorias (tabela `categoria_risco`) | 200 / 404 |
| POST | /categorias | cria categoria (ADMIN) | 201 / 400 / 409 |
| PUT | /categorias/{id} | atualiza categoria (ADMIN) | 200 / 400 / 404 / 409 |
| DELETE | /categorias/{id} | remove (409 se tiver riscos) (ADMIN) | 204 / 404 / 409 |
| GET | /bairros, /bairros/{id} | lista / busca bairros | 200 / 404 |
| POST | /bairros | cria bairro (ADMIN) | 201 / 400 / 409 |
| PUT | /bairros/{id} | atualiza bairro (ADMIN) | 200 / 400 / 404 / 409 |
| DELETE | /bairros/{id} | remove (409 se tiver riscos) (ADMIN) | 204 / 404 / 409 |
| GET | /riscos | lista (tabela `relato`); filtros `categoriaId`, `bairroId`, `gravidade`, `status`, `busca` | 200 |
| GET | /riscos/{id} | busca um risco | 200 / 404 |
| POST | /riscos | cria risco (começa ABERTO) | 201 / 400 / 404 |
| PUT | /riscos/{id} | atualiza risco | 200 / 400 / 404 |
| PATCH | /riscos/{id}/status | muda o status | 200 / 400 / 404 |
| DELETE | /riscos/{id} | remove risco, comentários e confirmações | 204 / 404 |
| GET | /riscos/{riscoId}/comentarios | lista comentários do risco | 200 / 404 |
| POST | /riscos/{riscoId}/comentarios | comenta (qualquer logado) | 201 / 400 / 404 |
| DELETE | /comentarios/{id} | remove (autor ou ADMIN) | 204 / 403 / 404 |
| GET | /riscos/{riscoId}/confirmacoes | `{confirmacoes, negacoes}` | 200 / 404 |
| PUT | /riscos/{riscoId}/confirmacoes | voto do usuário: `{"tipo":"CONFIRMA"}` ou `NEGA` (votar de novo troca) | 200 / 400 / 404 |
| DELETE | /riscos/{riscoId}/confirmacoes | retira o voto do usuário | 204 / 404 |

### Login e usuários

Rotas de leitura (categorias, bairros, riscos, comentários e confirmações) são públicas. Criar/editar/apagar exige
`Authorization: Bearer <token>` (no Swagger, botão **Authorize**).

| Método | Caminho | Quem pode | Status |
|---|---|---|---|
| POST | /auth/registrar | público (vira USUARIO) | 201 / 400 / 409 |
| POST | /auth/login | público, devolve o token | 200 / 401 |
| GET | /usuarios/me | logado | 200 |
| PUT | /usuarios/me | logado | 200 / 400 / 409 |
| PATCH | /usuarios/me/senha | logado | 204 / 400 / 401 |
| DELETE | /usuarios/me | logado | 204 |
| GET | /usuarios, /usuarios/{id} | ADMIN | 200 / 404 |
| POST | /usuarios | ADMIN (cria como USUARIO) | 201 / 400 / 409 |
| PUT | /usuarios/{id} | ADMIN | 200 / 400 / 404 / 409 |
| DELETE | /usuarios/{id} | ADMIN | 204 / 404 |

Permissões: categorias e bairros (POST/PUT/DELETE) só ADMIN. Riscos: criar qualquer
logado; editar/apagar só o dono ou ADMIN (403 caso contrário). Comentários: qualquer
logado comenta; só o autor ou ADMIN apaga.

**Perfil ADMIN:** a tabela `usuario` não tem coluna de perfil, então ADMIN é o usuário cujo
e-mail é `app.admin.email` (padrão `admin@riskmapping.com`); todos os outros são USUARIO.
Esse e-mail é reservado (ninguém se cadastra com ele) e o ADMIN não pode trocá-lo.
Para ter vários admins seria preciso uma coluna `perfil` no banco.

## Segurança

### Banco de dados
- Todo acesso ao banco usa **consultas parametrizadas** (`JdbcClient` com parâmetros nomeados): nenhum valor do
  usuário é concatenado no SQL, o que evita SQL injection. Nos filtros de busca, `%` e `_` são escapados.
- A senha do usuário nunca é guardada: só o hash (veja "Criptografia"). O hash nunca é devolvido pela API.

### Variáveis de ambiente e segredos
Nenhum segredo fica no código nem no Git. Eles vêm de variáveis de ambiente ou de
`config/application.properties` (local, listado no `.gitignore`); no Docker, do `.env` (também ignorado).

| Variável | Para quê | Observação |
|---|---|---|
| `DB_USUARIO` | usuário do banco | padrão `root` |
| `DB_SENHA` | senha do banco | |
| `DB_NOME` | nome do banco | padrão `risk_mapping` |
| `DB_HOST`, `DB_PORTA`, `DB_SSL` | servidor, porta e modo TLS | padrão `localhost`, `3306`, `PREFERRED`; fixos no `render.yaml` em produção |
| `JWT_SECRET` | chave que assina os tokens | **obrigatória**, mínimo 32 caracteres |
| `ADMIN_SENHA` | senha da conta ADMIN | **obrigatória** |
| `ADMIN_EMAIL` | e-mail do ADMIN | padrão `admin@riskmapping.com` |

`JWT_SECRET` e `ADMIN_SENHA` **não têm valor padrão**: se faltarem, a API se recusa a subir, em vez de
rodar com um segredo fraco. No Render, `JWT_SECRET` é gerado aleatoriamente (`generateValue`) e as demais
são preenchidas no painel (`sync: false`), então não vão para o Git.

### Criptografia
- **Senhas: BCrypt** (fator de custo 10, com salt aleatório embutido no hash). É um hash de mão única, não
  dá para recuperar a senha. Fica em `senha_hash` no formato `$2a$10$...` (60 caracteres). O BCrypt só lê
  os 72 primeiros bytes, por isso a senha tem limite de 72 caracteres.
- **Token: JWT assinado com HMAC-SHA** (HS256, HS384 ou HS512, a biblioteca escolhe conforme o tamanho do
  `JWT_SECRET`; quanto maior o segredo, mais forte) usando o `JWT_SECRET`. A assinatura garante que o token não foi
  alterado, mas o conteúdo **não é criptografado** (qualquer um lê o payload, que só tem o id do usuário e as
  datas). Por isso não se guarda nada sensível nele.
- **Em trânsito:** a conexão com o banco usa TLS. O HTTPS da API vem do Render; atrás do proxy, a API usa os
  cabeçalhos `X-Forwarded-*` para montar as URLs corretas.

### Token com expiração
- O login (`POST /auth/login`) devolve um token que **expira em 120 minutos** (`app.jwt.expiracao-minutos`).
  Depois disso as rotas protegidas respondem **401** e é preciso fazer login de novo.
- As rotas protegidas exigem `Authorization: Bearer <token>`. Sem o cabeçalho, com token inválido, expirado,
  com assinatura alterada ou com `alg=none`, a resposta é 401.
- O token só carrega o id do usuário. **A cada requisição a API busca o usuário no banco**, então: se a conta
  for apagada o token deixa de valer na hora, e o perfil (ADMIN ou USUARIO) vale o do banco, não o do token.
- A API é *stateless* (sem sessão nem cookie), então não há CSRF. Como o token não fica guardado no servidor,
  ele **não pode ser revogado** antes de expirar (não existe "logout" no servidor): apague o token no front.
- Os tokens sobrevivem a reinícios da API, desde que o `JWT_SECRET` seja o mesmo. Trocar o `JWT_SECRET`
  invalida todos os tokens emitidos.

### Login e contas
- **Limite de tentativas:** 5 falhas por e-mail+IP, 20 por IP e 50 por e-mail em 15 minutos; depois disso
  responde 429 (`Retry-After: 900`). Os contadores ficam em memória e zeram ao reiniciar a API.
- O tempo de resposta não revela se o e-mail existe (o BCrypt roda sempre) e a mensagem de erro é a mesma
  para e-mail inexistente e senha errada.
- **Perfil ADMIN:** é o usuário cujo e-mail é `ADMIN_EMAIL` (a tabela não tem coluna de perfil). Esse e-mail é
  reservado (ninguém se cadastra com ele) e o ADMIN não pode trocá-lo. Ao subir, a API cria a conta ADMIN ou
  sincroniza a senha com `ADMIN_SENHA`.
- Quem se cadastra sozinho é sempre USUARIO. Editar ou apagar um risco ou comentário é só do dono ou de um ADMIN (403).
- Ao criar usuário (`/auth/registrar` e `POST /usuarios`) é preciso enviar `confirmarSenha`, igual a `senha`;
  se faltar ou for diferente, a API responde 400 (`senhasIguais`).
- O cadastro (`/auth/registrar`) responde 409 para e-mail já usado: aceito para não prejudicar quem se
  cadastra; corrigir de verdade exige confirmação por e-mail.
- **CORS:** a API não libera nenhuma origem de navegador, porque é só API (consumida por clientes HTTP e
  pelo Swagger, que roda na mesma origem). Um site em outro domínio não consegue chamá-la pelo navegador.

Valores: `gravidade` = número de 1 a 5; `status` = ABERTO, CONFIRMADO, RESOLVIDO;
confirmação `tipo` = CONFIRMA, NEGA.

## Exemplo rápido

```bash
# login (devolve o token)
curl -X POST localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@riskmapping.com","senha":"..."}'

# criar um risco (categoriaId e bairroId vêm de GET /categorias e GET /bairros)
curl -X POST localhost:8080/api/v1/riscos \
  -H "Content-Type: application/json" -H "Authorization: Bearer <token>" \
  -d '{"titulo":"Rio transbordando","latitude":-23.65,"longitude":-46.71,"gravidade":4,"categoriaId":1,"bairroId":3}'
```
