CREATE TABLE IF NOT EXISTS characters (
    name TEXT PRIMARY KEY,
    player TEXT,
    items TEXT,
    health INTEGER,
    mana INTEGER,
    race INTEGER,
    class INTEGER,
    level INTEGER,
    experience INTEGER,
    money INTEGER,
    kills INTEGER,
    deaths INTEGER
);