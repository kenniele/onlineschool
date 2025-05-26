# Online School - Система онлайн обучения

Современная платформа для онлайн обучения с разделением ролей на администраторов, учителей и студентов.

## Технологии

- **Backend**: Java 17, Spring Boot 3.2.3, Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf, HTML5, CSS3, JavaScript
- **База данных**: PostgreSQL
- **Сборка**: Maven
- **Контейнеризация**: Docker, Docker Compose

## Быстрый старт

### Запуск через Docker Compose (рекомендуется)

```bash
# Клонируйте репозиторий
git clone <repository-url>
cd onlineschool

# Запустите все сервисы
docker-compose up -d

# Или с пересборкой приложения
docker-compose up --build -d
```

Приложение будет доступно по адресу: http://localhost:8080

### Остановка сервисов

```bash
# Остановить все сервисы
docker-compose down

# Остановить и удалить данные
docker-compose down -v
```

### Доступ к сервисам

- **Приложение**: http://localhost:8080
- **pgAdmin**: http://localhost:5050 (admin@onlineschool.com / admin)
- **PostgreSQL**: localhost:5432 (postgres / postgres)

## 🔐 Тестовые пользователи

Для входа в систему используйте следующие учетные данные:

| Роль | Логин | Пароль | Описание |
|------|-------|--------|----------|
| **Администратор** | `admin` | `password` | Полный доступ к системе |
| **Учитель** | `teacher` | `password` | Управление курсами и вебинарами |
| **Студент** | `student` | `password` | Просмотр курсов и участие в вебинарах |
| **Учитель 2** | `teacher2` | `password` | Дополнительный аккаунт учителя |
| **Студент 2** | `student2` | `password` | Дополнительный аккаунт студента |

### Как войти в систему:

1. Откройте http://localhost:8080
2. Нажмите "Войти" 
3. Введите логин и пароль из таблицы выше
4. После входа вы будете перенаправлены на соответствующий дашборд

## Структура проекта

```
src/
├── main/
│   ├── java/com/onlineSchool/
│   │   ├── controller/     # REST контроллеры
│   │   ├── model/          # JPA сущности
│   │   ├── repository/     # Spring Data репозитории
│   │   ├── service/        # Бизнес логика
│   │   └── config/         # Конфигурация Spring
│   └── resources/
│       ├── templates/      # Thymeleaf шаблоны
│       ├── static/         # CSS, JS, изображения
│       ├── application.properties # Конфигурация
│       └── data.sql        # Начальные данные
└── test/                   # Тесты
```

## Функциональность

### Для студентов
- Регистрация и авторизация
- Просмотр доступных курсов
- Запись на курсы
- Участие в вебинарах
- Отслеживание прогресса

### Для учителей
- Создание и управление курсами
- Планирование вебинаров
- Просмотр списка студентов
- Статистика по курсам

### Для администраторов
- Управление пользователями
- Модерация контента
- Системная статистика
- Настройки платформы

## Разработка

### Локальная разработка

Если вы хотите запустить приложение локально для разработки:

```bash
# Запустите только PostgreSQL
docker-compose up -d postgres

# Запустите приложение через Maven
mvn spring-boot:run
```

### Запуск тестов

```bash
mvn test
```

### Сборка проекта

```bash
mvn clean package
```

## Docker

### Сервисы

- **app**: Spring Boot приложение
- **postgres**: База данных PostgreSQL
- **pgadmin**: Веб-интерфейс для управления PostgreSQL

### Переменные окружения

Приложение поддерживает следующие переменные окружения:

- `SPRING_DATASOURCE_URL` - URL базы данных
- `SPRING_DATASOURCE_USERNAME` - Имя пользователя БД
- `SPRING_DATASOURCE_PASSWORD` - Пароль БД

### Логи

```bash
# Просмотр логов всех сервисов
docker-compose logs

# Просмотр логов конкретного сервиса
docker-compose logs app
docker-compose logs postgres
```

## База данных

### Подключение к PostgreSQL

```
Host: localhost
Port: 5432
Database: onlineschool
Username: postgres
Password: postgres
```

### Начальные данные

При первом запуске автоматически создаются:
- Тестовые пользователи (admin, teacher, student)
- Примеры курсов
- Примеры вебинаров

## Лицензия

MIT License 