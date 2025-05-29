# 📋 АРХИТЕКТУРА ОНЛАЙН-ШКОЛЫ

## 🏗️ ОБЩАЯ АРХИТЕКТУРА

**Проект представляет собой полнофункциональную платформу онлайн-школы**, построенную на **Spring Boot 3.2.3** с использованием классической **3-слойной архитектуры**:

```
┌─────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                    │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────┐│
│  │   Controllers   │ │   Thymeleaf     │ │  REST API   ││
│  │                 │ │   Templates     │ │             ││
│  └─────────────────┘ └─────────────────┘ └─────────────┘│
├─────────────────────────────────────────────────────────┤
│                     BUSINESS LAYER                      │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────┐│
│  │    Services     │ │   Security      │ │  Exception  ││
│  │                 │ │   Config        │ │  Handling   ││
│  └─────────────────┘ └─────────────────┘ └─────────────┘│
├─────────────────────────────────────────────────────────┤
│                      DATA LAYER                         │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────┐│
│  │  Repositories   │ │     Models      │ │ PostgreSQL  ││
│  │   (JPA/H2)      │ │   (Entities)    │ │  Database   ││
│  └─────────────────┘ └─────────────────┘ └─────────────┘│
└─────────────────────────────────────────────────────────┘
```

## 🎯 ДОМЕННАЯ МОДЕЛЬ

Система построена вокруг **7 основных сущностей**:

### 1. User (Пользователь)
```java
@Entity
public class User implements UserDetails {
    private Long id;
    private String username, email, password;
    private String firstName, lastName;
    private Role role; // ADMIN, TEACHER, STUDENT
    // Реализует Spring Security UserDetails
}
```

### 2. Course (Курс)
```java
@Entity
public class Course {
    private Long id;
    private String title, description;
    private LocalDateTime startDate, endDate;
    private boolean active;
    
    @ManyToOne private User teacher;
    @ManyToMany private Set<User> students;
    @OneToMany private List<Webinar> webinars;
}
```

### 3. Webinar (Вебинар)
```java
@Entity
public class Webinar {
    private Long id;
    private String title, description;
    private LocalDateTime startTime;
    private Integer duration;
    private WebinarStatus status; // SCHEDULED, ONGOING, COMPLETED, CANCELLED
    
    @ManyToOne private Course course;
    @ManyToOne private User teacher;
    @ManyToMany private Set<User> participants;
}
```

### 4. Comment (Комментарий)
```java
@Entity
public class Comment {
    private Long id;
    private String content;
    private EntityType entityType; // COURSE, WEBINAR
    private Long entityId;
    private LocalDateTime createdAt, updatedAt;
    
    @ManyToOne private User user;
}
```

### 5. Like (Лайк)
```java
@Entity
public class Like {
    private Long id;
    private EntityType entityType; // COURSE, WEBINAR  
    private Long entityId;
    private LocalDateTime createdAt;
    
    @ManyToOne private User user;
}
```

### 6. UserCourseEnrollment (Запись на курс)
```java
@Entity
public class UserCourseEnrollment {
    private Long id;
    private EnrollmentStatus status; // NOT_STARTED, IN_PROGRESS, COMPLETED
    private LocalDateTime enrolledAt, startedAt, completedAt;
    
    @ManyToOne private User user;
    @ManyToOne private Course course;
}
```

## 🔐 СИСТЕМА БЕЗОПАСНОСТИ

### Аутентификация и Авторизация
- **Spring Security 6** с формой входа
- **Ролевая модель**: ADMIN, TEACHER, STUDENT
- **BCrypt** для хеширования паролей
- **CSRF защита** включена
- **Session-based аутентификация**

