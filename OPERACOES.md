# 🛠️ Runbook de Operação — Produção (Agenda Psico)

> Guia prático para manter, atualizar e diagnosticar o ambiente de produção,
> e como o ciclo de **desenvolvimento → produção** funciona no dia a dia.
>
> Produto (clientes): **Agenda Psico** · Repositórios: `psicomanager-api` (backend) e
> `psicomanager-front` (frontend).
>
> Para o passo a passo histórico de como o ambiente foi montado, veja
> [`DEPLOY_ROADMAP.md`](./DEPLOY_ROADMAP.md).

---

## 1. Coordenadas do ambiente

| Item | Valor |
|---|---|
| VPS (Hostinger) | Ubuntu 24.04 · IP **`2.25.182.112`** |
| Usuário SSH (não-root) | **`mcsmanager`** (grupos: `docker`, `sudo`) |
| Pasta do deploy | **`/opt/psicomanager`** (dono: `mcsmanager`) |
| Arquivos de infra | `/opt/psicomanager/{.env, docker-compose.yml, nginx.conf}` |
| Imagens (GHCR) | `ghcr.io/mcszao/psicomanager-api:latest` · `ghcr.io/mcszao/psicomanager-front:latest` |
| Branch que deploya | api → **`main`** · front → **`master`** |
| Deploy CI | **runners self-hosted** na VPS (`vps-api`, `vps-front`) — sem SSH |
| Acesso SSH | só do seu IP (firewall) · chave `~/.ssh/psicomanager_deploy` (uso manual) |
| Banco | container `psicomanager-db` (MySQL 8) · db `psicomanager` · user `psico_app` |
| URL atual | **http://2.25.182.112** (HTTP/IP — domínio+SSL pendente) |
| Domínio planejado | `agenda.mcszao.tech` (subdomínio, p/ conviver com futuros projetos) |

### Arquitetura

```
Internet → Nginx (container, portas 80/443)
             ├── /api/*  → (rewrite remove /api) → Spring Boot  (container :8080)
             └── /*      →                          Next.js     (container :3000)
                                                     ↘ MySQL    (container :3306, rede interna)
```

Só o nginx expõe portas para fora. api/front/mysql só se falam pela rede Docker `internal`.

---

## 2. Ciclo de desenvolvimento → produção (o fluxo do dia a dia)

O deploy é **automático** via GitHub Actions. O fluxo normal é:

```
1. Desenvolve e testa localmente
   - front: npm run dev / npm run test / npm run build / tsc --noEmit
   - api:   mvn test / rodar local
2. Commit numa branch e abre PR (recomendado) ou commita direto
3. Merge / push no branch de produção:
   - psicomanager-api  → push em  main
   - psicomanager-front → push em master
4. O workflow .github/workflows/deploy.yml dispara sozinho com 2 jobs:
   - build  (runner do GitHub, ubuntu-latest): builda a imagem e publica no GHCR
   - deploy (runner SELF-HOSTED dentro da VPS): docker compose pull + up -d <serviço>
     → roda local na VPS, SEM SSH e SEM abrir a porta 22.
5. Em ~2-3 min a nova versão está no ar.
```

> **Pular o deploy num commit:** inclua `[skip ci]` na mensagem do commit
> (ex: mudanças só de documentação).

### Regras de ouro antes de dar push em produção
- ✅ `npm run build` (front) **precisa passar** — o build de produção faz type-check
  estrito que o `npm run dev` **não** faz. Um erro de tipo derruba o deploy.
- ✅ `mvn package` (api) precisa compilar.
- ✅ Migrations novas seguem a numeração `V{n}__descricao.sql` (ver seção 6).
- ✅ Teste o fluxo afetado localmente antes.

### Acompanhar um deploy
```bash
# Listar execuções recentes
gh run list --repo mCszao/psicomanager-api  --limit 5
gh run list --repo mCszao/psicomanager-front --limit 5

# Acompanhar a última em tempo real
gh run watch <RUN_ID> --repo mCszao/psicomanager-api --exit-status

# Ver logs de uma execução que falhou
gh run view <RUN_ID> --repo mCszao/psicomanager-api --log-failed
```

---

## 3. Acesso à VPS

```bash
# Conexão normal (usa sua chave ~/.ssh/id_ed25519 ou a de deploy)
ssh mcsmanager@2.25.182.112

# Forçando a chave dedicada de deploy
ssh -i ~/.ssh/psicomanager_deploy mcsmanager@2.25.182.112
```

> ⚠️ **Nunca** use `root`. O `mcsmanager` já roda Docker (grupo `docker`) e tem `sudo`
> (com senha) para o que precisar de privilégio.

---

## 4. Operações comuns na VPS

Sempre a partir de `/opt/psicomanager`:

