# ---------- BUILD ----------
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copier le POM en premier (cache Maven)
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:resolve

# Copier le reste du projet
COPY src ./src

# Build du fat-jar (avec dépendances et instrumentation)
RUN mvn clean package -DskipTests

# ---------- RUNTIME ----------
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copier le jar "jar-with-dependencies"
COPY --from=build /app/target/*-jar-with-dependencies.jar app.jar

# Spark écoute sur 8888 par défaut
EXPOSE 8888

CMD ["java", "-jar", "app.jar"]