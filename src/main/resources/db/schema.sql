-- Benutzer Tabelle
CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       email VARCHAR(100),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       total_ratings INT DEFAULT 0,
                       average_rating DECIMAL(3,2) DEFAULT 0.00
);

-- Medien Tabelle
CREATE TABLE media (
                       id SERIAL PRIMARY KEY,
                       title VARCHAR(200) NOT NULL,
                       description TEXT,
                       media_type VARCHAR(20) CHECK (media_type IN ('MOVIE', 'SERIES', 'GAME')),
                       release_year INT,
                       genres VARCHAR(200), -- Komma-separierte Liste
                       age_restriction INT,
                       creator_id INT REFERENCES users(id),
                       average_score DECIMAL(3,2) DEFAULT 0.00,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bewertungen Tabelle
CREATE TABLE ratings (
                         id SERIAL PRIMARY KEY,
                         media_id INT REFERENCES media(id) ON DELETE CASCADE,
                         user_id INT REFERENCES users(id) ON DELETE CASCADE,
                         stars INT CHECK (stars BETWEEN 1 AND 5),
                         comment TEXT,
                         confirmed BOOLEAN DEFAULT FALSE,
                         likes_count INT DEFAULT 0,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         UNIQUE(media_id, user_id)
);

-- Favoriten Tabelle
CREATE TABLE favorites (
                           id SERIAL PRIMARY KEY,
                           user_id INT REFERENCES users(id) ON DELETE CASCADE,
                           media_id INT REFERENCES media(id) ON DELETE CASCADE,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           UNIQUE(user_id, media_id)
);

-- Rating Likes Tabelle (wer hat welches Rating geliked)
CREATE TABLE rating_likes (
                              id SERIAL PRIMARY KEY,
                              rating_id INT REFERENCES ratings(id) ON DELETE CASCADE,
                              user_id INT REFERENCES users(id) ON DELETE CASCADE,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              UNIQUE(rating_id, user_id)
);

-- Indizes für Performance
CREATE INDEX idx_media_title ON media(title);
CREATE INDEX idx_media_genres ON media(genres);
CREATE INDEX idx_ratings_user ON ratings(user_id);
CREATE INDEX idx_ratings_media ON ratings(media_id);
CREATE INDEX idx_favorites_user ON favorites(user_id);