```bash
cd /opt/psicomanager

# Status dos 4 containers
docker compose ps

# Logs em tempo real (escolha o serviço)
docker compose logs -f api
docker compose logs -f front
docker compose logs -f nginx
docker compose logs -f mysql
docker logs psicomanager-api --tail 50      # alternativa por container

# Reiniciar um serviço
docker compose restart api

# Atualizar manualmente um serviço (puxa a :latest do GHCR)
docker compose pull api && docker compose up -d api

# Subir/derrubar tudo
docker compose up -d
docker compose down            # para tudo (NÃO apaga dados)
docker compose down -v         # ⚠️ APAGA TAMBÉM OS VOLUMES (banco!) — cuidado

# Limpar imagens órfãs (libera disco)
docker image prune -f

# Recarregar o nginx após editar nginx.conf (sem derrubar)
docker compose exec nginx nginx -t        # testa a config
docker compose exec nginx nginx -s reload # aplica
```

### Testes rápidos de saúde
```bash
curl -I http://localhost/                              # front (espera 200/307)
curl -s -o /dev/null -w "%{http_code}\n" \
  -X POST http://localhost/api/auth/signIn \
  -H "Content-Type: application/json" -d '{"username":"x","password":"y"}'
# espera um JSON BaseResponse (mesmo que erro de credencial = cadeia OK)
```

---

## 4.5 Runners self-hosted (executam o deploy)

O job `deploy` de cada workflow roda em um runner self-hosted instalado **dentro da VPS**,
como serviço systemd (usuário `mcsmanager`). São 2: `vps-api` e `vps-front`, em
`/home/mcsmanager/runners/{api,front}`. Eles conectam de **saída** ao GitHub (HTTPS),
por isso a porta 22 pode ficar fechada/restrita ao seu IP.

```bash
# Status dos runners
systemctl list-units 'actions.runner.*' --type=service
sudo ./svc.sh status        # dentro de /home/mcsmanager/runners/api (ou front)

# Parar / iniciar / reiniciar
cd /home/mcsmanager/runners/api      # ou .../front
sudo ./svc.sh stop
sudo ./svc.sh start

# Logs
journalctl -u 'actions.runner.mCszao-psicomanager-api.vps-api' -f
```

- Conferir online/offline também em: repo → **Settings → Actions → Runners**.
- **Re-registrar** (recriar VPS, trocar repo, etc.): pegue um token novo na sua máquina
  com `gh api -X POST repos/mCszao/psicomanager-api/actions/runners/registration-token --jq .token`,
  então na VPS rode `./config.sh --url https://github.com/mCszao/psicomanager-api --token <TOKEN> --replace --unattended`
  (scripts auxiliares: `setup-runners.sh` e `install-services.sh` na pasta local `psicomanager-deploy`).

---

## 5. Gerenciar segredos e variáveis de ambiente

Os segredos vivem em **2 lugares**, nunca em arquivo versionado do repositório:

### 5.1 GitHub Secrets (usados pelo CI/CD)
`Repo → Settings → Secrets and variables → Actions`

| Secret | api | front | Descrição |
|---|:--:|:--:|---|
| `GHCR_PAT` | ✅ | ✅ | token p/ login no GHCR (usado no job deploy, na VPS) |
| `NEXT_PUBLIC_API_URL` | — | ✅ | URL da API embutida no build do front |
| `VPS_HOST` `VPS_USER` `VPS_SSH_KEY` | ⚠️ | ⚠️ | **legado** — eram do deploy via SSH (appleboy). Não são mais usados desde a migração para runner self-hosted; podem ser removidos. |

> **Editar pela interface web** (não pelo terminal) para o valor não ficar no histórico
> de comandos. Pelo `gh` CLI, se precisar, prefira ler de arquivo:
> `cmd /c "gh secret set NOME --repo mCszao/repo < arquivo"` (preserva bytes — ver caso da chave SSH na seção 9).

### 5.2 `.env` da VPS (usado pelos containers)
Arquivo `/opt/psicomanager/.env` (só existe na VPS + cópia local não versionada):
```
MYSQL_ROOT_PASSWORD=...
MYSQL_USER=psico_app
MYSQL_PASSWORD=...
JWT_SECRET=...
GITHUB_USERNAME=mcszao        # minúsculo (GHCR não aceita maiúsculas)
GHCR_PAT=...
```
Após editar o `.env`, recrie os containers afetados: `docker compose up -d`.

### 5.3 `NEXT_PUBLIC_API_URL` — atenção
Essa variável é **embutida na imagem do front no momento do build** (não em runtime).
Mudou? → atualize o **secret no GitHub** e **dê push no front** para rebuildar.
- Hoje: `http://2.25.182.112/api`
- Após domínio+SSL: `https://agenda.mcszao.tech/api`

