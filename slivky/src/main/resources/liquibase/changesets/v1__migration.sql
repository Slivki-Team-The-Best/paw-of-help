-- ======================================================
-- Проект: Лапа Помощи (Paw of Help)
-- База данных: PostgreSQL
-- ======================================================

-- 1. Таблица пользователей (волонтеры, сотрудники приютов, владельцы)
CREATE TABLE users (
   id BIGSERIAL PRIMARY KEY,
   email VARCHAR(255) NOT NULL UNIQUE,
   password_hash VARCHAR(255) NOT NULL,
   full_name VARCHAR(255) NOT NULL,
   phone VARCHAR(50),
   location GEOGRAPHY(POINT, 4326), -- Геолокация для поиска рядом
   role VARCHAR(50) NOT NULL CHECK (role IN ('VOLUNTEER', 'SHELTER_STAFF', 'PET_OWNER', 'ADMIN')),
   rating DECIMAL(3,2) DEFAULT 0.00,
   volunteer_hours INT DEFAULT 0, -- накопленный волонтерский стаж
   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Профиль компетенций волонтера (связь многие-ко-многим)
CREATE TABLE skills (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE, -- ветпомощь, социализация, фотография, ремонт, транспортировка, фандрайзинг и др.
    category VARCHAR(50)
);

CREATE TABLE volunteer_skills (
              user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
              skill_id BIGINT REFERENCES skills(id) ON DELETE CASCADE,
              experience_years INT DEFAULT 0,
              PRIMARY KEY (user_id, skill_id)
);

-- 3. Предпочтения волонтера (работа с кошками/собаками/всеми)
CREATE TABLE volunteer_preferences (
                   user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                   works_with_cats BOOLEAN DEFAULT TRUE,
                   works_with_dogs BOOLEAN DEFAULT TRUE,
                   works_with_shelters BOOLEAN DEFAULT TRUE,
                   works_with_private BOOLEAN DEFAULT TRUE,
                   availability_schedule JSONB -- например: {"monday": "18:00-22:00", "weekend": "full"}
);

-- 4. Приюты и НКО
CREATE TABLE shelters (
      id BIGSERIAL PRIMARY KEY,
      name VARCHAR(255) NOT NULL,
      description TEXT,
      location GEOGRAPHY(POINT, 4326),
      address TEXT,
      phone VARCHAR(50),
      email VARCHAR(255),
      website VARCHAR(255),
      verified BOOLEAN DEFAULT FALSE,
      created_by BIGINT REFERENCES users(id),
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Сотрудники приюта (связь пользователей с приютами)
CREATE TABLE shelter_staff (
           user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
           shelter_id BIGINT REFERENCES shelters(id) ON DELETE CASCADE,
           position VARCHAR(100),
           PRIMARY KEY (user_id, shelter_id)
);

-- 6. Картотека животных
CREATE TABLE animals (
     id BIGSERIAL PRIMARY KEY,
     name VARCHAR(100),
     type VARCHAR(50) NOT NULL CHECK (type IN ('DOG', 'CAT', 'OTHER')),
     breed VARCHAR(100),
     age INT, -- в месяцах
     gender VARCHAR(10) CHECK (gender IN ('MALE', 'FEMALE', 'UNKNOWN')),
     description TEXT,
     health_status TEXT,
     special_needs TEXT,
     photo_urls TEXT[],
     status VARCHAR(50) DEFAULT 'SEEKING_HELP' CHECK (status IN ('SEEKING_HELP', 'IN_TREATMENT', 'ADOPTED', 'FOSTERED', 'DECEASED')),
     shelter_id BIGINT REFERENCES shelters(id) ON DELETE SET NULL,
     created_by BIGINT REFERENCES users(id),
     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. Задачи / задания для волонтеров
CREATE TABLE tasks (
   id BIGSERIAL PRIMARY KEY,
   title VARCHAR(255) NOT NULL,
   description TEXT,
   task_type VARCHAR(50) NOT NULL CHECK (task_type IN ('MEDICAL', 'TRANSPORT', 'FOSTER', 'CARE', 'EVENT', 'REPAIR', 'PHOTO', 'FUNDRAISING', 'OTHER')),
   priority VARCHAR(20) DEFAULT 'NORMAL' CHECK (priority IN ('URGENT', 'HIGH', 'NORMAL', 'LOW')),
   status VARCHAR(50) DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),

-- Геолокация выполнения задачи
   task_location GEOGRAPHY(POINT, 4326),
   address TEXT,

-- Время выполнения
   scheduled_start TIMESTAMP,
   scheduled_end TIMESTAMP,

-- Связи
   animal_id BIGINT REFERENCES animals(id) ON DELETE SET NULL,
   shelter_id BIGINT REFERENCES shelters(id) ON DELETE CASCADE,
   created_by BIGINT REFERENCES users(id),

-- Назначенный волонтер
   assigned_to BIGINT REFERENCES users(id),

-- Системные поля
   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. Требуемые навыки для задачи (многие-ко-многим)
CREATE TABLE task_required_skills (
                  task_id BIGINT REFERENCES tasks(id) ON DELETE CASCADE,
                  skill_id BIGINT REFERENCES skills(id) ON DELETE CASCADE,
                  PRIMARY KEY (task_id, skill_id)
);

-- 9. Отчеты о выполненных заданиях (для волонтерского стажа и репутации)
CREATE TABLE task_reports (
          id BIGSERIAL PRIMARY KEY,
          task_id BIGINT UNIQUE REFERENCES tasks(id) ON DELETE CASCADE,
          volunteer_id BIGINT REFERENCES users(id),
          completion_report TEXT,
          hours_spent DECIMAL(5,2),
          rating_given INT CHECK (rating_given BETWEEN 1 AND 5),
          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 10. Отзывы на волонтеров
CREATE TABLE volunteer_reviews (
               id BIGSERIAL PRIMARY KEY,
               task_id BIGINT REFERENCES tasks(id) ON DELETE CASCADE,
               reviewer_id BIGINT REFERENCES users(id), -- кто оставил отзыв
               volunteer_id BIGINT REFERENCES users(id),
               rating INT CHECK (rating BETWEEN 1 AND 5),
               comment TEXT,
               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 11. Заявки на передержку (от частных владельцев)
CREATE TABLE foster_requests (
             id BIGSERIAL PRIMARY KEY,
             owner_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
             animal_name VARCHAR(100),
             animal_type VARCHAR(50),
             animal_description TEXT,
             start_date DATE NOT NULL,
             end_date DATE NOT NULL,
             location GEOGRAPHY(POINT, 4326),
             special_requirements TEXT,
             status VARCHAR(50) DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'MATCHED', 'COMPLETED', 'CANCELLED')),
             matched_volunteer_id BIGINT REFERENCES users(id),
             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 12. Пожертвования
CREATE TABLE donations (
       id BIGSERIAL PRIMARY KEY,
       donor_id BIGINT REFERENCES users(id),
       shelter_id BIGINT REFERENCES shelters(id),
       amount DECIMAL(10,2),
       purpose VARCHAR(255),
       status VARCHAR(50) DEFAULT 'PENDING',
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 13. Чат-сообщения
CREATE TABLE chat_messages (
           id BIGSERIAL PRIMARY KEY,
           task_id BIGINT REFERENCES tasks(id) ON DELETE CASCADE,
           sender_id BIGINT REFERENCES users(id),
           message TEXT NOT NULL,
           read BOOLEAN DEFAULT FALSE,
           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 14. Календарь мероприятий
CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    event_location GEOGRAPHY(POINT, 4326),
    address TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    shelter_id BIGINT REFERENCES shelters(id) ON DELETE CASCADE,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 15. Участие волонтеров в мероприятиях
CREATE TABLE event_volunteers (
              event_id BIGINT REFERENCES events(id) ON DELETE CASCADE,
              volunteer_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
              checked_in BOOLEAN DEFAULT FALSE,
              PRIMARY KEY (event_id, volunteer_id)
);

-- ======================================================
-- ИНДЕКСЫ ДЛЯ ОПТИМИЗАЦИИ (геопоиск, фильтрация)
-- ======================================================

-- Геоиндексы
CREATE INDEX idx_users_location ON users USING GIST (location);
CREATE INDEX idx_shelters_location ON shelters USING GIST (location);
CREATE INDEX idx_tasks_location ON tasks USING GIST (task_location);
CREATE INDEX idx_foster_requests_location ON foster_requests USING GIST (location);

-- Индексы для фильтрации задач
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_priority ON tasks(priority);
CREATE INDEX idx_tasks_task_type ON tasks(task_type);
CREATE INDEX idx_tasks_scheduled_start ON tasks(scheduled_start);

-- Индексы для поиска по животным
CREATE INDEX idx_animals_type ON animals(type);
CREATE INDEX idx_animals_status ON animals(status);
CREATE INDEX idx_animals_shelter_id ON animals(shelter_id);

-- Индексы для чата и уведомлений
CREATE INDEX idx_chat_messages_task_id ON chat_messages(task_id);
CREATE INDEX idx_chat_messages_created_at ON chat_messages(created_at);

-- ======================================================
-- ФУНКЦИИ И ТРИГГЕРЫ
-- ======================================================

-- Автоматическое обновление updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_tasks_updated_at BEFORE UPDATE ON tasks
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ======================================================
-- НАЧАЛЬНЫЕ ДАННЫЕ (навыки)
-- ======================================================

INSERT INTO skills (name, category) VALUES
                    ('ветпомощь', 'medical'),
                    ('экстренная помощь', 'medical'),
                    ('инъекции', 'medical'),
                    ('социализация', 'care'),
                    ('фотография', 'media'),
                    ('ремонт', 'technical'),
                    ('транспортировка', 'logistics'),
                    ('фандрайзинг', 'fundraising'),
                    ('передержка', 'foster'),
                    ('выгул собак', 'care'),
                    ('уход за кошками', 'care'),
                    ('администрирование', 'management');

-- ======================================================
-- ПРИМЕРЫ ЗАПРОСОВ ДЛЯ ИНТЕЛЛЕКТУАЛЬНОЙ ЛЕНТЫ
-- ======================================================

-- Пример 1: Найти срочные задачи для ветеринара в радиусе 5 км
/*
SELECT t.*, ST_Distance(u.location, t.task_location) as distance
FROM tasks t
CROSS JOIN users u
WHERE u.id = [VOLUNTEER_ID]
AND t.status = 'OPEN'
AND t.priority = 'URGENT'
AND t.task_type IN ('MEDICAL')
AND ST_DWithin(u.location, t.task_location, 5000)
ORDER BY t.priority = 'URGENT' DESC, distance ASC;
*/

-- Пример 2: Найти волонтеров с нужными навыками для задачи
/*
SELECT u.*, 
array_agg(s.name) as skills,
vr.rating
FROM users u
JOIN volunteer_skills vs ON u.id = vs.user_id
JOIN skills s ON vs.skill_id = s.id
LEFT JOIN volunteer_reviews vr ON u.id = vr.volunteer_id
WHERE s.name IN ('транспортировка', 'ветпомощь')
AND ST_DWithin(u.location, [TASK_LOCATION], 10000)
GROUP BY u.id, vr.rating
ORDER BY vr.rating DESC;
*/