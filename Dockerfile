# Etapa 1: Compilar la aplicación Java
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
# Descargar dependencias de forma eficiente
RUN mvn dependency:go-offline -B
COPY src ./src
# Compilar el archivo .jar omitiendo los tests para acelerar el despliegue
RUN mvn clean package -DskipTests

# Etapa 2: Imagen ligera para ejecutar el servicio
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copia el archivo .jar compilado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar
EXPOSE 3000
ENTRYPOINT ["java", "-jar", "app.jar"]