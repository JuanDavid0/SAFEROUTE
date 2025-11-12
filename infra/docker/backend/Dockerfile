# Backend Dockerfile
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copiar archivos de Maven
COPY /backend/pom.xml .
COPY /backend/mvnw .
COPY /backend/.mvn .mvn

# Dar permisos de ejecución al wrapper de Maven
RUN chmod +x mvnw

# Descargar dependencias
RUN ./mvnw dependency:go-offline -B

# Copiar código fuente
COPY /backend/src src

# Construir aplicación
RUN ./mvnw clean package -DskipTests

# Ejecutar aplicación
ENV PORT=8080
EXPOSE 8080
CMD ["java", "-jar", "target/backend-0.0.1-SNAPSHOT.jar", "--server.port=${PORT}"]