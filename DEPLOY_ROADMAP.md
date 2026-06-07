# 🚀 DEPLOY ROADMAP — Psicomanager

> Documento de rastreamento de progresso para o agente de IA.
> Antes de qualquer ação, leia a seção **ESTADO ATUAL** e o **CONTEXTO DO PROJETO**.
> Ao concluir cada tarefa, atualize o status e registre decisões relevantes em **NOTAS DO AGENTE**.

---

## 📍 ESTADO ATUAL

```
SISTEMA NO AR     : http://2.25.182.112  (HTTP, via IP)
FASE EM ANDAMENTO : Fases 1-4 e 7 CONCLUIDAS. Pendente: Fase 5 (dominio+SSL) e Fase 6 (E2E)
ÚLTIMA TAREFA     : 7.4 — CI/CD automatico validado nos dois repos
PRÓXIMA AÇÃO      : (opcional) Fase 5 quando houver dominio; rotacionar GHCR_PAT
BLOQUEIOS         : nenhum
```

---

## 🗂️ CONTEXTO DO PROJETO

### Repositórios
| Repositório | Caminho local | Papel |
|---|---|---|
| `psicomanager-api` | `C:\Users\Totem\Documents\GitHub\psicomanager-api` | Backend — Spring Boot 3.3 · Java 17 · Maven |
| `psicomanager-front` | `C:\Users\Totem\Documents\GitHub\psicomanager-front` | Frontend — Next.js 14 · TypeScript |

### O que já está pronto (não mexer)
- ✅ `http-core.ts` já usa `process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080'`
- ✅ `.env.example` e `.env.local` existem no frontend
- ✅ Flyway gerencia todas as migrations do banco
- ✅ JWT com secret em `security.jwt.token.secret` no `application.properties`
- ✅ Porta local da API: `8080` | Porta local do front: `3000`

### Decisão de arquitetura (já definida)
```
Usuário → Nginx (80/443)
            ├── /api/* → Spring Boot (container interno, porta 8080)
            └── /*     → Next.js    (container interno, porta 3000)

Estratégia de URL:
  DEV  → NEXT_PUBLIC_API_URL=http://localhost:8080   (sem prefixo /api)
  PROD → NEXT_PUBLIC_API_URL=https://SEU_DOMINIO/api (com prefixo /api)

Nginx usa rewrite para tirar o /api antes de repassar para o backend:
  /api/auth/signIn → backend recebe → /auth/signIn  (backend não muda)
```

### Variáveis de ambiente de produção
| Variável | Onde vai | Descrição |
|---|---|---|
| `MYSQL_ROOT_PASSWORD` | VPS `.env` | Senha root do MySQL |
| `MYSQL_USER` | VPS `.env` | Usuário da aplicação no MySQL |
| `MYSQL_PASSWORD` | VPS `.env` | Senha do usuário da aplicação |
| `JWT_SECRET` | VPS `.env` | Secret para assinar os tokens JWT |
| `GITHUB_USERNAME` | VPS `.env` | Seu usuário GitHub (para pull das imagens GHCR) |
| `NEXT_PUBLIC_API_URL` | GitHub Secret (front repo) | URL completa da API + /api |
| `VPS_HOST` | GitHub Secret (ambos repos) | IP público da VPS |
| `VPS_USER` | GitHub Secret (ambos repos) | Usuário SSH da VPS (geralmente `root`) |
| `VPS_SSH_KEY` | GitHub Secret (ambos repos) | Chave privada SSH (conteúdo do arquivo) |
| `GHCR_PAT` | VPS `.env` + GitHub Secret | Personal Access Token com `read:packages` |

---

## ✅ CHECKLIST GERAL

