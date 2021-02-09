package com.bestaford.mintplay;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.Level;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.plugin.PluginBase;
import com.bestaford.mintplay.location.Location;
import com.bestaford.mintplay.location.Spawn;
import com.bestaford.mintplay.utils.Database;
import com.bestaford.mintplay.utils.Model;

import javax.imageio.ImageIO;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MintPlay extends PluginBase {

    public Database database;
    public Authorization authorization;
    public Locations locations;
    public Scoreboards scoreboards;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        database = new Database(this, "players");
        authorization = new Authorization(this);
        locations = new Locations(this);
        scoreboards = new Scoreboards(this);
        getServer().getPluginManager().registerEvents(authorization, this);
        getServer().getPluginManager().registerEvents(locations, this);
        getServer().getPluginManager().registerEvents(scoreboards, this);
        //TODO: compile ScoreboardAPI plugin for 1.0.11 nukkit api

        try {
            Entity.registerEntity("Model", Model.class);
        Location location = locations.getLocation("town");
        remove(location);
        Spawn spawn = location.getSpawn();
        String name = "coin-model";
        Path path = getDataFolder().toPath();
        Path skinPath = path.resolve("model-texture.png");
        Path geometryPath = path.resolve("model-geometry.json");
        Skin skin = Model.createSkin(name, skinPath, geometryPath);
            CompoundTag nbt = Entity.getDefaultNBT(spawn);
            CompoundTag skinTag = new CompoundTag()
                    .putByteArray("Data", skin.getSkinData().data)
                    .putInt("SkinImageWidth", skin.getSkinData().width)
                    .putInt("SkinImageHeight", skin.getSkinData().height)
                    .putString("ModelId", skin.getSkinId())
                    .putByteArray("SkinResourcePatch", skin.getSkinResourcePatch().getBytes(StandardCharsets.UTF_8))
                    .putByteArray("GeometryData", skin.getGeometryData().getBytes(StandardCharsets.UTF_8))
                    .putBoolean("IsTrustedSkin", true);
            nbt.putCompound("Skin", skinTag);
            Model model = new Model(spawn.getChunk(), nbt);
    } catch (Exception exception) {
        getLogger().error(exception.getMessage());
    }
    }

    public String replaceAll(String text, Player player) {
        text = text.replaceAll("%server", getConfig().getString("server"));
        text = text.replaceAll("%location", locations.getLocation(player).getName());
        text = text.replaceAll("%online", String.valueOf(getServer().getOnlinePlayers().size()));
        text = text.replaceAll("%max", String.valueOf(getServer().getMaxPlayers()));
        text = text.replaceAll("%name", player.getName());
        text = text.replaceAll("%time", new SimpleDateFormat("HH:mm").format(new Date()));
        text = text.replaceAll("%date", new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
        text = text.replaceAll("%tps", getServer().getTicksPerSecond() + " (" + getServer().getTickUsage() + ")");
        text = text.replaceAll("%avg", getServer().getTicksPerSecondAverage() + " (" + getServer().getTickUsageAverage() + ")");
        return text;
    }

    @Override
    public void onDisable() {
        remove(locations.getLocation("town"));
    }

    public void remove(Location location) {
        Level level = location.getLevel();
        for(Entity entity : level.getEntities()) {
            if(entity instanceof EntityHuman) {
                level.removeEntity(entity);
                level.save();
            }
        }
    }
}
