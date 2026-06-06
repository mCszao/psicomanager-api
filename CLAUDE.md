# Psicomanager — Backend

## Stack
- Java 17 · Spring Boot 3.3 · Spring Security · Spring Data JPA
- MySQL · Flyway (migrations) · JWT (Auth0) · Lombok · OpenHTMLtoPDF

## Comandos essenciais
```bash
./mvnw spring-boot:run          # porta 8080
./mvnw test                     # testes com JaCoCo
./mvnw compile                  # compila sem rodar
./mvnw verify                   # compila + testa + relatório de cobertura
```

## Estrutura de pacotes
```
src/main/java/com/psicomanager/api/
├── controllers/     → camada HTTP (ResponseEntity, logs de request)
├── services/        → regras de negócio (@Transactional, @Slf4j)
├── repositories/    → acesso ao banco (Spring Data JPA)
├── domain/{dominio}/
│   ├── dto/         → DTOs de entrada e resposta
│   ├── enums/       → enums do domínio
│   ├── exception/   → exceptions estendendo CustomException
│   ├── mapper/      → conversão entity ↔ DTO
│   └── model/       → entidades JPA
├── core/dto/        → BaseResponse<T> (wrapper padrão de respostas)
├── core/exception/  → CustomException (base de todas as domain exceptions)
├── infra/           → Security, filtros JWT, tenant
└── utils/
```

## Padrão de resposta
```json
{ "success": true, "object": { ... } }
```
Todas as respostas usam `BaseResponse<T>` de `core/dto/`.

## Padrão de implementação de novos endpoints

Seguir SEMPRE esta sequência ao adicionar uma ação:

### 1. Exception — em `domain/{dominio}/exception/`
```java
public class XxxAlreadyYyyException extends CustomException {
    public XxxAlreadyYyyException() {
        super("Mensagem de erro clara em português");
    }
}
```

### 2. Service — método `@Transactional` com logs `@Slf4j`
```java
@Transactional
public void xxxAction(String id) {
    log.info("Buscando entidade de id " + id + " para xxx");
    var entity = repo.findById(id).orElseThrow(EntityNotFoundException::new);
    if (entity.getStage() != StageEnum.OPENED) throw new EntityAlreadyXxxException();
    entity.setStage(StageEnum.XXX);
    repo.save(entity);
    log.info("Entidade de id " + id + " xxx com sucesso");
}
```

### 3. Controller — `@PatchMapping` com log de request
```java
@PatchMapping("/{id}/xxx")
public ResponseEntity<BaseResponse<String>> xxx(@PathVariable String id) {
    log.info("PATCH: /recursos/" + id + "/xxx");
    service.xxxAction(id);
    return ResponseEntity.ok(new BaseResponse<>(true, "Mensagem de sucesso!"));
}
```

### 4. Migration Flyway (se houver mudança no banco)
Arquivo em `src/main/resources/db/migration/`.
Próxima migration: verificar o maior V{N} existente e usar V{N+1}.
Nomear como: `V{N}__descricao-da-migration.sql`

## Convenções obrigatórias
- Controllers: `@Slf4j` + log de cada request recebido
- Autenticação: JWT no header `Authorization: Bearer <token>`
- Migrations Flyway: `src/main/resources/db/migration/V{N}__descricao.sql`
- Validação de entrada: `@Valid` nos controllers + DTOs com anotações Bean Validation
- Exceptions de domínio: sempre estendem `CustomException` de `core/exception/`
- Documentação: JavaDoc nos métodos de serviço (parâmetros, exceções lançadas, comportamento)
- Separação de regiões com `// region <nome>` / `// endregion` quando o arquivo for longo
- Multi-tenant: toda query filtrada por `organizationId` via `TenantService`

## Enums principais
- `StageEnum`: `OPENED | CONCLUDED | CANCELLED | ABSENT | RESCHEDULED`
- `AttendanceTypeEnum`: `PRESENTIAL | REMOTE`

## Formato de data
`dd-MM-yyyy HH:mm:ss` — anotação `@JsonFormat` nos DTOs.

## Próxima migration Flyway
A última migration é `V27__fix-missing-columns.sql`. A próxima deve ser `V28__...`.
