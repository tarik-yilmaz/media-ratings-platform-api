-- ==========================================================
-- MRP Demo Script (PostgreSQL)
-- Inserts / Updates / Selects / Deletes + Reset
-- ==========================================================

-- Optional: sauberer Start (nur Daten löschen, Tabellen behalten)
-- ACHTUNG: löscht ALLE Daten!
-- TRUNCATE TABLE rating_likes, favorites, ratings, media, users RESTART IDENTITY CASCADE;

-- ----------------------------------------------------------
-- 1) INSERT Demo: User anlegen
-- ----------------------------------------------------------
INSERT INTO users (username, password_hash, email)
VALUES
    ('alice', 'demo_hash_alice', 'alice@example.com'),
    ('bob',   'demo_hash_bob',   'bob@example.com');

-- Check
SELECT id, username, email, created_at FROM users ORDER BY id;

-- ----------------------------------------------------------
-- 2) INSERT Demo: Media anlegen (creator_id verweist auf users.id)
-- ----------------------------------------------------------
INSERT INTO media (title, description, media_type, release_year, genres, age_restriction, creator_id, average_score)
VALUES
    ('Interstellar', 'Space / Sci-Fi', 'MOVIE', 2014, 'Sci-Fi,Drama', 12,
     (SELECT id FROM users WHERE username = 'alice'), 0.00),
    ('Breaking Bad', 'Crime series', 'SERIES', 2008, 'Crime,Drama', 16,
     (SELECT id FROM users WHERE username = 'alice'), 0.00);

-- Check
SELECT id, title, media_type, creator_id, created_at FROM media ORDER BY id;

-- ----------------------------------------------------------
-- 3) INSERT Demo: Rating anlegen
-- (Unique(media_id, user_id) beachten!)
-- ----------------------------------------------------------
INSERT INTO ratings (media_id, user_id, stars, comment, comment_confirmed, likes_count)
VALUES
    (
        (SELECT id FROM media WHERE title = 'Interstellar'),
        (SELECT id FROM users WHERE username = 'bob'),
        5,
        'Sehr stark!',
        TRUE,
        0
    );

-- Check
SELECT id, media_id, user_id, stars, comment, comment_confirmed, likes_count, created_at
FROM ratings
ORDER BY id;

-- ----------------------------------------------------------
-- 4) UPDATE Demo: Rating ändern
-- ----------------------------------------------------------
UPDATE ratings
SET stars = 4,
    comment = 'Nach dem zweiten Mal: immer noch sehr gut.',
    updated_at = CURRENT_TIMESTAMP
WHERE media_id = (SELECT id FROM media WHERE title = 'Interstellar')
  AND user_id = (SELECT id FROM users WHERE username = 'bob');

-- Check
SELECT id, stars, comment, updated_at FROM ratings ORDER BY id;

-- ----------------------------------------------------------
-- 5) UPDATE Demo: average_score im Media korrekt setzen
-- (Simuliert, was dein Code später macht)
-- ----------------------------------------------------------
UPDATE media
SET average_score = (
    SELECT COALESCE(AVG(stars), 0.0)
    FROM ratings
    WHERE media_id = media.id
),
    updated_at = CURRENT_TIMESTAMP;

-- Check
SELECT id, title, average_score, updated_at FROM media ORDER BY id;

-- ----------------------------------------------------------
-- 6) SELECT Demo: Join (Media + Creator + Ratings)
-- ----------------------------------------------------------
SELECT
    m.id AS media_id,
    m.title,
    m.media_type,
    u.username AS creator,
    m.average_score,
    r.stars,
    r.comment
FROM media m
         JOIN users u ON u.id = m.creator_id
         LEFT JOIN ratings r ON r.media_id = m.id
ORDER BY m.id;

-- ----------------------------------------------------------
-- 7) DELETE Demo: Rating löschen
-- ----------------------------------------------------------
DELETE FROM ratings
WHERE media_id = (SELECT id FROM media WHERE title = 'Interstellar')
  AND user_id = (SELECT id FROM users WHERE username = 'bob');

-- Check
SELECT * FROM ratings ORDER BY id;

-- ----------------------------------------------------------
-- 8) Reset (nur Daten löschen, Tabellen bleiben)
-- ACHTUNG: löscht ALLE Daten!
-- ----------------------------------------------------------
TRUNCATE TABLE rating_likes, favorites, ratings, media, users RESTART IDENTITY CASCADE;

-- Check: sollte leer sein
SELECT * FROM users;
SELECT * FROM media;
SELECT * FROM ratings;
