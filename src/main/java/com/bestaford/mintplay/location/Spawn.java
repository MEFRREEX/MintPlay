package com.bestaford.mintplay.location;

import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.utils.ConfigSection;

public class Spawn extends Location {

    public Spawn(ConfigSection spawnSection) {
        super(spawnSection.getDouble("x") + 0.5, spawnSection.getDouble("y") + 0.5, spawnSection.getDouble("z") + 0.5, spawnSection.getInt("side") * 90, 0);
    }

    public Spawn(ConfigSection spawnSection, Level level) {
        this(spawnSection);
        setLevel(level);
    }
}