CREATE TABLE IF NOT EXISTS reset_rankings (
    player_id INT NOT NULL,
    daily_count INT NOT NULL DEFAULT 0,
    monthly_count INT NOT NULL DEFAULT 0,
    PRIMARY KEY (player_id)
);
