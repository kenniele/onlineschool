# Online School - Система онлайн обучения

Современная платформа для онлайн обучения с разделением ролей на администраторов, учителей и студентов.

## 🚀 Технологии

- **Backend**: Java 17, Spring Boot 3.2.3, Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf, HTML5, CSS3, JavaScript
- **База данных**: PostgreSQL
- **Сборка**: Maven
- **Контейнеризация**: Docker, Docker Compose

## ⚡ Быстрый старт

### 1. Клонирование и настройка

```bash
# Клонируйте репозиторий
git clone <your-repo-url>
cd onlineschool

# Создайте файл с переменными окружения
cp env.example .env
```

### 2. Запуск через Docker Compose (рекомендуется)

```bash
# Запустите все сервисы
docker-compose up -d

# Или с пересборкой приложения
docker-compose up --build -d
```

Приложение будет доступно по адресу: **http://localhost:8080**

### 3. Остановка сервисов

```bash
# Остановить все сервисы
docker-compose down

# Остановить и удалить данные
docker-compose down -v
```

## 🌐 Доступ к сервисам

| Сервис | URL | Описание |
|--------|-----|----------|
| **Веб-приложение** | http://localhost:8080 | Основная платформа онлайн-школы |
| **pgAdmin** | http://localhost:5050 | Веб-интерфейс для управления PostgreSQL |
| **PostgreSQL** | localhost:5432 | База данных (прямое подключение) |

## 🔐 Тестовые пользователи

| Роль | Логин | Пароль | Возможности |
|------|-------|--------|-------------|
| 👑 **Администратор** | `admin` | `password` | Полный доступ к системе, управление пользователями |
| 🎓 **Учитель** | `teacher` | `password` | Создание курсов и вебинаров |
| 📚 **Студент** | `student` | `password` | Просмотр курсов, участие в вебинарах |
| 🎓 **Учитель 2** | `teacher2` | `password` | Дополнительный аккаунт учителя |
| 📚 **Студент 2** | `student2` | `password` | Дополнительный аккаунт студента |

### Вход в систему:
1. Откройте http://localhost:8080
2. Нажмите "Войти"
3. Введите логин и пароль из таблицы выше
4. Вы будете автоматически перенаправлены на соответствующий дашборд

## 📋 API Documentation

### Управление курсами
```http
GET    /api/courses                    # Получить список курсов
POST   /api/courses/{id}/enroll        # Записаться на курс
DELETE /api/courses/{id}/enroll        # Отписаться от курса
GET    /api/courses/{id}/enrollment-status  # Проверить статус записи
```

### Управление вебинарами
```http
GET    /api/webinars                   # Получить список вебинаров
POST   /api/webinars/{id}/join         # Записаться на вебинар
DELETE /api/webinars/{id}/join         # Отписаться от вебинара
GET    /api/webinars/{id}/participation-status  # Проверить статус участия
```

### Пример использования API:
```bash
# Запись на курс с ID 1
curl -X POST http://localhost:8080/api/courses/1/enroll \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Запись на вебинар с ID 1
curl -X POST http://localhost:8080/api/webinars/1/join \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 📁 Структура проекта

```
src/
├── main/
│   ├── java/com/onlineSchool/
│   │   ├── controller/     # REST контроллеры
│   │   ├── model/          # JPA сущности
│   │   ├── repository/     # Spring Data репозитории
│   │   ├── service/        # Бизнес логика
│   │   ├── config/         # Конфигурация Spring
│   │   └── exception/      # Обработка исключений
│   └── resources/
│       ├── templates/      # Thymeleaf шаблоны
│       ├── static/         # CSS, JS, изображения
│       ├── application.properties # Конфигурация
│       └── data.sql        # Начальные данные
└── test/                   # Тесты
```

## ✨ Функциональность

### 👩‍🎓 Для студентов
- Регистрация и авторизация в системе
- Просмотр каталога доступных курсов
- Самостоятельная запись на курсы
- Участие в вебинарах в реальном времени
- Отслеживание прогресса обучения
- Управление своими записями

### 👨‍🏫 Для учителей
- Создание и редактирование курсов
- Планирование и проведение вебинаров
- Просмотр списка записанных студентов
- Статистика по курсам и вебинарам
- Управление содержимым курсов

### 👨‍💼 Для администраторов
- Управление всеми пользователями системы
- Модерация контента и курсов
- Просмотр системной статистики
- Настройка параметров платформы
- Управление ролями и правами доступа

## 🛠️ Разработка

### Локальная разработка

Для разработки без Docker:

```bash
# Создайте .env файл
cp env.example .env

