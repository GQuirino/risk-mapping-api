# ---- 1) compila o JAR ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# ---- 2) imagem final, só com o Java e o JAR ----
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 app
COPY --from=build /app/target/risk-mapping-api-0.0.1.jar app.jar
USER app

# A porta real vem da variável PORT (o Render define). 8080 é só o padrão local.
EXPOSE 8080

# SerialGC e limite de memória: o plano grátis do Render tem só 512 MB
ENTRYPOINT ["java", "-XX:+UseSerialGC", "-XX:MaxRAMPercentage=70", "-jar", "app.jar"]
