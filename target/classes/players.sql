CREATE TABLE IF NOT EXISTS players (
    name TEXT PRIMARY KEY,
    password TEXT,
    address TEXT,
    uuid TEXT,
    x REAL,
    y REAL,
    z REAL,
    yaw REAL,
    pitch REAL,
    location TEXT,
    level INTEGER,
    experience INTEGER
);