```
FASE 1 — Mudanças de Código
  ✅ 1.1  Backend: criar application-prod.properties
  ✅ 1.2  Backend: criar Dockerfile          (docker build OK)
  ✅ 1.3  Frontend: atualizar next.config.mjs (standalone)
  ✅ 1.4  Frontend: criar Dockerfile          (docker build OK)
  ✅ 1.5  Frontend: criar .dockerignore

FASE 2 — CI/CD (GitHub Actions)
  ✅ 2.1  Backend: criar .github/workflows/deploy.yml   (trigger: main)
  ✅ 2.2  Frontend: criar .github/workflows/deploy.yml  (trigger: master)
  ✅ 2.3  GitHub Secrets configurados nos dois repos (via gh CLI)
  ✅ 2.4  Imagens publicadas no GHCR (ghcr.io/mcszao/psicomanager-{api,front})

FASE 3 — VPS: Configuração Inicial
  ✅ 3.1  VPS Hostinger (Ubuntu 24.04) — IP 2.25.182.112
  ✅ 3.2  SSH ok (chave dedicada psicomanager_deploy instalada)
  ✅ 3.3  Docker 29.5 + Compose v5.1 (ja estavam instalados)
  ✅ 3.4  Firewall ja configurado pelo usuario (80/443 livres)
  ✅ 3.5  /opt/psicomanager criado (dono: mcsmanager)
  ✅ 3.6  /opt/psicomanager/.env enviado
  ✅ 3.7  /opt/psicomanager/docker-compose.yml enviado (+ lower-case-table-names=1)
  ✅ 3.8  /opt/psicomanager/nginx.conf enviado (HTTP only)

FASE 4 — Primeiro Deploy (sem SSL)
  ✅ 4.1  GHCR autenticado na VPS
  ✅ 4.2  docker compose pull + up (4 containers)
  ✅ 4.3  Containers rodando (api/db/front/nginx Up)
  ✅ 4.4  Front responde via http://2.25.182.112 (307 -> /login, /login 200)
  ✅ 4.5  API responde via http://2.25.182.112/api (BaseResponse JSON)
  ✅ 4.6  Flyway aplicou todas as migrations (apos fix de casing)

FASE 5 — Domínio e SSL  (PENDENTE — usuario so forneceu IP por enquanto)
  ⬜ 5.1  Registrar/usar domínio
  ⬜ 5.2  Apontar DNS (registro A) para 2.25.182.112
  ⬜ 5.3  Instalar Certbot na VPS
  ⬜ 5.4  Emitir certificado SSL (Let's Encrypt)
  ⬜ 5.5  Atualizar nginx.conf com bloco HTTPS
  ⬜ 5.6  Atualizar NEXT_PUBLIC_API_URL p/ https + rebuild front

FASE 6 — Verificação End-to-End  (PENDENTE — precisa de usuario de teste)
  ⬜ 6.1  Login com usuário de teste
  ⬜ 6.2  Cadastro de novo paciente
  ⬜ 6.3  Criação de agendamento
  ⬜ 6.4  Concluir / cancelar / remarcar sessão
  ⬜ 6.5  Geração de documento PDF
  ⬜ 6.6  Fluxo completo sem erros de console

FASE 7 — Validação do CI/CD Automático
  ✅ 7.1  Workflow Deploy API rodou de ponta a ponta (build+push+SSH)
  ✅ 7.2  Imagens no GHCR atualizadas
  ✅ 7.3  Deploy SSH na VPS automatico OK
  ✅ 7.4  Mesma validação no psicomanager-front (verde)
```

---

## FASE 1 — Mudanças de Código

### 1.1 ⬜ Backend: criar `application-prod.properties`

**Arquivo:** `psicomanager-api/src/main/resources/application-prod.properties`

Esse arquivo só é carregado quando `SPRING_PROFILES_ACTIVE=prod`.
Sobrescreve os valores hardcoded do `application.properties` de dev.

```properties
# Banco de dados (valores vêm das variáveis de ambiente do docker-compose)
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

# JWT
security.jwt.token.secret=${JWT_SECRET}

# Log — dentro do container, sem path do Windows
logging.file.name=/app/logs/app.log

# Reduz ruído em produção
spring.jpa.show-sql=false
logging.level.root=WARN
logging.level.com.psicomanager=INFO
```

**Verificação:** arquivo criado em `src/main/resources/application-prod.properties`. ✓

---

### 1.2 ⬜ Backend: criar `Dockerfile`

**Arquivo:** `psicomanager-api/Dockerfile`

Build multi-stage: etapa de build com Maven, etapa de runtime com JRE leve.

```dockerfile
# ---- Etapa 1: Build com Maven ----
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Copia só o pom.xml primeiro para cachear dependências
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copia o código e builda (pula testes — ambiente CI já rodou)
COPY src ./src
RUN mvn clean package -DskipTests -q

# ---- Etapa 2: Runtime leve ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Cria diretório de logs
RUN mkdir -p /app/logs

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Verificação:** `docker build -t psicomanager-api .` no diretório do backend roda sem erro. ✓

---

### 1.3 ⬜ Frontend: atualizar `next.config.mjs`

**Arquivo:** `psicomanager-front/next.config.mjs`

Adicionar `output: 'standalone'` (necessário para o Docker funcionar com Next.js).

```js
/** @type {import('next').NextConfig} */
const nextConfig = {
  output: 'standalone',
};

