CREATE TABLE IF NOT EXISTS blueprints (
    author VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (author, name)
);

CREATE TABLE IF NOT EXISTS blueprint_points (
    id SERIAL PRIMARY KEY,
    author VARCHAR(100) NOT NULL,
    blueprint_name VARCHAR(100) NOT NULL,
    x INT NOT NULL,
    y INT NOT NULL,
    CONSTRAINT fk_blueprint FOREIGN KEY (author, blueprint_name) 
        REFERENCES blueprints(author, name) ON DELETE CASCADE
);