FROM maven:3.9.3-eclipse-temurin-17 AS build
WORKDIR /app

# Копирование файлов с зависимостями для оптимизации кэширования
COPY pom.xml .
RUN mvn dependency:go-offline

# Копирование исходного кода
COPY src ./src

# Сборка приложения
RUN mvn clean package -DskipTests

# Финальный образ
FROM eclipse-temurin:17-jre
WORKDIR /app

# Копирование готового jar-файла
COPY --from=build /app/target/*.jar app.jar

# Порт, на котором работает приложение
EXPOSE 8080

# Запуск приложения
ENTRYPOINT ["java","-jar","app.jar"] 