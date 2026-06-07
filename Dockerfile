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