### 5.4 Rotacionar o `GHCR_PAT` (procedimento seguro, sem expor o token)
1. Gere um novo token clássico no GitHub (escopos `write:packages`, `read:packages`, `delete:packages`).
2. **GitHub Secrets:** edite `GHCR_PAT` pela web nos **2 repos** (cole o novo valor).
3. **VPS:** `nano /opt/psicomanager/.env` → troca a linha `GHCR_PAT=` → salva. Depois:
   ```bash
   cd /opt/psicomanager && set -a; . ./.env; set +a
   echo "$GHCR_PAT" | docker login ghcr.io -u "$GITHUB_USERNAME" --password-stdin   # Login Succeeded
   ```
4. Valide com um rerun de workflow (`gh run rerun <id> --repo ...`).
5. **Só então** revogue o token antigo no GitHub.

---

## 6. Banco de dados e migrations (Flyway)

- As migrations ficam em `psicomanager-api/src/main/resources/db/migration/` e rodam
  **automaticamente** quando a api sobe. `ddl-auto=validate` (Hibernate só valida).
- Nova migration: criar `V{n}__descricao.sql` com o próximo número sequencial. No próximo
  deploy da api ela é aplicada sozinha.

### ⚠️ Armadilha de case-sensitivity (Windows dev × Linux prod)
No Windows o MySQL é **case-insensitive** para nomes de tabela; no Linux é
**case-sensitive** por padrão. Uma migration que escreve `ALTER TABLE USERS` (a tabela
foi criada como `users`) **passa no dev e quebra em prod**.

Mitigado com `command: --lower-case-table-names=1` no serviço mysql do `docker-compose.yml`
(faz o Linux se comportar como o Windows). Ainda assim, **mantenha o casing consistente**
nas migrations (use os mesmos nomes/backticks de quando a tabela foi criada).

### Migration falhou em produção?
Sintoma nos logs da api: `Detected failed migration to version N ... Migrations have failed validation`
e o container entra em **restart loop**.
```bash
# 1. Ver o erro SQL real (o primeiro, não a validação dos reinícios)
docker logs psicomanager-api 2>&1 | grep -niE "failed|SQLException|doesn't exist" | head

# 2a. Corrigir o SQL da migration, commitar e deixar o deploy rodar de novo; OU
# 2b. Reparar o histórico do Flyway (avançado):
docker compose exec mysql mysql -u root -p psicomanager \
  -e "DELETE FROM flyway_schema_history WHERE success=0;"
docker compose restart api
```

### Acessar o banco
```bash
docker compose exec mysql mysql -u root -p psicomanager   # senha = MYSQL_ROOT_PASSWORD do .env
```

### Backup / restore (recomendado agendar)
```bash
# Backup
docker compose exec mysql mysqldump -u root -p"$MYSQL_ROOT_PASSWORD" psicomanager \
  > backup_$(date +%F).sql

# Restore
cat backup_2026-06-07.sql | docker compose exec -T mysql mysql -u root -p"$MYSQL_ROOT_PASSWORD" psicomanager
```
> Os dados ficam no volume `psicomanager_mysql_data`. **Nunca** rode `docker compose down -v`
> sem ter backup — `-v` apaga o volume.

---

## 7. Domínio e SSL (HTTPS)

Quando o registro A de `agenda.mcszao.tech` apontar para `2.25.182.112`:

1. Ajustar `server_name` no `nginx.conf` para o domínio, `nginx -s reload`.
2. Emitir o certificado (via container, sem instalar nada no host):
   ```bash
   cd /opt/psicomanager
   docker run --rm \
     -v /etc/letsencrypt:/etc/letsencrypt \
     -v psicomanager_certbot_www:/var/www/certbot \
     certbot/certbot certonly --webroot -w /var/www/certbot \
     -d agenda.mcszao.tech --email SEU_EMAIL --agree-tos --no-eff-email
   ```
3. Trocar o `nginx.conf` para o bloco HTTPS (porta 443 + `ssl_certificate`), `nginx -s reload`.
4. Atualizar o secret `NEXT_PUBLIC_API_URL` → `https://agenda.mcszao.tech/api` e dar push no front.
5. **Renovação automática** (cron do `mcsmanager`, sem sudo):
   ```bash
   # crontab -e  → adicionar:
   0 3 * * 1 docker run --rm -v /etc/letsencrypt:/etc/letsencrypt -v psicomanager_certbot_www:/var/www/certbot certbot/certbot renew --quiet && docker exec psicomanager-nginx nginx -s reload
   ```

---

## 8. Hospedar OUTROS projetos na mesma VPS (futuro)

A VPS foi pensada para receber mais APIs. Recomendações para não conflitar:

