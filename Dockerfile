
# Usar una imagen base de OpenJDK
FROM openjdk:17-jdk-slim

# Copiar el archivo JAR de la aplicación al contenedor
COPY target/viverolite-0.0.1-SNAPSHOT.jar /app/viverolite.jar

# Exponer el puerto en el que la aplicación correrá
EXPOSE 8080

# Comando para ejecutar la aplicación Spring Boot
ENTRYPOINT ["java", "-jar", "/app/viverolite.jar"]