export default nextConfig;
```

> ⚠️ **Nota:** `NEXT_PUBLIC_API_URL` é injetada como build-arg no Dockerfile.
> Dev continua usando `.env.local` com `http://localhost:8080` sem mudança.

**Verificação:** `npm run build` roda sem erro e a pasta `.next/standalone/` é gerada. ✓

---

### 1.4 ⬜ Frontend: criar `Dockerfile`

**Arquivo:** `psicomanager-front/Dockerfile`

Build multi-stage. A URL da API é passada como build-arg pelo GitHub Actions.

```dockerfile
# ---- Etapa 1: Dependências ----
FROM node:20-alpine AS deps
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm ci

# ---- Etapa 2: Build ----
FROM node:20-alpine AS builder
WORKDIR /app

COPY --from=deps /app/node_modules ./node_modules
COPY . .

# Recebe a URL da API como argumento de build (vem do GitHub Actions secret)
ARG NEXT_PUBLIC_API_URL
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
ENV NEXT_TELEMETRY_DISABLED=1

RUN npm run build

# ---- Etapa 3: Runtime ----
FROM node:20-alpine AS runner
WORKDIR /app

ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1

# Usuário não-root por segurança
RUN addgroup --system --gid 1001 nodejs \
    && adduser --system --uid 1001 nextjs

COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static
COPY --from=builder /app/public ./public

USER nextjs
EXPOSE 3000

CMD ["node", "server.js"]
```

**Verificação:** `docker build --build-arg NEXT_PUBLIC_API_URL=http://localhost:8080 -t psicomanager-front .` roda sem erro. ✓

---

### 1.5 ⬜ Frontend: criar `.dockerignore`

**Arquivo:** `psicomanager-front/.dockerignore`

Evita copiar arquivos desnecessários para dentro do container no build.

```
node_modules
.next
.git
.env*
coverage
*.md
```

**Verificação:** arquivo criado. ✓

---

## FASE 2 — CI/CD (GitHub Actions)

### 2.1 ⬜ Backend: criar workflow de deploy

**Arquivo:** `psicomanager-api/.github/workflows/deploy.yml`

Dispara no push para `main`. Faz o build da imagem Docker, publica no GHCR, e SSH na VPS para atualizar o container.

```yaml
name: Deploy API

on:
  push:
    branches: [main]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Login no GHCR
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Build e push da imagem
        uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: |
            ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:latest
            ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.sha }}

      - name: Deploy na VPS via SSH
        uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.VPS_HOST }}
          username: ${{ secrets.VPS_USER }}
          key: ${{ secrets.VPS_SSH_KEY }}
          script: |
            cd /opt/psicomanager
            echo "${{ secrets.GHCR_PAT }}" | docker login ghcr.io -u ${{ github.actor }} --password-stdin
            docker compose pull api
            docker compose up -d api
            docker image prune -f
```

**Verificação:** arquivo em `.github/workflows/deploy.yml`. ✓

---

### 2.2 ⬜ Frontend: criar workflow de deploy

**Arquivo:** `psicomanager-front/.github/workflows/deploy.yml`

Igual ao da API, mas passa `NEXT_PUBLIC_API_URL` como build-arg para a imagem Docker.

```yaml
name: Deploy Front

on:
  push:
    branches: [main]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Login no GHCR
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Build e push da imagem
        uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          build-args: |
            NEXT_PUBLIC_API_URL=${{ secrets.NEXT_PUBLIC_API_URL }}
          tags: |
            ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:latest
            ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.sha }}

      - name: Deploy na VPS via SSH
        uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.VPS_HOST }}
          username: ${{ secrets.VPS_USER }}
          key: ${{ secrets.VPS_SSH_KEY }}
          script: |
            cd /opt/psicomanager
            echo "${{ secrets.GHCR_PAT }}" | docker login ghcr.io -u ${{ github.actor }} --password-stdin
            docker compose pull front
            docker compose up -d front
            docker image prune -f
```

**Verificação:** arquivo em `.github/workflows/deploy.yml`. ✓

---

### 2.3 ⬜ Configurar GitHub Secrets

Acessar: **GitHub → repo → Settings → Secrets and variables → Actions → New repository secret**

