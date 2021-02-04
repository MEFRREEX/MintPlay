package com.bestaford.mintplay;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerLocallyInitializedEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.particle.FloatingTextParticle;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import com.bestaford.mintplay.location.*;

import java.util.HashMap;
import java.util.Map;

public class Locations implements Listener {

    public MintPlay plugin;
    public HashMap<String, Location> locations = new HashMap<>();
    public int loadingTime = 40;

    public Locations(MintPlay plugin) {
        this.plugin = plugin;
        loadLocations();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLocallyInitialized(PlayerLocallyInitializedEvent event) {
        Player player = event.getPlayer();
        addFloatingText(player, getLocation(player));
        //TODO: fix slab quit/join bug, when player spawns under location
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location location = getLocation(player);
        for(Map.Entry<String, Portal> portalEntry : location.getPortals().entrySet()) {
            String portalName = portalEntry.getKey();
            Portal portal = portalEntry.getValue();
            if(portal.getBoundingBox().intersectsWith(player.getBoundingBox())) {
                Spawn spawn = portal.getSpawn();
                spawn.setLevel(plugin.getServer().getLevelByName(portalName));
                teleport(player, spawn);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();
        if(item.getId() == Item.STICK) {
            Block block = event.getBlock();
            player.sendMessage("x: " + block.getX() + " y: " + block.getY() + " z: " + block.getZ() + " id: " + block.getId() + " damage: " + block.getDamage() + " yaw: " + player.getYaw());
        }
    }

    public void loadLocations() {
        for(Map.Entry<String, Object> locationEntry : plugin.getConfig().getSections("locations").entrySet()) {
            String locationName = locationEntry.getKey();
            ConfigSection locationSection = (ConfigSection) locationEntry.getValue();
            if(plugin.getServer().loadLevel(locationName)) {
                Level level = plugin.getServer().getLevelByName(locationName);
                locations.put(locationName, new Location(locationSection, level));
            }
        }
    }

    public Location getLocation(String name) {
        return locations.get(name);
    }

    public Location getLocation(Level level) {
        return getLocation(level.getName());
    }

    public Location getLocation(Player player) {
        return getLocation(player.getLevel());
    }

    public void teleport(Player player, Spawn spawn) {
        player.setImmobile(true);
        player.addEffect(Effect.getEffect(Effect.BLINDNESS).setDuration(loadingTime));
        player.teleport(spawn);
        addFloatingText(player, getLocation(spawn.getLevel()));
        plugin.getServer().getScheduler().scheduleDelayedTask(plugin, () -> {
            player.setImmobile(false);
            player.sendTitle(TextFormat.BOLD.toString() + TextFormat.YELLOW.toString() + getLocation(player).getName(), "", 20, loadingTime, 20);
        }, loadingTime);
    }

    public void addFloatingText(Player player, Location location) {
        for(Map.Entry<String, Portal> portalEntry : location.getPortals().entrySet()) {
            String portalName = portalEntry.getKey();
            Portal portal = portalEntry.getValue();
            Location targetLocation = getLocation(portalName);
            location.getLevel().addParticle(new FloatingTextParticle(portal.getFloatingTextPosition(), TextFormat.YELLOW + targetLocation.getName(), TextFormat.YELLOW + "Игроков: " + targetLocation.getLevel().getPlayers().size()), player);
        }
    }
}