# Запустите только PostgreSQL
docker-compose up -d postgres

# Запустите приложение через Maven
mvn spring-boot:run
```

### Тестирование

```bash
# Запуск всех тестов
mvn test

# Запуск с отчетом о покрытии
mvn test jacoco:report
```

### Сборка

```bash
# Сборка JAR файла
mvn clean package

# Сборка без тестов
mvn clean package -DskipTests
```

## 🐳 Docker

### Конфигурация сервисов

| Сервис | Образ | Порт | Описание |
|--------|-------|------|----------|
| **app** | onlineschool-app | 8080 | Spring Boot приложение |
| **postgres** | postgres:15-alpine | 5432 | База данных PostgreSQL |
| **pgadmin** | dpage/pgadmin4 | 5050 | Веб-интерфейс для PostgreSQL |

### Переменные окружения

Приложение поддерживает следующие переменные окружения (см. `env.example`):

```bash
# База данных
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/onlineschool
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_password

# JWT Security
JWT_SECRET=your_super_secret_key_here_at_least_32_characters_long
JWT_EXPIRATION=86400000

# PostgreSQL для Docker
POSTGRES_PASSWORD=your_strong_password
PGADMIN_DEFAULT_PASSWORD=your_pgadmin_password
```

### Полезные команды

```bash
# Просмотр логов всех сервисов
docker-compose logs

# Просмотр логов конкретного сервиса
docker-compose logs app
docker-compose logs postgres
docker-compose logs pgadmin

# Перезапуск сервисов
docker-compose restart

# Масштабирование (если нужно)
docker-compose up --scale app=2
```

## 🗄️ База данных

### Подключение к PostgreSQL

```
Host: localhost
Port: 5432
Database: onlineschool
Username: postgres
Password: см. переменную POSTGRES_PASSWORD в .env
```

### Доступ через pgAdmin

1. Откройте http://localhost:5050
2. Войдите с данными:
   - **Email**: admin@onlineschool.com
   - **Пароль**: см. PGADMIN_DEFAULT_PASSWORD в .env
3. Добавьте новый сервер:
   - **Host**: postgres
   - **Port**: 5432
   - **Database**: onlineschool
   - **Username**: postgres
   - **Password**: как в POSTGRES_PASSWORD

### Начальные данные

При первом запуске автоматически создаются:
- Пользователи всех ролей (admin, teacher, student)
- Примеры курсов по различным направлениям
- Запланированные вебинары
- Связи между курсами и студентами

## 🔒 Безопасность

### Реализованные меры безопасности:
- ✅ JWT токены для аутентификации
- ✅ Хэширование паролей с помощью BCrypt
- ✅ CORS политики для контроля доступа
- ✅ Валидация входящих данных
- ✅ Проверка прав доступа на уровне контроллеров
- ✅ Защита от XSS и CSRF атак

### Рекомендации для продакшена:
- [ ] Использовать HTTPS для всех соединений
- [ ] Настроить reverse proxy (nginx)
- [ ] Использовать сильные уникальные пароли
- [ ] Настроить мониторинг и алертинг
- [ ] Использовать внешние системы управления секретами
- [ ] Настроить регулярные бэкапы базы данных

## 🚀 Деплой

### Подготовка к продакшену

1. Обновите переменные окружения в `.env`
2. Установите сильные пароли для всех сервисов
3. Настройте доменные имена вместо localhost
4. Настройте SSL сертификаты
5. Настройте мониторинг и логирование

### Docker Compose для продакшена

```bash
# Запуск в продакшене
docker-compose -f docker-compose.prod.yml up -d
```

## 🤝 Вклад в проект

1. Форкните репозиторий
2. Создайте ветку для новой функции (`git checkout -b feature/amazing-feature`)
3. Зафиксируйте изменения (`git commit -m 'Add some amazing feature'`)
4. Отправьте ветку (`git push origin feature/amazing-feature`)
5. Откройте Pull Request

## 📝 Лицензия

Этот проект распространяется под лицензией MIT. Подробности см. в файле `LICENSE`.

## 📞 Поддержка

Если у вас есть вопросы или проблемы:
- Создайте Issue в репозитории
- Проверьте логи: `docker-compose logs`
- Убедитесь, что все сервисы запущены: `docker-compose ps`

---

**Сделано с ❤️ для образования** 