**Secrets necessários no repo `psicomanager-api`:**
| Secret | Valor |
|---|---|
| `VPS_HOST` | IP público da VPS (ex: `192.168.1.1`) |
| `VPS_USER` | Usuário SSH (geralmente `root`) |
| `VPS_SSH_KEY` | Conteúdo completo da chave privada SSH (`~/.ssh/id_rsa`) |
| `GHCR_PAT` | Personal Access Token com escopo `read:packages` e `write:packages` |

**Secrets necessários no repo `psicomanager-front`:**
| Secret | Valor |
|---|---|
| `VPS_HOST` | (mesmo valor) |
| `VPS_USER` | (mesmo valor) |
| `VPS_SSH_KEY` | (mesmo valor) |
| `GHCR_PAT` | (mesmo valor) |
| `NEXT_PUBLIC_API_URL` | `https://SEU_DOMINIO/api` (ou `http://IP_VPS/api` na fase inicial) |

**Como criar o GHCR_PAT:**
1. GitHub → Profile → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Generate new token (classic)
3. Escopos: `read:packages`, `write:packages`, `delete:packages`
4. Copiar o token gerado

**Verificação:** todos os secrets criados nos dois repos. ✓

---

### 2.4 ⬜ Verificar imagens no GHCR

Depois de fazer o primeiro push para `main` em cada repo:
- Acessar `https://github.com/SEU_USUARIO?tab=packages`
- Verificar que `psicomanager-api` e `psicomanager-front` aparecem como packages
- Se as imagens estiverem privadas, torná-las públicas em: Package → Package settings → Change visibility → Public

**Verificação:** duas imagens visíveis no GHCR com tag `latest`. ✓

---

## FASE 3 — VPS: Configuração Inicial

### 3.1 ⬜ Contratar VPS Hostinger

**Plano recomendado:** KVM 1
- 1 vCPU, 4 GB RAM, 50 GB SSD NVMe
- Sistema operacional: **Ubuntu 22.04 LTS**
- Suficiente para: Spring Boot (~800MB), Next.js (~200MB), MySQL (~500MB), Nginx (~10MB)

**Após contratar:** anotar o IP público da VPS. É o valor que vai em `VPS_HOST`.

---

### 3.2 ⬜ Configurar acesso SSH local

No seu computador local:

```bash
# Gerar par de chaves SSH (se ainda não tiver)
ssh-keygen -t rsa -b 4096 -C "psicomanager-deploy"

# Copiar a chave pública para a VPS
# (use a senha root temporária que a Hostinger enviou por email)
ssh-copy-id root@IP_DA_VPS

# Testar conexão
ssh root@IP_DA_VPS
```

O conteúdo de `~/.ssh/id_rsa` (chave **privada**) é o que vai no secret `VPS_SSH_KEY` do GitHub.

**Verificação:** `ssh root@IP_DA_VPS` conecta sem pedir senha. ✓

---

### 3.3 ⬜ Instalar Docker + Docker Compose na VPS

Executar dentro da VPS via SSH:

```bash
# Atualizar pacotes
apt update && apt upgrade -y

# Instalar dependências
apt install -y ca-certificates curl gnupg lsb-release

# Adicionar repositório oficial do Docker
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] \
  https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" \
  | tee /etc/apt/sources.list.d/docker.list > /dev/null

# Instalar Docker Engine + Compose
apt update
apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# Verificar instalação
docker --version
docker compose version
```

**Verificação:** ambos os comandos retornam versão sem erro. ✓

---

### 3.4 ⬜ Configurar UFW (firewall)

```bash
# Instalar UFW se não estiver presente
apt install -y ufw

# Regras básicas
ufw default deny incoming
ufw default allow outgoing
ufw allow ssh        # porta 22
ufw allow 80/tcp     # HTTP
ufw allow 443/tcp    # HTTPS

# Ativar
ufw --force enable
ufw status
```

> ⚠️ **Não fechar a porta 22** antes de ativar o UFW, senão perde o acesso SSH.

**Verificação:** `ufw status` mostra as 3 portas liberadas. ✓

---

### 3.5 ⬜ Criar estrutura de diretórios na VPS

```bash
mkdir -p /opt/psicomanager
cd /opt/psicomanager
```

**Verificação:** `ls /opt/psicomanager` sem erro. ✓

---

### 3.6 ⬜ Criar `/opt/psicomanager/.env`

Criar o arquivo com os valores reais (não commitar no git):

