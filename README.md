# Online School

Информационная система для онлайн-школы, построенная на Spring Boot.

## Требования

- Java 17 или выше
- Maven 3.8 или выше
- Docker и Docker Compose (для запуска в контейнерах)
- Make (опционально, для использования Makefile)

## Запуск приложения

### Используя Maven

```bash
# Сборка проекта
mvn clean package -DskipTests

# Запуск приложения
mvn spring-boot:run
```

### Используя Makefile

```bash
# Сборка проекта
make build

# Запуск приложения
make run
```

### Используя Docker

```bash
# Сборка Docker образа
make docker-build
# или
docker build -t online-school:1.0.0 .

# Запуск контейнера
make docker-run
# или
docker run -p 8080:8080 online-school:1.0.0
```

### Используя Docker Compose

```bash
# Запуск приложения
docker-compose up app

# Остановка приложения
docker-compose down
```

## Запуск тестов

### Используя Maven

```bash
# Запуск всех тестов
mvn test

# Запуск определенного теста
mvn test -Dtest=UserControllerTest
```

### Используя Makefile

```bash
# Запуск всех тестов
make test

# Запуск определенного теста (интерактивный режим)
make test-specific
```

### Используя Docker

```bash
# Запуск тестов в Docker
make docker-test
# или
docker-compose up test
```

## Порты и URL

- Приложение доступно по адресу: http://localhost:8080
- H2 консоль доступна по адресу: http://localhost:8080/h2-console
  - JDBC URL: jdbc:h2:mem:testdb
  - Имя пользователя: sa
  - Пароль: (пустой) 