package com.bestaford.mintplay.module;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.*;
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
import com.bestaford.mintplay.util.PlayerData;
import com.google.gson.Gson;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class Locations implements Listener {

    private final MintPlay plugin;
    private final HashMap<String, Location> locations = new HashMap<>();
    private final HashMap<Vector3, FloatingTextParticle> particles = new HashMap<>();
    private final int loadingTime = 40;

    public Locations(MintPlay plugin) {
        this.plugin = plugin;
        plugin.database.createTable("blocks");
        loadLocations();
        //TODO: move Location, Portal and Spawn into Locations class
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Location location = getLocation(player);
        location.onPlayerJoin(player);
        updateFloatingText(location);
        PlayerData playerData = plugin.authorization.getPlayerData(player);
        if(playerData.isRegistered()) {
            player.teleport(new Vector3(playerData.getX(), playerData.getY(), playerData.getZ()));
        }
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
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if(isBlockExist(block)) {
            event.setCancelled(true);
        } else {
            saveBlockData(block, player);
        }
        //TODO: restricted locations
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if(isBlockExist(block)) {
            removeBlockData(block);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onLeavesDecay(LeavesDecayEvent event) {
        event.setCancelled(true);
        //TODO: check block in db
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockIgnite(BlockIgniteEvent event) {
        event.setCancelled(true);
        //TODO: check block in db
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
        //TODO: check block in db
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

    public Location getLocation(cn.nukkit.level.Location location) {
        return getLocation(location.getLevel());
    }

    public Location getLocation(Player player) {
        return getLocation(player.getLevel());
    }

    public HashMap<String, Location> getLocations() {
        return locations;
    }

    public void teleport(Player player, cn.nukkit.level.Location target) {
        Location oldLocation = getLocation(player);
        Location newLocation = getLocation(target);
        player.teleport(target);
        if(!oldLocation.equals(newLocation)) {
            oldLocation.onPlayerQuit(player);
            updateFloatingText(oldLocation);
            newLocation.onPlayerJoin(player);
            updateFloatingText(newLocation);
            plugin.scoreboards.updateTag(player, "location", newLocation.getName());
        }
        player.setImmobile(true);
        player.addEffect(Effect.getEffect(Effect.BLINDNESS).setDuration(loadingTime));
        plugin.getServer().getScheduler().scheduleDelayedTask(plugin, () -> {
            player.setImmobile(false);
            player.sendTitle(TextFormat.BOLD.toString() + TextFormat.YELLOW.toString() + newLocation.getName(), "", loadingTime / 2, loadingTime, loadingTime / 2);
        }, loadingTime);
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

    public String toJson(Block block) {
        HashMap<String, String> data = new HashMap<>();
        data.put("x", String.valueOf(block.getFloorX()));
        data.put("y", String.valueOf(block.getFloorY()));
        data.put("z", String.valueOf(block.getFloorZ()));
        data.put("level", block.getLevel().getName());
        return new Gson().toJson(data);

    }

    public boolean isBlockExist(Block block) {
        try {
            PreparedStatement preparedStatement = plugin.database.connection.prepareStatement("SELECT * FROM blocks WHERE position = ?");
            preparedStatement.setString(1, toJson(block));
            return preparedStatement.executeQuery().next();
        } catch (Exception exception) {
            plugin.getLogger().error(exception.getMessage());
        }
        return false;
    }

    public void saveBlockData(Block block, Player player) {
        try {
            PreparedStatement preparedStatement = plugin.database.connection.prepareStatement("INSERT INTO blocks (position, player) VALUES (?, ?)");
            preparedStatement.setString(1, toJson(block));
            preparedStatement.setString(2, player.getName());
            preparedStatement.execute();
        } catch (Exception exception) {
            plugin.getLogger().error(exception.getMessage());
        }
    }

    public void removeBlockData(Block block) {
        try {
            PreparedStatement preparedStatement = plugin.database.connection.prepareStatement("DELETE FROM blocks WHERE position = ?");
            preparedStatement.setString(1, toJson(block));
            preparedStatement.execute();
        } catch (Exception exception) {
            plugin.getLogger().error(exception.getMessage());
        }
    }

    public String getBlockData(Block block) {
        try {
            PreparedStatement preparedStatement = plugin.database.connection.prepareStatement("SELECT player FROM blocks WHERE position = ?");
            preparedStatement.setString(1, toJson(block));
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getString("player");
        } catch (Exception exception) {
            plugin.getLogger().error(exception.getMessage());
        }
        return null;
    }
}