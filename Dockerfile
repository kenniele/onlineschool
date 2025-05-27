# Используем официальный образ OpenJDK 17
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем Maven wrapper и pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Делаем Maven wrapper исполняемым
RUN chmod +x ./mvnw

# Загружаем зависимости (для кэширования слоев)
RUN ./mvnw dependency:go-offline -B

# Копируем исходный код
COPY src ./src

# Собираем приложение
RUN ./mvnw clean package -DskipTests

# Экспонируем порт
EXPOSE 8080

# Запускаем приложение
CMD ["java", "-jar", "target/onlineschool-1.0.0.jar"] 