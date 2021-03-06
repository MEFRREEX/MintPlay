package com.bestaford.mintplay.location;

import cn.nukkit.Player;
import cn.nukkit.level.Level;
import cn.nukkit.utils.ConfigSection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Location {

    private final String name;
    private final boolean restricted;
    private final Level level;
    private final Spawn spawn;
    private final HashMap<String, Portal> portals = new HashMap<>();
    private final HashMap<UUID, Player> players = new HashMap<>();

    public Location(ConfigSection locationSection, Level level) {
        this.name = locationSection.getString("name");
        this.restricted = locationSection.getBoolean("restricted");
        this.level = level;
        this.spawn = new Spawn(locationSection.getSection("spawn"), level);
        for(Map.Entry<String, Object> portalEntry : locationSection.getSections("portals").entrySet()) {
            String portalName = portalEntry.getKey();
            ConfigSection portalSection = (ConfigSection) portalEntry.getValue();
            portals.put(portalName, new Portal(portalSection));
        }
        //TODO: restrict world editing
    }

    public void onPlayerJoin(Player player) {
        players.put(player.getUniqueId(), player);
    }

    public void onPlayerQuit(Player player) {
        players.remove(player.getUniqueId());
    }

    public String getName() {
        return name;
    }

    public boolean isRestricted() {
        return restricted;
    }

    public Level getLevel() {
        return level;
    }

    public Spawn getSpawn() {
        return spawn;
    }

    public HashMap<String, Portal> getPortals() {
        return portals;
    }

    public Collection<Player> getPlayers() {
        return players.values();
    }
}