### Матрица доступа:
```
┌──────────────┬───────┬─────────┬─────────┐
│   Функция    │ ADMIN │ TEACHER │ STUDENT │
├──────────────┼───────┼─────────┼─────────┤
│ Управление   │   ✅   │    ❌    │    ❌    │
│ пользователями│       │         │         │
├──────────────┼───────┼─────────┼─────────┤
│ Создание     │   ✅   │    ✅    │    ❌    │
│ курсов       │       │         │         │
├──────────────┼───────┼─────────┼─────────┤
│ Создание     │   ✅   │    ✅    │    ❌    │
│ вебинаров    │       │         │         │
├──────────────┼───────┼─────────┼─────────┤
│ Запись на    │   ✅   │    ❌    │    ✅    │
│ курсы        │       │         │         │
├──────────────┼───────┼─────────┼─────────┤
│ Комментарии  │   ✅   │    ✅    │    ✅    │
│ и лайки      │       │         │         │
└──────────────┴───────┴─────────┴─────────┘
```

## 🎨 АРХИТЕКТУРА PRESENTATION СЛОЯ

### Контроллеры (10 штук):

1. **HomeController** - главная страница, курсы, вебинары
2. **AuthController** - вход/регистрация
3. **AdminController** - админская панель
4. **TeacherController** - кабинет преподавателя
5. **StudentController** - кабинет студента
6. **UserController** - управление пользователями
7. **CourseController** - управление курсами
8. **WebinarController** - управление вебинарами
9. **CommentController** - REST API для комментариев
10. **LikeController** - REST API для лайков

### Шаблоны Thymeleaf:
```
templates/
├── fragments/
│   ├── header.html     # Навигация (роли)
│   └── footer.html
├── admin/
│   ├── admin.html      # Админ панель
│   ├── users.html      # Управление пользователями
│   ├── courses.html    # Управление курсами
│   └── webinars.html   # Управление вебинарами
├── teacher/
│   ├── dashboard.html  # Дашборд учителя
│   ├── courses.html    # Курсы учителя
│   └── webinars.html   # Вебинары учителя
├── student/
│   ├── dashboard.html  # Дашборд студента
│   ├── courses.html    # Курсы студента
│   └── progress.html   # Прогресс студента
├── auth/
│   ├── login.html
│   └── register.html
└── public/
    ├── index.html      # Главная страница
    ├── courses.html    # Каталог курсов
    ├── webinars.html   # Список вебинаров
    └── course-details.html
```

## 🔧 БИЗНЕС-ЛОГИКА (Services)

### 9 основных сервисов:

1. **UserService** - управление пользователями, профили
2. **CourseService** - курсы, запись студентов
3. **WebinarService** - вебинары, участники
4. **CommentService** - комментарии к курсам/вебинарам
5. **LikeService** - лайки к курсам/вебинарам
6. **ProgressService** - отслеживание прогресса студентов
7. **UserCourseEnrollmentService** - записи на курсы
8. **CustomUserDetailsService** - интеграция с Spring Security
9. **CustomSecurityService** - дополнительные проверки безопасности

### Ключевые алгоритмы:

**Расчет прогресса студента**:
```java
public Integer calculateCourseProgress(User student, Course course) {
    List<Webinar> courseWebinars = webinarRepository.findByCourseId(course.getId());
    if (courseWebinars.isEmpty()) return 100; // Курс без вебинаров = завершен
    
    long attendedCount = courseWebinars.stream()
        .mapToLong(webinar -> webinar.getParticipants().contains(student) ? 1 : 0)
        .sum();
    
    return (int) ((attendedCount * 100) / courseWebinars.size());
}
```

**Каскадное удаление**:
```java
@Transactional
public void deleteById(Long id) {
    Course course = courseRepository.findById(id).get();
    
    // 1. Удаляем связанные вебинары и их данные
    webinarRepository.findByCourseId(id).forEach(webinar -> {
        commentRepository.deleteAll(commentRepository.findByEntityTypeAndEntityId(WEBINAR, webinar.getId()));
        likeRepository.deleteAll(likeRepository.findByEntityTypeAndEntityId(WEBINAR, webinar.getId()));
    });
    
    // 2. Удаляем записи на курс
    userCourseEnrollmentRepository.deleteAll(userCourseEnrollmentRepository.findByCourse(course));
    
    // 3. Удаляем комментарии и лайки курса
    commentRepository.deleteAll(commentRepository.findByEntityTypeAndEntityId(COURSE, id));
    likeRepository.deleteAll(likeRepository.findByEntityTypeAndEntityId(COURSE, id));
    
    // 4. Удаляем сам курс
    courseRepository.deleteById(id);
}
```

