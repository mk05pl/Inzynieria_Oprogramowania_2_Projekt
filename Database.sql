DROP DATABASE IF EXISTS cinema_db;
CREATE DATABASE cinema_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE cinema_db;

CREATE TABLE users (
                       id         VARCHAR(36)  NOT NULL PRIMARY KEY,
                       username   VARCHAR(100) NOT NULL UNIQUE,
                       password   VARCHAR(255) NOT NULL,
                       role       ENUM('CUSTOMER', 'EMPLOYEE', 'MANAGER') NOT NULL,
                       created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE films (
                       id               VARCHAR(36)  NOT NULL PRIMARY KEY,
                       title            VARCHAR(255) NOT NULL,
                       duration_minutes INT          NOT NULL CHECK (duration_minutes > 0),
                       created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE halls (
                       id       VARCHAR(36)  NOT NULL PRIMARY KEY,
                       name     VARCHAR(100) NOT NULL,
                       capacity INT          NOT NULL CHECK (capacity > 0)
);

CREATE TABLE screenings (
                            id         VARCHAR(36) NOT NULL PRIMARY KEY,
                            film_id    VARCHAR(36) NOT NULL,
                            hall_id    VARCHAR(36) NOT NULL,
                            start_time DATETIME    NOT NULL,
                            end_time   DATETIME    NOT NULL,
                            is_3d      TINYINT(1)  NOT NULL DEFAULT 0,
                            created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT fk_screening_film FOREIGN KEY (film_id) REFERENCES films(id),
                            CONSTRAINT fk_screening_hall FOREIGN KEY (hall_id) REFERENCES halls(id),

                            INDEX idx_hall_time (hall_id, start_time, end_time)
);

CREATE TABLE seats (
                       id           VARCHAR(36) NOT NULL PRIMARY KEY,
                       screening_id VARCHAR(36) NOT NULL,
                       row_number   INT         NOT NULL,
                       seat_number  INT         NOT NULL,
                       is_premium   TINYINT(1)  NOT NULL DEFAULT 0,
                       is_reserved  TINYINT(1)  NOT NULL DEFAULT 0,

                       CONSTRAINT fk_seat_screening FOREIGN KEY (screening_id)
                           REFERENCES screenings(id) ON DELETE CASCADE,
                       UNIQUE KEY uq_seat (screening_id, row_number, seat_number)
);

CREATE TABLE tickets (
                         id           VARCHAR(36)   NOT NULL PRIMARY KEY,
                         screening_id VARCHAR(36)   NOT NULL,
                         seat_id      VARCHAR(36)   NOT NULL UNIQUE,
                         owner_id     VARCHAR(36)   NOT NULL,
                         price        DECIMAL(8, 2) NOT NULL,
                         status       ENUM('PURCHASED', 'RESERVED', 'RETURNED') NOT NULL DEFAULT 'PURCHASED',
                         created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT fk_ticket_screening FOREIGN KEY (screening_id) REFERENCES screenings(id),
                         CONSTRAINT fk_ticket_seat      FOREIGN KEY (seat_id)      REFERENCES seats(id),
                         CONSTRAINT fk_ticket_owner     FOREIGN KEY (owner_id)     REFERENCES users(id)
);

INSERT INTO users (id, username, password, role) VALUES
                                                     ('u-mgr-1',  'admin',          'admin123', 'MANAGER'),
                                                     ('u-emp-1',  'kasjer1',        'pass1',    'EMPLOYEE'),
                                                     ('u-cust-1', 'jan.kowalski',   'haslo1',   'CUSTOMER'),
                                                     ('u-cust-2', 'anna.nowak',     'haslo2',   'CUSTOMER'),
                                                     ('u-cust-3', 'piotr.wisniewski','haslo3',  'CUSTOMER');

INSERT INTO films (id, title, duration_minutes) VALUES
                                                    ('f-1', 'Inception',      148),
                                                    ('f-2', 'Interstellar',   169),
                                                    ('f-3', 'The Matrix',     136);

INSERT INTO halls (id, name, capacity) VALUES
                                           ('h-1', 'Sala 1', 30),
                                           ('h-2', 'Sala 2', 50);

INSERT INTO screenings (id, film_id, hall_id, start_time, end_time, is_3d) VALUES
                                                                               ('s-1', 'f-1', 'h-1', '2026-05-10 18:00:00', '2026-05-10 20:28:00', 0),
                                                                               ('s-2', 'f-2', 'h-2', '2026-05-10 18:00:00', '2026-05-10 20:49:00', 1),
                                                                               ('s-3', 'f-3', 'h-1', '2026-05-10 21:00:00', '2026-05-10 23:16:00', 0);

INSERT INTO seats (id, screening_id, row_number, seat_number, is_premium, is_reserved) VALUES
                                                                                           ('st-s1-r1-1',  's-1', 1,  1, 1, 0), ('st-s1-r1-2',  's-1', 1,  2, 1, 0),
                                                                                           ('st-s1-r1-3',  's-1', 1,  3, 1, 0), ('st-s1-r1-4',  's-1', 1,  4, 1, 0),
                                                                                           ('st-s1-r1-5',  's-1', 1,  5, 1, 0), ('st-s1-r1-6',  's-1', 1,  6, 1, 0),
                                                                                           ('st-s1-r1-7',  's-1', 1,  7, 1, 0), ('st-s1-r1-8',  's-1', 1,  8, 1, 0),
                                                                                           ('st-s1-r1-9',  's-1', 1,  9, 1, 0), ('st-s1-r1-10', 's-1', 1, 10, 1, 0),
                                                                                           ('st-s1-r2-1',  's-1', 2,  1, 0, 0), ('st-s1-r2-2',  's-1', 2,  2, 0, 0),
                                                                                           ('st-s1-r2-3',  's-1', 2,  3, 0, 0), ('st-s1-r2-4',  's-1', 2,  4, 0, 0),
                                                                                           ('st-s1-r2-5',  's-1', 2,  5, 0, 0), ('st-s1-r2-6',  's-1', 2,  6, 0, 0),
                                                                                           ('st-s1-r2-7',  's-1', 2,  7, 0, 0), ('st-s1-r2-8',  's-1', 2,  8, 0, 0),
                                                                                           ('st-s1-r2-9',  's-1', 2,  9, 0, 0), ('st-s1-r2-10', 's-1', 2, 10, 0, 0),
                                                                                           ('st-s1-r3-1',  's-1', 3,  1, 0, 0), ('st-s1-r3-2',  's-1', 3,  2, 0, 0),
                                                                                           ('st-s1-r3-3',  's-1', 3,  3, 0, 0), ('st-s1-r3-4',  's-1', 3,  4, 0, 0),
                                                                                           ('st-s1-r3-5',  's-1', 3,  5, 0, 0), ('st-s1-r3-6',  's-1', 3,  6, 0, 0),
                                                                                           ('st-s1-r3-7',  's-1', 3,  7, 0, 0), ('st-s1-r3-8',  's-1', 3,  8, 0, 0),
                                                                                           ('st-s1-r3-9',  's-1', 3,  9, 0, 0), ('st-s1-r3-10', 's-1', 3, 10, 0, 0);

UPDATE seats SET is_reserved = 1 WHERE id = 'st-s1-r2-3';

INSERT INTO tickets (id, screening_id, seat_id, owner_id, price, status) VALUES
    ('t-1', 's-1', 'st-s1-r2-3', 'u-cust-1', 20.00, 'PURCHASED');

SELECT 'users'     AS tabela, COUNT(*) AS wiersze FROM users
UNION ALL
SELECT 'films',    COUNT(*) FROM films
UNION ALL
SELECT 'halls',    COUNT(*) FROM halls
UNION ALL
SELECT 'screenings', COUNT(*) FROM screenings
UNION ALL
SELECT 'seats',    COUNT(*) FROM seats
UNION ALL
SELECT 'tickets',  COUNT(*) FROM tickets;
