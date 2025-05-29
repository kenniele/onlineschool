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
(1, 'Основы программирования', 'Комплексный курс по изучению основ программирования на Java. Освоите синтаксис языка, принципы ООП, работу с коллекциями и исключениями. Курс включает практические задания, проекты и подготовку к собеседованиям.', 2, '2024-01-15 09:00:00', '2024-06-15 18:00:00', true, CURRENT_TIMESTAMP),
(2, 'Веб-разработка', 'Полный курс современной веб-разработки от основ до продвинутых техник. Изучите HTML5, CSS3, JavaScript ES6+, React, Node.js и работу с базами данных. Создадите реальные проекты: лендинг, интернет-магазин и SPA-приложение.', 2, '2024-02-01 10:00:00', '2024-07-01 17:00:00', true, CURRENT_TIMESTAMP),
(3, 'Дизайн интерфейсов', 'Углубленное изучение UX/UI дизайна с практическим подходом. Освоите принципы пользовательского опыта, создание wireframes и прототипов, работу в Figma и Adobe XD. Изучите психологию пользователей и A/B тестирование.', 4, '2024-03-01 11:00:00', '2024-08-01 16:00:00', true, CURRENT_TIMESTAMP),
(4, 'Базы данных', 'Профессиональный курс по работе с реляционными и NoSQL базами данных. Изучите SQL на продвинутом уровне, оптимизацию запросов, индексирование, транзакции. Освоите PostgreSQL, MySQL, MongoDB и Redis.', 4, '2024-04-01 09:30:00', '2024-09-01 18:30:00', true, CURRENT_TIMESTAMP);

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
INSERT INTO webinars (id, title, description, meeting_url, start_time, duration, max_participants, teacher_id, course_id, active, status, created_at) VALUES
(1, 'Введение в Java', 'Изучаем основы программирования на Java: переменные, типы данных, условные операторы, циклы. Практические примеры и упражнения для начинающих разработчиков. Рассмотрим синтаксис языка и основные конструкции ООП.', 'https://zoom.us/j/123456789?pwd=abc123', '2026-01-15 10:00:00', 90, 50, 2, 1, true, 'SCHEDULED', CURRENT_TIMESTAMP),
(2, 'HTML и CSS', 'Основы веб-разработки: HTML разметка, CSS стилизация, адаптивный дизайн. Создаем красивые и функциональные веб-страницы с нуля. Изучим семантическую разметку, современные CSS-техники, Flexbox и Grid Layout.', 'https://meet.google.com/abc-defg-hij', '2026-01-16 14:00:00', 120, 30, 2, 2, true, 'SCHEDULED', CURRENT_TIMESTAMP),
(3, 'Принципы UX дизайна', 'Изучаем принципы пользовательского опыта: исследование пользователей, создание персон, wireframes, прототипирование. Практические кейсы и методы UX-дизайна. Узнаем, как создавать интуитивно понятные интерфейсы.', 'https://teams.microsoft.com/l/meetup-join/19%3ameeting_example', '2026-01-17 16:00:00', 60, 25, 4, 3, true, 'SCHEDULED', CURRENT_TIMESTAMP),
(4, 'SQL Основы', 'Основы работы с базами данных: SQL запросы, создание таблиц, связи между таблицами, индексы. Практические примеры работы с PostgreSQL и MySQL. Изучим SELECT, INSERT, UPDATE, DELETE и сложные запросы с JOIN.', 'https://zoom.us/j/987654321?pwd=xyz789', '2026-01-18 11:00:00', 75, 40, 4, 4, true, 'SCHEDULED', CURRENT_TIMESTAMP);

-- Участники вебинаров (Many-to-Many связь)
INSERT INTO webinar_participants (webinar_id, user_id) VALUES
(1, 3), (1, 5),
(2, 3),
(3, 5),
(4, 3), (4, 5);

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