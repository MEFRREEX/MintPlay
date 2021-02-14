package com.bestaford.mintplay.module;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBurnEvent;
import cn.nukkit.event.block.BlockIgniteEvent;
import cn.nukkit.event.block.LeavesDecayEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.particle.FloatingTextParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import com.bestaford.mintplay.MintPlay;
import com.bestaford.mintplay.location.*;

import java.util.HashMap;
import java.util.Map;

public class Locations implements Listener {

    public MintPlay plugin;
    public HashMap<String, Location> locations = new HashMap<>();
    public HashMap<Vector3, FloatingTextParticle> particles = new HashMap<>();

    public Locations(MintPlay plugin) {
        this.plugin = plugin;
        loadLocations();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Location location = getLocation(player);
        location.onPlayerJoin(player);
        updateFloatingText(location);
        //TODO: fix spawn under location when quit on slab
        //TODO: teleport to location on join
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Location location = getLocation(player);
        location.onPlayerQuit(player);
        updateFloatingText(location);
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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onLeavesDecay(LeavesDecayEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockIgnite(BlockIgniteEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    public void loadLocations() {
        for(Map.Entry<String, Object> locationEntry : plugin.getConfig().getSections("locations").entrySet()) {
            String locationName = locationEntry.getKey();
            ConfigSection locationSection = (ConfigSection) locationEntry.getValue();
            if(plugin.getServer().loadLevel(locationName)) {
                Level level = plugin.getServer().getLevelByName(locationName);
                level.setRaining(false);
                level.setThundering(false);
                level.setTime(0);
                //TODO: sync ingame time with real server time
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

    public Location getLocation(Spawn spawn) {
        return getLocation(spawn.getLevel());
    }

    public Location getLocation(Player player) {
        return getLocation(player.getLevel());
    }

    public HashMap<String, Location> getLocations() {
        return locations;
    }

    public void teleport(Player player, Spawn spawn) {
        Location oldLocation = getLocation(player);
        oldLocation.onPlayerQuit(player);
        updateFloatingText(oldLocation);
        player.teleport(spawn);
        Location newLocation = getLocation(spawn);
        newLocation.onPlayerJoin(player);
        updateFloatingText(newLocation);
        plugin.scoreboards.updateTag(player, "location", newLocation.getName());
        player.setImmobile(true);
        player.addEffect(Effect.getEffect(Effect.BLINDNESS).setDuration(40));
        plugin.getServer().getScheduler().scheduleDelayedTask(plugin, () -> {
            player.setImmobile(false);
            player.sendTitle(TextFormat.BOLD.toString() + TextFormat.YELLOW.toString() + newLocation.getName(), "", 20, 40, 20);
        }, 40);
    }

    public void updateFloatingText(Location location) {
        for(Player player : location.getPlayers()) {
            addFloatingText(player, location);
        }
        for(String portalName : location.getPortals().keySet()) {
            Location targetLocation = getLocation(portalName);
            for(Player player : targetLocation.getPlayers()) {
                addFloatingText(player, targetLocation);
            }
        }
    }

    public void addFloatingText(Player player, Location location) {
        for(Map.Entry<String, Portal> portalEntry : location.getPortals().entrySet()) {
            String portalName = portalEntry.getKey();
            Portal portal = portalEntry.getValue();
            Vector3 position = portal.getFloatingTextPosition();
            Location targetLocation = getLocation(portalName);
            String title = TextFormat.RED + "> " + TextFormat.YELLOW + targetLocation.getName() + TextFormat.RED + " <";
            String text = TextFormat.RED + "> " + TextFormat.YELLOW + "Игроков: " + targetLocation.getPlayers().size() + TextFormat.RED + " <";
            FloatingTextParticle particle;
            if(particles.containsKey(position)) {
                particle = particles.get(position);
                particle.setTitle(title);
                particle.setText(text);
            } else {
                particle = new FloatingTextParticle(position, title, text);
                particles.put(position, particle);
            }
            location.getLevel().addParticle(particle, player);
        }
    }
}