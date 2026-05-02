-- XploreNow seed data for PostgreSQL
-- Usage:
-- 1) Ensure schema exists (run app once with spring.jpa.hibernate.ddl-auto=update)
-- 2) Execute: psql -h localhost -U xplorenow -d xplorenow -f scripts/seed-postgres.sql

BEGIN;

-- Clean tables (child to parent)
DELETE FROM reservation_events;
DELETE FROM ratings;
DELETE FROM reservations;
DELETE FROM news;
DELETE FROM activity_itinerary;
DELETE FROM otp_verifications;
DELETE FROM user_preferences;
DELETE FROM activity_images;
DELETE FROM activity_schedules;
DELETE FROM activities;
DELETE FROM guides;
DELETE FROM user_roles;
DELETE FROM users;
DELETE FROM destinations;
DELETE FROM roles;

-- Roles
INSERT INTO roles (name) VALUES
('TRAVELER'),
('GUIDE'),
('ADMIN');

-- Users
-- Users con contraseña 'password123'
INSERT INTO users (
  created_at, updated_at, email, password_hash, first_name, last_name, phone, enabled
) VALUES
(NOW(), NOW(), 'traveler1@xplorenow.test', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7uqqQOa', 'Sofia', 'Lopez', '+5491111111111', TRUE),
(NOW(), NOW(), 'traveler2@xplorenow.test', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7uqqQOa', 'Mateo', 'Diaz', '+5491222222222', TRUE),
(NOW(), NOW(), 'guide.ba@xplorenow.test', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7uqqQOa', 'Lucia', 'Fernandez', '+5491333333333', TRUE),
(NOW(), NOW(), 'guide.mza@xplorenow.test', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7uqqQOa', 'Nicolas', 'Suarez', '+5491444444444', TRUE),
(NOW(), NOW(), 'admin@xplorenow.test', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7uqqQOa', 'Admin', 'Root', '+5491555555555', TRUE),
-- Agregamos el test@xplorenow.test para tus pruebas de Android
(NOW(), NOW(), 'test@xplorenow.test', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7uqqQOa', 'Usuario', 'Prueba', '+5491100000000', TRUE);

-- User roles
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON
  (u.email LIKE 'traveler%' AND r.name = 'TRAVELER') OR
  (u.email LIKE 'guide.%' AND r.name = 'GUIDE') OR
  (u.email = 'admin@xplorenow.test' AND r.name = 'ADMIN');

-- Destinations
INSERT INTO destinations (
  created_at, updated_at, name, city, country, description, cover_image_url, active
) VALUES
(
  NOW(), NOW(), 'Buenos Aires Centro', 'Buenos Aires', 'Argentina',
  'Circuito urbano por casco historico, arquitectura y cultura local.',
  'https://res.cloudinary.com/dj4j7srsg/image/upload/v1777650767/CENTRO_9_julio_big_yirbpc.jpg', TRUE
),
(
  NOW(), NOW(), 'Mendoza Andes', 'Mendoza', 'Argentina',
  'Experiencias de montana, naturaleza y bodegas en la cordillera.',
  'https://res.cloudinary.com/dj4j7srsg/image/upload/v1777650798/Gaucho-Day-2-1024x602_ztor6d.jpg', TRUE
),
(
  NOW(), NOW(), 'Bariloche Lagos', 'San Carlos de Bariloche', 'Argentina',
  'Aventura, trekking y paisajes de lagos patagonicos.',
  'https://res.cloudinary.com/dj4j7srsg/image/upload/v1777650821/94_qeu9a9.jpg', TRUE
);

-- Guides
INSERT INTO guides (
  created_at, updated_at, user_id, bio, spoken_languages, years_experience, rating
)
SELECT NOW(), NOW(), u.id,
       'Guia local con enfoque en experiencias autenticas.',
       'SPANISH,ENGLISH',
       6,
       4.70
FROM users u
WHERE u.email = 'guide.ba@xplorenow.test';

INSERT INTO guides (
  created_at, updated_at, user_id, bio, spoken_languages, years_experience, rating
)
SELECT NOW(), NOW(), u.id,
       'Especialista en actividades de aventura y enoturismo.',
       'SPANISH,PORTUGUESE',
       8,
       4.85
FROM users u
WHERE u.email = 'guide.mza@xplorenow.test';

-- Activities
INSERT INTO activities (
  created_at, updated_at, name, short_description, full_description, category,
  destination_id, guide_id, duration_minutes, base_price, currency, language,
  meeting_point, meeting_point_latitude, meeting_point_longitude, inclusions, cancellation_policy, highlighted, active
)
SELECT NOW(), NOW(),
       'Free Tour Centro Historico',
       'Recorrido por plazas, edificios iconicos y secretos de la ciudad.',
       'Tour guiado a pie por el centro historico, incluyendo hitos culturales y recomendaciones gastronomicas.',
       'CULTURA',
       d.id,
       g.id,
       120,
       0,
       'ARS',
       'SPANISH',
       'Plaza de Mayo, frente al Cabildo',
       -34.604722,
       -58.371111,
       'Guia local experto',
       'Cancelacion gratuita hasta 24h antes',
       TRUE,
       TRUE
FROM destinations d
JOIN guides g ON TRUE
JOIN users gu ON gu.id = g.user_id
WHERE d.name = 'Buenos Aires Centro' AND gu.email = 'guide.ba@xplorenow.test';

INSERT INTO activities (
  created_at, updated_at, name, short_description, full_description, category,
  destination_id, guide_id, duration_minutes, base_price, currency, language,
  meeting_point, meeting_point_latitude, meeting_point_longitude, inclusions, cancellation_policy, highlighted, active
)
SELECT NOW(), NOW(),
       'Excursion Alta Montana',
       'Ruta panoramica por cordillera y miradores de altura.',
       'Salida de dia completo con paradas en puntos panoramicos de la cordillera andina.',
       'NATURALEZA',
       d.id,
       g.id,
       540,
       49000,
       'ARS',
       'SPANISH',
       'Terminal de Omnibus de Mendoza, darsena 7',
       -32.888889,
       -68.845556,
       'Transporte, guia, snack',
       'Cancelacion gratuita hasta 48h antes',
       TRUE,
       TRUE
FROM destinations d
JOIN guides g ON TRUE
JOIN users gu ON gu.id = g.user_id
WHERE d.name = 'Mendoza Andes' AND gu.email = 'guide.mza@xplorenow.test';

INSERT INTO activities (
  created_at, updated_at, name, short_description, full_description, category,
  destination_id, guide_id, duration_minutes, base_price, currency, language,
  meeting_point, meeting_point_latitude, meeting_point_longitude, inclusions, cancellation_policy, highlighted, active
)
SELECT NOW(), NOW(),
       'Aventura Kayak en Lago',
       'Experiencia de aventura en aguas calmas con equipamiento completo.',
       'Actividad de medio dia para nivel inicial e intermedio con briefing de seguridad.',
       'AVENTURA',
       d.id,
       g.id,
       180,
       35000,
       'ARS',
       'ENGLISH',
       'Puerto San Carlos',
       -41.145556,
       -71.308889,
       'Equipamiento completo y seguro',
       'Cancelacion gratuita hasta 72h antes',
       FALSE,
       TRUE
FROM destinations d
JOIN guides g ON TRUE
JOIN users gu ON gu.id = g.user_id
WHERE d.name = 'Bariloche Lagos' AND gu.email = 'guide.mza@xplorenow.test';

-- News
INSERT INTO news (
  created_at, updated_at, title, short_description, full_description, image_url, active
) VALUES
(
  NOW(), NOW(),
  'Nueva temporada de experiencias urbanas',
  '¡Ya estan disponibles nuevas salidas guiadas en el centro historico!',
  'XploreNow lanza una nueva temporada de actividades urbanas con circuitos renovados, nuevos horarios y experiencias pensadas para viajeros que quieren descubrir la ciudad con una mirada local.',
  'https://res.cloudinary.com/dj4j7srsg/image/upload/v1777650968/Science_cityplanning_541068522_z0luvw.jpg',
  TRUE
),
(
  NOW(), NOW(),
  'Aventura de montana con cupos limitados',
  'Se habilitaron salidas especiales en la cordillera para los proximos fines de semana.',
  'Las excursiones de montana suman nuevas fechas con cupos limitados, ideal para quienes buscan naturaleza, fotografia y una experiencia completa de dia entero.',
  'https://res.cloudinary.com/dj4j7srsg/image/upload/v1777649789/samples/landscapes/nature-mountains.jpg',
  TRUE
),
(NOW(), NOW(),
  'Free tour disponible!',
  'Ya se predispone de vacantes gratuitas para un paseo increible en nuestra casa principal',
  'Descubriran nuestra cultura, historia, vision y mision, con el objetivo de que puedan ser parte de nosotros y conocernos mas.',
  'https://res.cloudinary.com/dj4j7srsg/image/upload/v1777650878/Congressional-Plaza-building-National-Congress-Buenos-Aires_lule9c.jpg',
  TRUE
),
(
  NOW(), NOW(),
  'Descuento 2x1 en excursiones seleccionadas',
  'Por tiempo limitado, hay descuento 2x1 en algunas actividades de fin de semana.',
  'Aprovecha el descuento 2x1 en excursiones seleccionadas y disfruta mas experiencias con tu grupo, familia o amigos, por tiempo limitado.',
  'https://res.cloudinary.com/dj4j7srsg/image/upload/v1777653697/2x1-red-half-price-commercial-tag-isolated_123447-1009_nkvas1.jpg',
  FALSE
),
(
  NOW(), NOW(),
  'Nuevos destinos incorporados',
  'Sumamos nuevas rutas y lugares para seguir explorando mas experiencias.',
  'Incorporamos nuevos destinos a nuestro catalogo, con propuestas para quienes buscan paisajes, cultura local y actividades guiadas en lugares imperdibles.',
  'https://res.cloudinary.com/dj4j7srsg/image/upload/v1777653732/nyc_shutterstock_1810675303_hq4tcb.webp',
  TRUE
);
-- Schedules (future dates to ensure catalog has available data)
INSERT INTO activity_schedules (
  created_at, updated_at, activity_id, start_date_time, end_date_time,
  price, total_spots, reserved_spots
)
SELECT NOW(), NOW(), a.id,
       NOW() + INTERVAL '2 days',
       NOW() + INTERVAL '2 days 2 hours',
       CASE WHEN a.name = 'Free Tour Centro Historico' THEN 0 ELSE a.base_price END,
       30,
       5
FROM activities a;

INSERT INTO activity_schedules (
  created_at, updated_at, activity_id, start_date_time, end_date_time,
  price, total_spots, reserved_spots
)
SELECT NOW(), NOW(), a.id,
       NOW() + INTERVAL '7 days',
       NOW() + INTERVAL '7 days 3 hours',
       CASE WHEN a.name = 'Free Tour Centro Historico' THEN 0 ELSE a.base_price END,
       25,
       3
FROM activities a;

-- Activity images
INSERT INTO activity_images (
  created_at, updated_at, activity_id, image_url, display_order
)
SELECT NOW(), NOW(), a.id,
       CASE a.name
         WHEN 'Free Tour Centro Historico' THEN 'https://res.cloudinary.com/dj4j7srsg/image/upload/v1777650402/minor_hotels_argentina_buenos_aires_ahy7mc.jpg'
         WHEN 'Excursion Alta Montana' THEN 'https://res.cloudinary.com/dj4j7srsg/image/upload/v1777650596/excursion-privada-alta-montana_2196_202007211340160.Mobile_qbic74.jpg'
         WHEN 'Aventura Kayak en Lago' THEN 'https://res.cloudinary.com/dj4j7srsg/image/upload/v1777650704/MBN-Minnewanka-Boat-Rentals-Kayaking.jpg_dol8aw.jpg'
       END,
       1
FROM activities a
WHERE a.name IN ('Free Tour Centro Historico', 'Excursion Alta Montana', 'Aventura Kayak en Lago');

INSERT INTO activity_images (
  created_at, updated_at, activity_id, image_url, display_order
)
SELECT NOW(), NOW(), a.id,
       CASE a.name
         WHEN 'Free Tour Centro Historico' THEN 'https://res.cloudinary.com/dj4j7srsg/image/upload/v1777650480/centro-buenos-aires_ah0bac.jpg'
         WHEN 'Excursion Alta Montana' THEN 'https://res.cloudinary.com/dj4j7srsg/image/upload/v1777650597/68_k4tiu8.jpg'
         WHEN 'Aventura Kayak en Lago' THEN 'https://res.cloudinary.com/dj4j7srsg/image/upload/v1777650666/49_p6v3zs.jpg'
       END,
       2
FROM activities a
WHERE a.name IN ('Free Tour Centro Historico', 'Excursion Alta Montana', 'Aventura Kayak en Lago');

-- Preferences (for featured endpoint)
INSERT INTO user_preferences (
  created_at, updated_at, user_id, preferred_category, preferred_destination_id
)
SELECT NOW(), NOW(), u.id, 'CULTURA', d.id
FROM users u
JOIN destinations d ON d.name = 'Buenos Aires Centro'
WHERE u.email = 'traveler1@xplorenow.test';

INSERT INTO user_preferences (
  created_at, updated_at, user_id, preferred_category, preferred_destination_id
)
SELECT NOW(), NOW(), u.id, 'AVENTURA', d.id
FROM users u
JOIN destinations d ON d.name = 'Bariloche Lagos'
WHERE u.email = 'traveler2@xplorenow.test';

-- Reservation samples
INSERT INTO reservations (
  created_at, updated_at, user_id, schedule_id, seats, total_amount, status, voucher_code, cancelled_at
)
SELECT NOW(), NOW(), u.id, s.id, 2, s.price * 2, 'CONFIRMED', 'XPLR-SEED001', NULL
FROM users u
JOIN activity_schedules s ON TRUE
JOIN activities a ON a.id = s.activity_id
WHERE u.email = 'traveler1@xplorenow.test'
  AND a.name = 'Excursion Alta Montana'
ORDER BY s.start_date_time
LIMIT 1;

INSERT INTO reservations (
  created_at, updated_at, user_id, schedule_id, seats, total_amount, status, voucher_code, cancelled_at
)
SELECT NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days', u.id, s.id, 1, s.price, 'CONFIRMED', 'XPLR-SEED002', NULL
FROM users u
JOIN activity_schedules s ON TRUE
JOIN activities a ON a.id = s.activity_id
WHERE u.email = 'traveler2@xplorenow.test'
  AND a.name = 'Aventura Kayak en Lago'
ORDER BY s.start_date_time
LIMIT 1;

INSERT INTO reservations (
  created_at, updated_at, user_id, schedule_id, seats, total_amount, status, voucher_code, cancelled_at
)
SELECT NOW(), NOW(), u.id, s.id, 1, s.price, 'CONFIRMED', 'XPLR-SEED003', NULL
FROM users u
JOIN activity_schedules s ON TRUE
JOIN activities a ON a.id = s.activity_id
WHERE u.email = 'traveler1@xplorenow.test'
  AND a.name = 'Aventura Kayak en Lago'
  AND s.start_date_time > NOW()
ORDER BY s.start_date_time
LIMIT 1;

-- Travel preferences (profile endpoint)
INSERT INTO user_preferences (
  created_at, updated_at, user_id, preferred_category, preferred_destination_id, travel_preference_type
)
SELECT NOW(), NOW(), u.id, NULL, NULL, 'ADVENTURE'
FROM users u
WHERE u.email = 'traveler1@xplorenow.test';

INSERT INTO user_preferences (
  created_at, updated_at, user_id, preferred_category, preferred_destination_id, travel_preference_type
)
SELECT NOW(), NOW(), u.id, NULL, NULL, 'CULTURE'
FROM users u
WHERE u.email = 'traveler1@xplorenow.test';

-- Rating sample
INSERT INTO ratings (
  created_at, updated_at, user_id, reservation_id, activity_stars, guide_stars, comment
)
SELECT NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', u.id, r.id, 5, 4, 'Excelente experiencia de prueba'
FROM users u
JOIN reservations r ON r.user_id = u.id
WHERE u.email = 'traveler2@xplorenow.test'
  AND r.voucher_code = 'XPLR-SEED002';

-- Reservation events (future sync support)
INSERT INTO reservation_events (
  created_at, updated_at, reservation_id, change_type, changed_at, detail
)
SELECT NOW(), NOW(), r.id, 'CONFIRMED', NOW(), 'Reserva confirmada por seed'
FROM reservations r
WHERE r.voucher_code = 'XPLR-SEED001';

INSERT INTO reservation_events (
  created_at, updated_at, reservation_id, change_type, changed_at, detail
)
SELECT NOW(), NOW(), r.id, 'CONFIRMED', NOW() - INTERVAL '6 days', 'Reserva completada en seed'
FROM reservations r
WHERE r.voucher_code = 'XPLR-SEED002';

INSERT INTO reservation_events (
  created_at, updated_at, reservation_id, change_type, changed_at, detail
)
SELECT NOW(), NOW(), r.id, 'CONFIRMED', NOW(), 'Reserva extra para pruebas de cancelacion'
FROM reservations r
WHERE r.voucher_code = 'XPLR-SEED003';

-- Activity Itineraries (Puntos del recorrido)
-- Free Tour Centro Historico de Buenos Aires
INSERT INTO activity_itinerary (
  created_at, updated_at, activity_id, name, latitude, longitude, order_index
)
SELECT NOW(), NOW(), a.id, 'Inicio: Plaza de Mayo', -34.604722, -58.371111, 1
FROM activities a
WHERE a.name = 'Free Tour Centro Historico';

INSERT INTO activity_itinerary (
  created_at, updated_at, activity_id, name, latitude, longitude, order_index
)
SELECT NOW(), NOW(), a.id, 'Casa Rosada', -34.605556, -58.371389, 2
FROM activities a
WHERE a.name = 'Free Tour Centro Historico';

INSERT INTO activity_itinerary (
  created_at, updated_at, activity_id, name, latitude, longitude, order_index
)
SELECT NOW(), NOW(), a.id, 'Catedral Metropolitana', -34.605556, -58.371389, 3
FROM activities a
WHERE a.name = 'Free Tour Centro Historico';

INSERT INTO activity_itinerary (
  created_at, updated_at, activity_id, name, latitude, longitude, order_index
)
SELECT NOW(), NOW(), a.id, 'Teatro Colón', -34.601667, -58.385556, 4
FROM activities a
WHERE a.name = 'Free Tour Centro Historico';

INSERT INTO activity_itinerary (
  created_at, updated_at, activity_id, name, latitude, longitude, order_index
)
SELECT NOW(), NOW(), a.id, 'Obelisco de Buenos Aires', -34.603722, -58.381592, 5
FROM activities a
WHERE a.name = 'Free Tour Centro Historico';

INSERT INTO activity_itinerary (
  created_at, updated_at, activity_id, name, latitude, longitude, order_index
)
SELECT NOW(), NOW(), a.id, 'Café Tortoni', -34.602500, -58.374722, 6
FROM activities a
WHERE a.name = 'Free Tour Centro Historico';

-- Excursion Alta Montana en Mendoza
INSERT INTO activity_itinerary (
  created_at, updated_at, activity_id, name, latitude, longitude, order_index
)
SELECT NOW(), NOW(), a.id, 'Salida: Terminal de Omnibus', -32.888889, -68.845556, 1
FROM activities a
WHERE a.name = 'Excursion Alta Montana';

INSERT INTO activity_itinerary (
  created_at, updated_at, activity_id, name, latitude, longitude, order_index
)
SELECT NOW(), NOW(), a.id, 'Mirador del Aconcagua', -32.646667, -68.908333, 2
FROM activities a
WHERE a.name = 'Excursion Alta Montana';

INSERT INTO activity_itinerary (
  created_at, updated_at, activity_id, name, latitude, longitude, order_index
)
SELECT NOW(), NOW(), a.id, 'Puente del Inca', -32.636111, -68.921389, 3
FROM activities a
WHERE a.name = 'Excursion Alta Montana';

INSERT INTO activity_itinerary (
  created_at, updated_at, activity_id, name, latitude, longitude, order_index
)
SELECT NOW(), NOW(), a.id, 'Laguna de los Horcones', -32.758333, -68.908333, 4
FROM activities a
WHERE a.name = 'Excursion Alta Montana';

INSERT INTO activity_itinerary (
  created_at, updated_at, activity_id, name, latitude, longitude, order_index
)
SELECT NOW(), NOW(), a.id, 'Regreso a Terminal', -32.888889, -68.845556, 5
FROM activities a
WHERE a.name = 'Excursion Alta Montana';

-- Aventura Kayak en Lago Nahuel Huapi
INSERT INTO activity_itinerary (
  created_at, updated_at, activity_id, name, latitude, longitude, order_index
)
SELECT NOW(), NOW(), a.id, 'Punto de Encuentro: Puerto San Carlos', -41.145556, -71.308889, 1
FROM activities a
WHERE a.name = 'Aventura Kayak en Lago';

INSERT INTO activity_itinerary (
  created_at, updated_at, activity_id, name, latitude, longitude, order_index
)
SELECT NOW(), NOW(), a.id, 'Zona de Kayak - Bahia Lopez', -41.155556, -71.298889, 2
FROM activities a
WHERE a.name = 'Aventura Kayak en Lago';

INSERT INTO activity_itinerary (
  created_at, updated_at, activity_id, name, latitude, longitude, order_index
)
SELECT NOW(), NOW(), a.id, 'Descanso Isla Escondida', -41.165556, -71.288889, 3
FROM activities a
WHERE a.name = 'Aventura Kayak en Lago';

INSERT INTO activity_itinerary (
  created_at, updated_at, activity_id, name, latitude, longitude, order_index
)
SELECT NOW(), NOW(), a.id, 'Regreso a Puerto', -41.145556, -71.308889, 4
FROM activities a
WHERE a.name = 'Aventura Kayak en Lago';

COMMIT;
