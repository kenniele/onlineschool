.PHONY: build run clean test docker-build docker-run docker-test test-course test-user test-webinar test-like fix-tests

# Переменные
APP_NAME=online-school
VERSION=1.0.0

# Локальные команды Maven
build:
	mvn clean package -DskipTests

run:
	mvn spring-boot:run

clean:
	mvn clean

test:
	mvn test

# Специфичные тесты по компонентам
test-course:
	mvn test -Dtest=CourseControllerTest -Dsurefire.useFile=false

test-user:
	mvn test -Dtest=UserControllerTest -Dsurefire.useFile=false

test-webinar:
	mvn test -Dtest=WebinarControllerTest -Dsurefire.useFile=false

test-like:
	mvn test -Dtest=LikeControllerTest -Dsurefire.useFile=false

# Docker команды
docker-build:
	docker build -t $(APP_NAME):$(VERSION) .

docker-run:
	docker run -p 8080:8080 $(APP_NAME):$(VERSION)

docker-test:
	docker build -t $(APP_NAME)-test:$(VERSION) -f Dockerfile.test .
	docker run --rm $(APP_NAME)-test:$(VERSION)

# Запуск определенного теста
test-specific:
	@read -p "Введите название теста (например, UserControllerTest): " test_name; \
	mvn test -Dtest=$$test_name

# Настройка БД для тестов
db-setup:
	@echo "Создание H2 базы данных для тестов..."
	@mkdir -p ~/h2-data
	@touch ~/h2-data/testdb.mv.db

# Помощь по доступным командам
help:
	@echo "Доступные команды:"
	@echo "  make build        - Сборка приложения без запуска тестов"
	@echo "  make run          - Запуск приложения с помощью Maven"
	@echo "  make clean        - Очистка директории target"
	@echo "  make test         - Запуск всех тестов"
	@echo "  make test-course  - Запуск тестов для CourseController"
	@echo "  make test-user    - Запуск тестов для UserController"
	@echo "  make test-webinar - Запуск тестов для WebinarController"
	@echo "  make test-like    - Запуск тестов для LikeController"
	@echo "  make test-specific - Запуск определенного теста (интерактивно)"
	@echo "  make docker-build - Сборка Docker образа для приложения"
	@echo "  make docker-run   - Запуск приложения в Docker"
	@echo "  make docker-test  - Запуск тестов в Docker"
	@echo "  make db-setup     - Настройка БД для тестов" 