```bash
cat > /opt/psicomanager/.env << 'EOF'
# MySQL
MYSQL_ROOT_PASSWORD=ESCOLHA_UMA_SENHA_FORTE
MYSQL_USER=psico_app
MYSQL_PASSWORD=ESCOLHA_OUTRA_SENHA_FORTE

# JWT — deve ser uma string longa e aleatória
JWT_SECRET=GERE_UMA_STRING_ALEATORIA_DE_64_CHARS

# GitHub
GITHUB_USERNAME=SEU_USUARIO_GITHUB
GHCR_PAT=SEU_TOKEN_GHCR
EOF
```

**Gerar JWT_SECRET:**
```bash
# Rodar localmente ou na VPS
openssl rand -base64 64
```

**Verificação:** `cat /opt/psicomanager/.env` mostra os valores corretamente. ✓

---

### 3.7 ⬜ Criar `/opt/psicomanager/docker-compose.yml`

```bash
cat > /opt/psicomanager/docker-compose.yml << 'EOF'
services:

  mysql:
    image: mysql:8.0
    container_name: psicomanager-db
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: psicomanager
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - internal
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-uroot", "-p${MYSQL_ROOT_PASSWORD}"]
      interval: 10s
      timeout: 5s
      retries: 10
      start_period: 30s

  api:
    image: ghcr.io/${GITHUB_USERNAME}/psicomanager-api:latest
    container_name: psicomanager-api
    restart: unless-stopped
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/psicomanager?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Cuiaba
      SPRING_DATASOURCE_USERNAME: ${MYSQL_USER}
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
    depends_on:
      mysql:
        condition: service_healthy
    networks:
      - internal

  front:
    image: ghcr.io/${GITHUB_USERNAME}/psicomanager-front:latest
    container_name: psicomanager-front
    restart: unless-stopped
    networks:
      - internal

  nginx:
    image: nginx:alpine
    container_name: psicomanager-nginx
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/conf.d/default.conf:ro
      - /etc/letsencrypt:/etc/letsencrypt:ro
      - certbot_www:/var/www/certbot:ro
    depends_on:
      - api
      - front
    networks:
      - internal

volumes:
  mysql_data:
  certbot_www:

networks:
  internal:
    driver: bridge
EOF
```

**Verificação:** `docker compose config` valida o arquivo sem erros. ✓

---

### 3.8 ⬜ Criar `/opt/psicomanager/nginx.conf` (HTTP only — fase inicial)

Este é o nginx sem SSL, usado na Fase 4. Será atualizado na Fase 5.

```bash
cat > /opt/psicomanager/nginx.conf << 'EOF'
server {
    listen 80;
    server_name _;

    # Desafio Certbot (usado na Fase 5)
    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    # Proxy para a API — remove o prefixo /api antes de repassar
    location /api/ {
        rewrite ^/api/(.*) /$1 break;
        proxy_pass http://psicomanager-api:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Proxy para o frontend
    location / {
        proxy_pass http://psicomanager-front:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_cache_bypass $http_upgrade;
    }
}
EOF
```

**Verificação:** arquivo criado em `/opt/psicomanager/nginx.conf`. ✓

---

## FASE 4 — Primeiro Deploy (sem SSL)

### 4.1 ⬜ Autenticar GHCR na VPS

```bash
# Na VPS, carregue as variáveis do .env
source /opt/psicomanager/.env

# Login no GHCR
echo $GHCR_PAT | docker login ghcr.io -u $GITHUB_USERNAME --password-stdin
```

**Verificação:** mensagem `Login Succeeded`. ✓

---

### 4.2 ⬜ Subir os containers

```bash
cd /opt/psicomanager

# Baixar as imagens e subir tudo
docker compose pull
docker compose up -d

# Acompanhar os logs iniciais
docker compose logs -f --tail=50
```

> ℹ️ Na primeira execução, o MySQL pode demorar 30-60s para estar pronto.
> O container `api` aguarda automaticamente via `healthcheck`.
> Acompanhe: `docker compose logs -f mysql` e `docker compose logs -f api`

**Verificação:** logs da API mostram `Started PsicomanagerApplication in X seconds`. ✓

---

### 4.3 ⬜ Verificar containers rodando

```bash
docker compose ps
```

Esperado: todos com status `running` (mysql, api, front, nginx).

**Verificação:** 4 containers com status `Up`. ✓

---

### 4.4 ⬜ Testar acesso via IP

No navegador ou curl:

```bash
# Frontend
curl -I http://IP_DA_VPS

# API — health check básico
curl http://IP_DA_VPS/api/auth/signIn \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"teste","password":"errada"}' \
  -v
```

> Esperado na API: qualquer resposta JSON (mesmo erro de credenciais) confirma que o nginx → backend está funcionando.

