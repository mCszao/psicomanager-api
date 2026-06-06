# Comando: /new-migration

Cria uma migration Flyway para o psicomanager-api com numeração correta e SQL compatível com MySQL.

## Como usar

```
/new-migration descricao=<descricao> operacao=<create-table|alter-table|seed|index>
```

Exemplo: `/new-migration descricao="adicionar coluna phone em patients" operacao=alter-table`

## O que este comando faz

### Passo 1 — Verificar a numeração

Listar os arquivos em `src/main/resources/db/migration/` e encontrar o maior V{N}.
A migration a criar será `V{N+1}`.

> **Referência atual:** última migration conhecida é `V27__fix-missing-columns.sql`.
> Sempre confirmar lendo o diretório antes de assumir.

### Passo 2 — Nomear o arquivo

```
V{N+1}__descricao-com-hifens-sem-espacos.sql
```

O nome deve ser descritivo e em inglês técnico (padrão do projeto):
- `create-table-reports`
- `alter-table-patients-add-phone`
- `add-index-organization-id-to-alerts`
- `seed-default-plan-templates`

### Passo 3 — Gerar o SQL

Usar os templates do agente `flyway-expert` para o tipo de operação:

**CREATE TABLE:** UUID como PK, InnoDB, utf8mb4, colunas de auditoria, organization_id se multi-tenant.

**ALTER TABLE:** `ADD COLUMN`, `ADD CONSTRAINT`, `ADD INDEX` — sem `DROP` ou `TRUNCATE`.

**SEED:** `INSERT INTO ... VALUES ...` para dados iniciais (ex: contas padrão, templates).

**INDEX:** `CREATE INDEX idx_{tabela}_{coluna} ON {tabela}({coluna});`

### Passo 4 — Criar o arquivo

Escrever o arquivo em `src/main/resources/db/migration/V{N+1}__descricao.sql`.

### Passo 5 — Validar

Verificar:
- [ ] Numeração sequencial sem gaps
- [ ] `ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci` em CREATE TABLE
- [ ] IDs como `VARCHAR(36) NOT NULL DEFAULT (UUID())`
- [ ] Sem DDL destrutivo
- [ ] FK com `ON DELETE` explícito
- [ ] Colunas `created_at` e `updated_at` em tabelas novas

## Instruções de execução

1. SEMPRE leia o diretório de migrations antes de assumir o número.
2. Confirme o SQL com o usuário antes de criar o arquivo se a operação for complexa.
3. Após criar, rode `./mvnw compile -q` para verificar se o Flyway valida o arquivo na inicialização.
