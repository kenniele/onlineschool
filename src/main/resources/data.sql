-- Начальные данные для приложения
-- Пользователи с правильными BCrypt хешами паролей (password = "password")
INSERT INTO users (username, email, password, first_name, last_name, role, active, created_at) VALUES
('admin', 'admin@onlineschool.com', '$2a$10$VpCwenFb8ptoiJvD9Cr6cOqO10.L0.LYnqDHa23nSGVCm3PTJjT8G', 'Админ', 'Системы', 'ADMIN', true, CURRENT_TIMESTAMP),
('teacher', 'teacher@onlineschool.com', '$2a$10$VpCwenFb8ptoiJvD9Cr6cOqO10.L0.LYnqDHa23nSGVCm3PTJjT8G', 'Иван', 'Петров', 'TEACHER', true, CURRENT_TIMESTAMP),
('student', 'student@onlineschool.com', '$2a$10$VpCwenFb8ptoiJvD9Cr6cOqO10.L0.LYnqDHa23nSGVCm3PTJjT8G', 'Мария', 'Сидорова', 'STUDENT', true, CURRENT_TIMESTAMP),
('teacher2', 'teacher2@onlineschool.com', '$2a$10$VpCwenFb8ptoiJvD9Cr6cOqO10.L0.LYnqDHa23nSGVCm3PTJjT8G', 'Анна', 'Козлова', 'TEACHER', true, CURRENT_TIMESTAMP),
('student2', 'student2@onlineschool.com', '$2a$10$VpCwenFb8ptoiJvD9Cr6cOqO10.L0.LYnqDHa23nSGVCm3PTJjT8G', 'Алексей', 'Смирнов', 'STUDENT', true, CURRENT_TIMESTAMP);

-- Курсы
INSERT INTO courses (id, title, description, teacher_id, start_date, end_date, active, created_at) VALUES
(1, 'Основы программирования', 'Изучение основ программирования на Java', 2, '2024-01-15 09:00:00', '2024-06-15 18:00:00', true, CURRENT_TIMESTAMP),
(2, 'Веб-разработка', 'Создание современных веб-приложений', 2, '2024-02-01 10:00:00', '2024-07-01 17:00:00', true, CURRENT_TIMESTAMP),
(3, 'Дизайн интерфейсов', 'Принципы UX/UI дизайна', 4, '2024-03-01 11:00:00', '2024-08-01 16:00:00', true, CURRENT_TIMESTAMP),
(4, 'Базы данных', 'Работа с реляционными базами данных', 4, '2024-04-01 09:30:00', '2024-09-01 18:30:00', true, CURRENT_TIMESTAMP);

-- Записи студентов на курсы (Many-to-Many связь)
INSERT INTO course_student (course_id, user_id) VALUES
(1, 3), (1, 5),
(2, 3),
(3, 5),
(4, 3), (4, 5);

-- Записи в таблицу user_course_enrollments (детальная информация о записи)
INSERT INTO user_course_enrollments (id, user_id, course_id, status, enrolled_at) VALUES
(1, 3, 1, 'IN_PROGRESS', CURRENT_TIMESTAMP),
(2, 5, 1, 'NOT_STARTED', CURRENT_TIMESTAMP),
(3, 3, 2, 'IN_PROGRESS', CURRENT_TIMESTAMP),
(4, 5, 3, 'NOT_STARTED', CURRENT_TIMESTAMP),
(5, 3, 4, 'NOT_STARTED', CURRENT_TIMESTAMP),
(6, 5, 4, 'NOT_STARTED', CURRENT_TIMESTAMP);

-- Вебинары
INSERT INTO webinars (id, title, description, start_time, duration, max_participants, teacher_id, course_id, active, status, created_at) VALUES
(1, 'Введение в Java', 'Основы языка программирования Java', '2024-12-01 10:00:00', 90, 50, 2, 1, true, 'SCHEDULED', CURRENT_TIMESTAMP),
(2, 'HTML и CSS', 'Основы верстки веб-страниц', '2024-12-02 14:00:00', 120, 30, 2, 2, true, 'SCHEDULED', CURRENT_TIMESTAMP),
(3, 'Принципы UX дизайна', 'Как создавать удобные интерфейсы', '2024-12-03 16:00:00', 60, 25, 4, 3, true, 'SCHEDULED', CURRENT_TIMESTAMP),
(4, 'SQL Основы', 'Введение в SQL и реляционные базы данных', '2024-12-04 11:00:00', 75, 40, 4, 4, true, 'SCHEDULED', CURRENT_TIMESTAMP);

-- Участники вебинаров (Many-to-Many связь)
INSERT INTO webinar_participants (webinar_id, user_id) VALUES
(1, 3), (1, 5),
(2, 3),
(3, 5),
(4, 3), (4, 5);

-- Материалы для вебинаров
INSERT INTO webinar_materials (id, title, file_path, file_type, webinar_id, uploaded_at) VALUES
(1, 'Презентация: Введение в Java', '/materials/java-intro.pdf', 'PDF', 1, CURRENT_TIMESTAMP),
(2, 'Примеры кода Java', '/materials/java-examples.zip', 'ZIP', 1, CURRENT_TIMESTAMP),
(3, 'HTML шаблоны', '/materials/html-templates.zip', 'ZIP', 2, CURRENT_TIMESTAMP),
(4, 'UX Guidelines', '/materials/ux-guidelines.pdf', 'PDF', 3, CURRENT_TIMESTAMP);

-- Комментарии
INSERT INTO comments (id, content, user_id, entity_type, entity_id, created_at) VALUES
(1, 'Отличный курс! Очень понятно объясняется', 3, 'COURSE', 1, CURRENT_TIMESTAMP),
(2, 'Когда будет следующий вебинар?', 5, 'WEBINAR', 1, CURRENT_TIMESTAMP),
(3, 'Спасибо за материалы!', 3, 'WEBINAR', 2, CURRENT_TIMESTAMP);

-- Лайки
INSERT INTO likes (id, user_id, entity_type, entity_id, created_at) VALUES
(1, 3, 'COURSE', 1, CURRENT_TIMESTAMP),
(2, 5, 'COURSE', 1, CURRENT_TIMESTAMP),
(3, 3, 'WEBINAR', 1, CURRENT_TIMESTAMP),
(4, 5, 'WEBINAR', 2, CURRENT_TIMESTAMP); 