**Verificação:** frontend carrega no navegador, API responde JSON. ✓

---

### 4.5 ⬜ Confirmar Flyway rodou as migrations

```bash
docker compose logs api | grep -i "flyway\|migration\|schema"
```

Esperado: linhas como `Successfully applied X migration(s)` ou `Schema is up to date`.

**Verificação:** nenhum erro de migration nos logs. ✓

---

## FASE 5 — Domínio e SSL

### 5.1 ⬜ Apontar DNS para a VPS

No painel do registrador do domínio, criar registro:
- **Tipo:** A
- **Nome:** `@` (raiz) ou subdomínio desejado (ex: `psicomanager`)
- **Valor:** IP público da VPS

Propagação DNS pode levar até 24h, mas geralmente é minutos.

**Verificação:** `dig SEU_DOMINIO +short` retorna o IP da VPS. ✓

---

### 5.2 ⬜ Atualizar o nginx.conf com o domínio

```bash
# Editar nginx.conf e trocar "server_name _;" por:
# server_name SEU_DOMINIO www.SEU_DOMINIO;
sed -i 's/server_name _;/server_name SEU_DOMINIO www.SEU_DOMINIO;/' /opt/psicomanager/nginx.conf

docker compose exec nginx nginx -s reload
```

---

### 5.3 ⬜ Instalar Certbot e emitir certificado

```bash
# Instalar Certbot
apt install -y certbot

# Emitir certificado (modo standalone — para antes de usar o plugin nginx)
# O nginx precisa estar parado OU usar o desafio webroot
docker compose stop nginx

certbot certonly \
  --standalone \
  --email SEU_EMAIL \
  --agree-tos \
  --no-eff-email \
  -d SEU_DOMINIO \
  -d www.SEU_DOMINIO

docker compose start nginx
```

**Verificação:** `/etc/letsencrypt/live/SEU_DOMINIO/fullchain.pem` existe. ✓

---

### 5.4 ⬜ Atualizar nginx.conf com HTTPS

Substituir o conteúdo completo de `/opt/psicomanager/nginx.conf`:

```nginx
# Redireciona HTTP → HTTPS
server {
    listen 80;
    server_name SEU_DOMINIO www.SEU_DOMINIO;

    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    location / {
        return 301 https://$host$request_uri;
    }
}

# HTTPS
server {
    listen 443 ssl;
    server_name SEU_DOMINIO www.SEU_DOMINIO;

    ssl_certificate     /etc/letsencrypt/live/SEU_DOMINIO/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/SEU_DOMINIO/privkey.pem;

    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_prefer_server_ciphers off;

    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    location /api/ {
        rewrite ^/api/(.*) /$1 break;
        proxy_pass http://psicomanager-api:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location / {
        proxy_pass http://psicomanager-front:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_cache_bypass $http_upgrade;
    }
}
```

```bash
docker compose exec nginx nginx -s reload
```

---

### 5.5 ⬜ Atualizar `NEXT_PUBLIC_API_URL` e redesployar o front

Agora que há um domínio real com HTTPS, atualizar o secret no GitHub:
- `NEXT_PUBLIC_API_URL` → `https://SEU_DOMINIO/api`

Fazer um push qualquer no `psicomanager-front` para disparar o rebuild com a URL correta.

**Verificação:** `https://SEU_DOMINIO` abre o sistema com HTTPS. ✓

---

### 5.6 ⬜ Configurar renovação automática do SSL

```bash
# Testar renovação
certbot renew --dry-run

# O certbot instala um cronjob automático. Verificar:
systemctl status certbot.timer

# Após renovação, o nginx precisa ser recarregado:
# Adicionar hook de pós-renovação
echo '#!/bin/bash
cd /opt/psicomanager
docker compose exec nginx nginx -s reload' > /etc/letsencrypt/renewal-hooks/post/reload-nginx.sh

chmod +x /etc/letsencrypt/renewal-hooks/post/reload-nginx.sh
```

**Verificação:** `certbot renew --dry-run` finaliza com sucesso. ✓

---

## FASE 6 — Verificação End-to-End

### 6.1 ⬜ Login
- Acessar `https://SEU_DOMINIO/login`
- Fazer login com um usuário existente
- Confirmar redirecionamento para o dashboard

### 6.2 ⬜ Cadastro de paciente
- Navegar para `/patients`
- Cadastrar um novo paciente completo
- Verificar que aparece na listagem

### 6.3 ⬜ Criação de agendamento
- Navegar para `/schedules`
- Criar agendamento para o paciente recém-cadastrado
- Verificar que aparece com status `OPENED`

