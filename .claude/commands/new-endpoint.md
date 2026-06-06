# Comando: /new-endpoint

Scaffolda todas as camadas de um novo endpoint no psicomanager-api seguindo os padrões do projeto.

## Como usar

```
/new-endpoint dominio=<dominio> acao=<acao> metodo=<GET|POST|PATCH|DELETE> rota=<rota>
```

Exemplo: `/new-endpoint dominio=schedule acao=archive metodo=PATCH rota=/schedules/{id}/archive`

## O que este comando faz

Leia os arquivos do domínio antes de implementar qualquer coisa. Para o domínio `schedule`, as referências são:
- `src/main/java/com/psicomanager/api/schedule/ScheduleController.java`
- `src/main/java/com/psicomanager/api/schedule/ScheduleService.java`
- `src/main/java/com/psicomanager/api/schedule/exception/`

Para outros domínios, ler os arquivos equivalentes.

---

## Camada 1 — Exception (se a ação pode falhar por regra de negócio)

Localização: `src/main/java/com/psicomanager/api/{dominio}/exception/`

```java
package com.psicomanager.api.{dominio}.exception;

import com.psicomanager.api.core.exception.CustomException;

/**
 * Lançada quando a entidade não está em estado válido para a ação {acao}.
 */
public class {Entidade}Already{Acao}Exception extends CustomException {
    public {Entidade}Already{Acao}Exception() {
        super("Mensagem de erro clara em português para o usuário.");
    }
}
```

---

## Camada 2 — Service

No `{Dominio}Service.java`, adicionar método na region correta:

```java
/**
 * Descrição do que o método faz.
 *
 * @param id ID da entidade
 * @throws {Entidade}NotFoundException se a entidade não for encontrada
 * @throws {Entidade}Already{Acao}Exception se a entidade não estiver em estado válido
 */
@Transactional
public void {acao}{Entidade}(String id) {
    log.info("Buscando {entidade} de id " + id + " para {acao}");
    var entity = repo.findById(id).orElseThrow({Entidade}NotFoundException::new);

    if (entity.getStage() != StageEnum.OPENED) {
        throw new {Entidade}Already{Acao}Exception();
    }

    entity.setStage(StageEnum.{ACAO});
    repo.save(entity);
    log.info("{Entidade} de id " + id + " {acao} com sucesso");
}
```

---

## Camada 3 — Controller

No `{Dominio}Controller.java`, adicionar endpoint:

```java
@{Metodo}Mapping("/{id}/{acao}")
public ResponseEntity<BaseResponse<String>> {acao}(@PathVariable String id) {
    log.info("{METODO}: /{dominio}/" + id + "/{acao}");
    {dominio}Service.{acao}{Entidade}(id);
    return ResponseEntity.ok(new BaseResponse<>(true, "Mensagem de sucesso em português!"));
}
```

Para endpoints com body:
```java
@{Metodo}Mapping("/{id}/{acao}")
public ResponseEntity<BaseResponse<String>> {acao}(
        @PathVariable String id,
        @RequestBody @Valid {Acao}DTO data) {
    log.info("{METODO}: /{dominio}/" + id + "/{acao}");
    {dominio}Service.{acao}{Entidade}(id, data);
    return ResponseEntity.ok(new BaseResponse<>(true, "Mensagem de sucesso!"));
}
```

---

## Camada 4 — DTO de entrada (se o endpoint recebe body)

Localização: `src/main/java/com/psicomanager/api/{dominio}/dto/`

```java
package com.psicomanager.api.{dominio}.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

public record {Acao}DTO(
    @NotNull(message = "Campo obrigatório")
    String campoObrigatorio,

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    LocalDateTime data
) {}
```

---

## Camada 5 — Migration Flyway (se houver mudança no banco)

Usar o agente `flyway-expert` para criar a migration com a numeração correta.
Lembrar: última migration é V27, próxima é V28.

---

## Instruções de execução

1. Leia os arquivos do domínio antes de editar qualquer um.
2. Implemente na ordem: Exception → Service → Controller → DTO → Migration.
3. Após cada camada, rode `./mvnw compile -q` e corrija erros antes de continuar.
4. Ao final, use o agente `java-reviewer` para validar o resultado.
5. Se o endpoint for sensível (dados do usuário, ações destrutivas), use o agente `security-guard`.