- **Um subdomínio por projeto** (`agenda.mcszao.tech`, `projeto2.mcszao.tech`, ...),
  cada um com registro A → `2.25.182.112`.
- **Um diretório por stack** em `/opt/<projeto>/` com seu próprio `docker-compose.yml`.
- **Nginx como roteador central:** o ideal, ao crescer, é ter **um único nginx** (ou Traefik)
  na frente, com um `server { server_name X; ... }` por subdomínio, fazendo proxy para os
  containers de cada projeto. Hoje o nginx vive dentro do compose do Agenda Psico; ao
  adicionar o 2º projeto, considere **promover o nginx/Traefik a um stack separado**
  (`/opt/proxy/`) que conhece todos os upstreams.
- **Redes Docker isoladas** por projeto; o proxy participa das redes que precisa alcançar.
- Atenção ao **uso de recursos** (RAM/CPU) — cada Spring Boot consome ~500-800MB.

---

## 9. Troubleshooting (problemas que já enfrentamos)

| Sintoma | Causa | Solução |
|---|---|---|
| Job `deploy` fica "Queued" e não roda | Runner self-hosted offline | Na VPS: `cd /home/mcsmanager/runners/api && sudo ./svc.sh start`; conferir em Settings → Actions → Runners |
| (Histórico) Deploy via SSH dava `i/o timeout` na porta 22 ou `ssh: no key found` | Firewall restringe SSH ao seu IP / secret de chave corrompido pelo pipe do PowerShell | **Resolvido** migrando o deploy para runner self-hosted (não usa mais SSH nem a porta 22) |
| `docker login ghcr.io` nega no **Windows** (`denied`) mas credenciais válidas | Bug do credential helper do Docker Desktop (`credsStore: desktop`) | Não bloqueia: o CI usa o `GITHUB_TOKEN`; na VPS (Linux) o login funciona normal |
| GHCR rejeita push (`name invalid`) | Nome de imagem com maiúscula (`mCszao`) | Workflows usam `IMAGE_NAME=${GITHUB_REPOSITORY,,}` (minúsculas) |
| `docker compose pull` não acha a imagem | `GITHUB_USERNAME` no `.env` com maiúscula | Tem que ser `mcszao` (minúsculo) |
| api em **restart loop**, logs com `Migrations have failed validation` | Migration falhou (ex: casing de tabela) | Ver seção 6 (corrigir SQL ou limpar `flyway_schema_history`) |
| `502 Bad Gateway` no `/api` | api ainda subindo ou caiu | `docker compose ps` / `logs api`; aguardar o `Started ... in Xs` |
| Build de produção do front quebra com erro de tipo | `npm run dev` não faz type-check estrito; `next build` faz | Rodar `tsc --noEmit` antes do push e corrigir |
| Front aponta para a API errada | `NEXT_PUBLIC_API_URL` é embutida no build | Atualizar o secret e **rebuildar** (push no front) — seção 5.3 |

---

## 10. Onde mora cada coisa (fontes de verdade)

| Config | Versionado no git? | Onde está |
|---|---|---|
| Código api/front | ✅ | repositórios |
| Dockerfiles, workflows | ✅ | repositórios |
| `docker-compose.yml`, `nginx.conf` | ❌ (hoje) | `/opt/psicomanager/` na VPS + cópia local `…/GitHub/psicomanager-deploy/` |
| `.env` de produção (segredos) | ❌ (e nunca deve) | `/opt/psicomanager/.env` na VPS |
| Secrets de CI | ❌ | GitHub Secrets |
| Runners self-hosted | ❌ | `/home/mcsmanager/runners/{api,front}` na VPS (serviços systemd) |

> **Recomendação:** versionar `docker-compose.yml` e `nginx.conf` (sem o `.env`) num repo
> de infra (ex: tornar `psicomanager-deploy` um repositório git, ou uma pasta `deploy/` no
> `psicomanager-api`). Hoje eles existem só na VPS e numa cópia local — se a VPS for perdida,
> essa config precisa ser recriada a partir do `DEPLOY_ROADMAP.md`.

---

## 11. Checklist de segurança

- [ ] `GHCR_PAT` rotacionado (foi exposto durante o setup inicial).
- [ ] Token com escopo mínimo (`read/write/delete:packages`), com expiração.
- [ ] Acesso à VPS só por chave SSH (sem senha); nunca usar `root`.
- [ ] **SSH (22) restrito ao seu IP** no firewall — o deploy não depende mais da 22
  (usa runner self-hosted), então pode manter travado.
- [ ] `.env` de produção fora do git, permissões restritas (`chmod 600`).
- [ ] Backups do MySQL agendados e testados.
- [ ] Firewall: 80/443 abertos ao público; 22 só do seu IP.
- [ ] Revisar periodicamente os alertas do Dependabot (front).