### 6.4 ⬜ Ações de sessão
- Abrir o detalhe do agendamento
- Testar: concluir, cancelar, remarcar, marcar ausência
- Verificar que cada ação muda o status corretamente

### 6.5 ⬜ Geração de PDF
- Gerar documento do paciente
- Verificar download do PDF sem erros

### 6.6 ⬜ Verificar console do navegador
- Abrir DevTools → Console
- Confirmar que não há erros de rede (404, 500, CORS)

**Verificação:** todas as funcionalidades operam sem erros. ✓

---

## FASE 7 — Validação do CI/CD Automático

### 7.1 ⬜ Validar deploy automático do backend

```bash
# No seu computador local, dentro do psicomanager-api:
# Fazer uma mudança mínima (ex: adicionar comentário em algum arquivo)
# e commitar/pushear para main

git add .
git commit -m "chore: validar pipeline de deploy"
git push origin main
```

- Acessar GitHub → psicomanager-api → Actions
- Verificar que o workflow "Deploy API" disparou e concluiu com sucesso ✓
- Verificar no GHCR que a imagem `:latest` foi atualizada ✓
- Verificar na VPS: `docker compose logs api | tail -20` mostra a nova versão ✓

### 7.2 ⬜ Validar deploy automático do frontend

Mesma validação no `psicomanager-front`.

**Verificação:** ambos os workflows rodando de ponta a ponta automaticamente. ✓

---

## 📋 NOTAS DO AGENTE

> *Use esta seção para registrar decisões tomadas, problemas encontrados e soluções aplicadas.*

```
[DATA] [FASE] [TAREFA] — Descrição do que foi feito / problema encontrado / decisão tomada
```

### 2026-06-07 — Fase 1 e 2 (mudanças de código + workflows)

- **[1.1–1.5]** Todos os 5 arquivos criados conforme o roadmap. `docker build` validado
  localmente para front (com `--build-arg NEXT_PUBLIC_API_URL`) e api — ambos OK.

- **[1.3] BLOQUEIO RESOLVIDO — build de produção do front estava quebrado.**
  O `npm run build` (type-check estrito, que o `npm run dev` não faz) falhava com erros
  pré-existentes. Corrigidos:
  - `components/ui/label-input-container.tsx`: removido (código morto, 0 imports, usava
    `placeholder` sem valor + cores cruas fora dos tokens do projeto).
  - `hooks/useCreateSession.ts`: adicionado guard `if (!selectedPatient)` antes de chamar
    `ScheduleFactory` (que exige `patientId: string`). Corrige tipo e melhora UX.
  - `hooks/useTranscription.ts`: tipos da Web Speech API (`SpeechRecognition`,
    `SpeechRecognitionEvent`) não existem na lib DOM padrão → anotados como `any`
    (consistente com o `(window as any)` já usado no arquivo).
  - `util/DateUtils.ts` (DateTimeBuilder): `new Date(...)` recebia strings → envolvido em
    `Number(...)`. Sem mudança de runtime (JS já coagia).
  - `util/sessionActionsConfig.ts`: adicionada entrada `reschedule` ao `CONFIRM_CONFIG`
    (exigida pelo tipo `ConfirmConfigMap` = todas as `PendingAction`).
  - Resultado: `tsc --noEmit` limpo, `npm run build` OK, `.next/standalone` gerado,
    102/102 testes passando.

- **[2.1/2.2] Ajustes obrigatórios nos workflows vs. roadmap original:**
  - **Branch:** front usa `master` (não `main`). Workflow do front dispara em `master`;
    o da api em `main`. Os dois repos têm remote em `github.com/mCszao/...`.
  - **GHCR minúsculas:** usuário `mCszao` tem maiúsculas; GHCR rejeita. Adicionado passo
    `IMAGE_NAME=${GITHUB_REPOSITORY,,}` para gerar `ghcr.io/mcszao/psicomanager-*`.
  - **⚠️ Impacto na Fase 3.6:** no `/opt/psicomanager/.env` da VPS, `GITHUB_USERNAME` deve
    ser **`mcszao`** (minúsculo), senão o `docker compose pull` não acha a imagem.

- **Nota técnica:** backend usa Spring Boot `3.3.1-SNAPSHOT` (repo de snapshots). Funciona,
  mas considerar fixar numa versão release antes de produção definitiva.

### 2026-06-07 — Fases 3 a 7 (VPS, primeiro deploy e CI/CD)

