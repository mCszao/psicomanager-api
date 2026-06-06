# Comando: /pr-backend

Prepara um Pull Request do psicomanager-api com checklist completo de qualidade antes do merge.

## Como usar

```
/pr-backend branch=<nome-da-branch> titulo=<titulo-do-pr>
```

Exemplo: `/pr-backend branch=feat/session-archive titulo="feat: adiciona ação de arquivar sessão"`

## O que este comando executa

### Passo 1 — Verificações automáticas

Execute em ordem e exiba o resultado de cada uma:

```bash
# 1. Compilação
./mvnw compile -q

# 2. Testes
./mvnw test

# 3. Verificação completa (compila + testa + cobertura JaCoCo)
./mvnw verify -q
```

Se qualquer verificação falhar, **pare e corrija antes de continuar**.

---

### Passo 2 — Checklist de revisão manual

Percorra os arquivos alterados (`git diff main...HEAD --name-only`) e verifique:

**Controllers:**
- [ ] `@Slf4j` presente na classe
- [ ] `log.info(...)` como primeira linha de cada método
- [ ] `@RequestBody @Valid` em endpoints com body
- [ ] Retorno `ResponseEntity<BaseResponse<T>>`
- [ ] Sem lógica de negócio no controller

**Services:**
- [ ] `@Transactional` em métodos de escrita
- [ ] Logs em etapas chave (busca, validação, persistência, sucesso)
- [ ] JavaDoc em métodos públicos não triviais
- [ ] Exceptions específicas de domínio (não `RuntimeException` genérico)

**Exceptions:**
- [ ] Localizadas em `domain/{dominio}/exception/`
- [ ] Estendem `CustomException`
- [ ] Mensagem em português clara para o usuário

**Migrations Flyway (se houver):**
- [ ] Numeração sequencial sem gaps
- [ ] Sem `DROP TABLE` ou `TRUNCATE`
- [ ] `ENGINE=InnoDB` e charset em CREATE TABLE
- [ ] IDs com UUID, não AUTO_INCREMENT

**Segurança:**
- [ ] Novos endpoints não têm `@PermitAll` sem justificativa
- [ ] Sem dados sensíveis nos logs
- [ ] Queries de listagem filtradas por `organizationId`

---

### Passo 3 — Agentes de revisão

Rode os agentes nas classes alteradas:

```
Use the java-reviewer subagent on "src/main/java/com/psicomanager/api/{dominio}/"
```

Se houver mudança em `infra/` ou em autenticação:
```
Use the security-guard subagent on "src/main/java/com/psicomanager/api/infra/"
```

Corrija qualquer problema crítico (❌) antes de continuar.

---

### Passo 4 — Gerar descrição do PR

Produza um texto pronto para colar no GitHub:

```markdown
## O que muda
<resumo das alterações em bullet points>

## Tipo de mudança
- [ ] Bug fix
- [ ] Nova feature
- [x] <marcar o correto>

## Checklist
- [x] ./mvnw compile passou
- [x] ./mvnw test passou
- [x] Controllers com @Slf4j e logs de request
- [x] Services com @Transactional e JavaDoc
- [x] Exceptions estendem CustomException
- [x] Migrations Flyway corretas (se houver)
- [x] Sem dados sensíveis em logs
- [x] Queries filtradas por organizationId

## Migrations incluídas
<listar migrations novas, se houver — nome do arquivo e o que altera>

## Impacto no frontend
<descrever se algum DTO mudou e o que o frontend precisa atualizar>
```

---

### Passo 5 — Commit e push (aguardar confirmação do usuário)

```bash
git add .
git commit -m "<titulo-do-pr>"
git push origin <branch>
```

**Aguarde confirmação antes de executar o push.**
