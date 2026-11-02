# Imagen del backend para Render (Render no tiene runtime de Java: se despliega con Docker).
# Las variables (DB_URL, JWT_SECRET_KEY, SPOTIFY_*...) se configuran en Render, no aquí.

# 1. Compilación con el Maven Wrapper del proyecto (los tests se pasan en local, no en el despliegue)
FROM eclipse-temurin:23-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml lombok.config ./
RUN chmod +x mvnw && ./mvnw -q dependency:go-offline
COPY src/ src/
RUN ./mvnw -q package -DskipTests

# 2. Ejecución: solo el JRE y el jar
FROM eclipse-temurin:23-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# El plan gratuito de Render tiene 512 MB: la JVM se ajusta a la memoria del contenedor
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
