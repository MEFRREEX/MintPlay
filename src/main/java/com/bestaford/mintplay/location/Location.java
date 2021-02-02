package com.bestaford.mintplay.location;

import cn.nukkit.level.Level;
import cn.nukkit.utils.ConfigSection;

import java.util.HashMap;
import java.util.Map;

public class Location {

    public String name;
    public Spawn spawn;
    public HashMap<String, Portal> portals;

    public Location(ConfigSection locationSection, Level level) {
        this.name = locationSection.getString("name");
        this.spawn = new Spawn(locationSection.getSection("spawn"), level);
        this.portals = new HashMap<>();
        for(Map.Entry<String, Object> portalEntry : locationSection.getSections("portals").entrySet()) {
            String portalName = portalEntry.getKey();
            ConfigSection portalSection = (ConfigSection) portalEntry.getValue();
            portals.put(portalName, new Portal(portalSection));
        }
    }

    public String getName() {
        return name;
    }

    public Spawn getSpawn() {
        return spawn;
    }

    public HashMap<String, Portal> getPortals() {
        return portals;
    }
}
