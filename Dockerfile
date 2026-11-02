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
# El plan gratuito de Render tiene 512 MB y muy poca CPU (Spring tarda minutos en arrancar):
# - Límites de memoria fijos: la JVM no ve el límite del contenedor (con MaxRAMPercentage pasaba de 512 MB).
#   Montón de 256 MB + metaspace + caché de código + pilas de hilos caben con margen.
# - TieredStopAtLevel=1: solo el compilador JIT rápido (C1), que arranca antes con poca CPU
# - UseSerialGC: el recolector más ligero, sin hilos extra que compitan por la CPU
ENV JAVA_OPTS="-Xmx256m -Xss512k -XX:MaxMetaspaceSize=160m -XX:ReservedCodeCacheSize=48m -XX:MaxDirectMemorySize=32m -XX:TieredStopAtLevel=1 -XX:+UseSerialGC"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