## 🗄️ СЛОЙ ДАННЫХ

### 6 JPA Repositories:
```java
UserRepository extends JpaRepository<User, Long>
CourseRepository extends JpaRepository<Course, Long>  
WebinarRepository extends JpaRepository<Webinar, Long>
CommentRepository extends JpaRepository<Comment, Long>
LikeRepository extends JpaRepository<Like, Long>
UserCourseEnrollmentRepository extends JpaRepository<UserCourseEnrollment, Long>
```

### База данных:
- **PostgreSQL** для production
- **H2** для тестов
- **Hibernate DDL**: `create-drop` (пересоздание схемы)
- **Начальные данные**: `data.sql`

### Схема базы данных:
```sql
-- Пользователи
users (id, username, email, password, first_name, last_name, role)

-- Курсы  
courses (id, title, description, teacher_id, start_date, end_date, active)

-- Many-to-Many: студенты <-> курсы
course_students (course_id, student_id)

-- Вебинары
webinars (id, title, description, course_id, teacher_id, start_time, duration, status, active)

-- Many-to-Many: студенты <-> вебинары  
webinar_participants (webinar_id, participant_id)

-- Комментарии (полиморфные)
comments (id, content, entity_type, entity_id, user_id, created_at, updated_at)

-- Лайки (полиморфные)
likes (id, entity_type, entity_id, user_id, created_at)

-- Записи на курсы
user_course_enrollments (id, user_id, course_id, status, enrolled_at, started_at, completed_at)
```

## 🌐 API АРХИТЕКТУРА

### RESTful эндпоинты:

**Комментарии** (`/api/comments`):
```
GET    /                     # Все комментарии
GET    /{id}                 # Комментарий по ID  
GET    /webinar/{webinarId}  # Комментарии к вебинару
GET    /user/{userId}        # Комментарии пользователя
POST   /                     # Создать комментарий (auth)
PUT    /{id}                 # Обновить (auth + ownership)
DELETE /{id}                 # Удалить (auth + ownership)
```

**Лайки** (`/api/likes`):
```
GET    /entity/{type}/{id}           # Лайки к сущности
GET    /count/{type}/{id}            # Количество лайков
GET    /has-liked/{userId}/{type}/{id} # Проверка лайка
POST   /toggle/{type}/{id}           # Поставить/убрать лайк (auth)
```

### AJAX интеграция:
- Лайки обновляются без перезагрузки страницы
- Комментарии добавляются динамически
- CSRF токены передаются в метатегах

## 🧪 ТЕСТИРОВАНИЕ

**144 теста** разделены на категории:

### Unit тесты:
- **ServiceTest** - тестирование бизнес-логики
- **RepositoryTest** - тестирование репозиториев

### Integration тесты:
- **ControllerTest** - тестирование веб-слоя  
- **ApiIntegrationTest** - тестирование REST API
- **CascadeDeletionTest** - тестирование каскадного удаления
- **ProgressIntegrationTest** - тестирование системы прогресса

### Тестовое окружение:
```properties
spring.profiles.active=test
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
```

## 🔄 ЖИЗНЕННЫЙ ЦИКЛ ЗАПРОСА

**Пример: студент ставит лайк вебинару**