- **VPS:** Hostinger Ubuntu 24.04, IP `2.25.182.112`. **Usuario NAO-root: `mcsmanager`.**
  Docker 29.5 + Compose v5.1 ja instalados; firewall ja ativo (80/443 livres).
- **Bootstrap (sudo, feito pelo usuario):** `usermod -aG docker mcsmanager`,
  `mkdir -p /opt/psicomanager` + `chown mcsmanager`. Assim o deploy roda sem root.
- **Chave SSH:** par dedicado `~/.ssh/psicomanager_deploy` (ed25519) instalado no
  authorized_keys do mcsmanager. A privada e o secret `VPS_SSH_KEY`.
- **Secrets (gh CLI):** VPS_HOST=2.25.182.112, VPS_USER=mcsmanager, VPS_SSH_KEY,
  GHCR_PAT nos dois repos; NEXT_PUBLIC_API_URL=http://2.25.182.112/api no front.
- **Arquivos da VPS:** mantidos fora dos repos em `C:\...\GitHub\psicomanager-deploy\`
  (.env com segredos reais, docker-compose.yml, nginx.conf) e enviados via scp.

- **BLOQUEIO 1 — secret VPS_SSH_KEY corrompido.** `Get-Content | gh secret set` no
  PowerShell re-codifica a chave (quebra o PEM) -> `ssh: no key found`. Corrigido
  regravando via `cmd /c "gh secret set ... < arquivo"` (preserva bytes).

- **BLOQUEIO 2 — Flyway V5 quebrou no Linux (case-sensitivity do MySQL).**
  `V5__alter-table-users.sql` usa `ALTER TABLE USERS` (maiusculo); V1 cria `users`.
  No Windows (dev) e case-insensitive e passa; no Linux (`lower_case_table_names=0`)
  falha com `Table 'psicomanager.USERS' doesn't exist`, deixando a migration "failed".
  **Decisao:** NAO editar a migration (mudaria o checksum e quebraria o Flyway no dev
  local do usuario). Em vez disso, `command: --lower-case-table-names=1` no servico
  mysql do compose (so vale na init -> recriado o volume com `down -v`). Resolve a V5
  e qualquer outro casing divergente de uma vez, alinhando prod ao comportamento do dev.

- **Resultado:** 4 containers Up; API `Started in 9.4s`, Flyway aplicou tudo;
  front 307->/login e /login 200; API responde BaseResponse via nginx. Sistema no ar.
- **CI/CD:** ambos workflows verdes de ponta a ponta (build+push GHCR + deploy SSH).

- **PENDENCIAS / RECOMENDACOES:**
  - 🔐 **Rotacionar o GHCR_PAT** (foi exposto no chat). Gerar novo, atualizar o secret
    nos 2 repos e o `.env` da VPS, revogar o antigo.
  - Fase 5 (dominio + HTTPS/SSL) quando houver dominio: apos emitir o cert, trocar
    NEXT_PUBLIC_API_URL para `https://DOMINIO/api` e dar push no front (rebuild).
  - Workflows usam actions em Node 20 (deprecado a partir de set/2026) — bumpar versoes.
  - Front tem 36 alertas Dependabot — revisar dependencias.

---

## 🔧 REFERÊNCIA RÁPIDA

### Comandos úteis na VPS

```bash
# Ver status dos containers
cd /opt/psicomanager && docker compose ps

# Ver logs em tempo real
docker compose logs -f [mysql|api|front|nginx]

# Reiniciar container específico
docker compose restart api

# Atualizar manualmente uma imagem
docker compose pull api && docker compose up -d api

# Ver logs do nginx
docker compose exec nginx cat /var/log/nginx/error.log

# Acessar o banco de dados
docker compose exec mysql mysql -u root -p psicomanager

# Limpar imagens antigas
docker image prune -f
```

### Estrutura final na VPS

```
/opt/psicomanager/
  ├── .env               ← variáveis de ambiente (NÃO commitar)
  ├── docker-compose.yml ← orquestra os 4 containers
  └── nginx.conf         ← configuração do reverse proxy

/etc/letsencrypt/       ← certificados SSL (gerenciado pelo Certbot)
```

### Ports internas (apenas na rede Docker)

| Serviço | Container | Porta interna |
|---|---|---|
| MySQL | psicomanager-db | 3306 |
| Spring Boot | psicomanager-api | 8080 |
| Next.js | psicomanager-front | 3000 |
| Nginx | psicomanager-nginx | 80 / 443 (expostas) |
