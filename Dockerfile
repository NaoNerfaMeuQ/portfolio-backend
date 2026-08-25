# Estágio 1: Build da Aplicação
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Cache de dependências Maven
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Compilação do código fonte
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Estágio 2: Imagem final enxuta para execução
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/app.jar app.jar

EXPOSE 8080

ENV PORT=8080
ENV JAVA_TOOL_OPTIONS="-Xmx350m -Xms128m"

ENTRYPOINT ["java", "-jar", "app.jar"]