```
1. [BROWSER] POST /api/likes/toggle/WEBINAR/123
   ↓
2. [SECURITY] Spring Security проверяет аутентификацию 
   ↓
3. [CONTROLLER] LikeController.toggleLike()
   ↓  
4. [SERVICE] LikeService.hasLiked() → LikeService.like()
   ↓
5. [REPOSITORY] LikeRepository.save(like)
   ↓
6. [DATABASE] INSERT INTO likes (entity_type, entity_id, user_id, created_at)
   ↓
7. [RESPONSE] JSON {liked: true, likesCount: 42, message: "Лайк поставлен"}
   ↓
8. [FRONTEND] JavaScript обновляет счетчик лайков на странице
```

## 🚀 РАЗВЕРТЫВАНИЕ

### Docker контейнеризация:
```dockerfile
FROM openjdk:21-jdk-slim
COPY target/onlineschool-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Docker Compose:
```yaml
services:
  app:
    build: .
    ports: ["8080:8080"]
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/onlineschool
  db:
    image: postgres:15
    environment:
      - POSTGRES_DB=onlineschool
```

### Makefile команды:
```makefile
make run          # Запуск приложения
make test         # Запуск тестов  
make docker-build # Сборка Docker образа
make docker-up    # Запуск в Docker Compose
```

## ⚡ ПРОИЗВОДИТЕЛЬНОСТЬ И ОПТИМИЗАЦИЯ

### JPA оптимизации:
- `@Query` с JOIN FETCH для избежания N+1 проблемы
- Ленивая загрузка для коллекций
- Кеширование на уровне сессии Hibernate

### Профили конфигурации:
- **dev** - для разработки (H2, подробное логирование)
- **prod** - для продакшена (PostgreSQL, минимальное логирование)  
- **test** - для тестов (H2 в памяти)

### Логирование:
```properties
logging.level.org.springframework=INFO
logging.level.org.hibernate.SQL=DEBUG  
logging.level.com.onlineSchool=DEBUG
```

## 🔮 АРХИТЕКТУРНЫЕ РЕШЕНИЯ

### Полиморфные связи:
Комментарии и лайки используют **Entity Type pattern**:
```java
public enum EntityType { COURSE, WEBINAR }

@Entity
public class Comment {
    private EntityType entityType;  // Тип сущности
    private Long entityId;          // ID сущности
}
```

### Каскадное удаление:
Реализовано **программно** через сервисы вместо JPA CASCADE для большего контроля.

### Ролевая безопасность:
- **Method-level security**: `@PreAuthorize("hasRole('ADMIN')")`
- **Template-level security**: `sec:authorize="hasRole('STUDENT')"`
- **Conditional navigation** в зависимости от роли

## 📊 МЕТРИКИ ПРОЕКТА

```
📁 Структура:
├── 7 моделей данных
├── 6 репозиториев  
├── 9 сервисов
├── 10 контроллеров
├── 144 теста
├── 30+ HTML шаблонов
└── 1500+ строк CSS

📈 Покрытие:
├── Тесты: 144/144 ✅
├── Компиляция: ✅  
├── Security: ✅
└── Integration: ✅

🔧 Технологии:
├── Spring Boot 3.2.3
├── Spring Security 6
├── Spring Data JPA
├── Thymeleaf
├── PostgreSQL/H2
├── Docker + Docker Compose
├── Maven
└── Lombok
```

## 🎯 КЛЮЧЕВЫЕ ОСОБЕННОСТИ АРХИТЕКТУРЫ

1. **Модульность** - четкое разделение слоев и ответственности
2. **Безопасность** - комплексная ролевая модель с Spring Security
3. **Тестируемость** - 144 теста покрывают все уровни
4. **Расширяемость** - легко добавлять новые роли, сущности, функции
5. **Производительность** - оптимизированные запросы и ленивая загрузка
6. **UX** - AJAX для интерактивности, адаптивный дизайн
7. **DevOps** - полная контейнеризация и автоматизация

Это **enterprise-grade решение** для образовательной платформы с продуманной архитектурой, полным покрытием тестами и готовностью к production развертыванию! 🚀 