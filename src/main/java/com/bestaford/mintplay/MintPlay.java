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
        Location location = locations.getLocation("town");
        remove(location);
        Spawn spawn = location.getSpawn();
        CompoundTag nbt = Entity.getDefaultNBT(spawn);
        Path path = getDataFolder().toPath();
        Skin skin = new Skin();
        skin.setSkinData(ImageIO.read(path.resolve("model-texture.png").toFile()));
        skin.setGeometryName("geometry.coin-model");
        skin.setGeometryData(new String(Files.readAllBytes(path.resolve("model-geometry.json"))));
        skin.setTrusted(true);
        CompoundTag skinTag = new CompoundTag()
                .putByteArray("Data", skin.getSkinData().data)
                .putInt("SkinImageWidth", skin.getSkinData().width)
                .putInt("SkinImageHeight", skin.getSkinData().height)
                .putString("ModelId", skin.getSkinId())
                .putString("CapeId", skin.getCapeId())
                .putByteArray("CapeData", skin.getCapeData().data)
                .putInt("CapeImageWidth", skin.getCapeData().width)
                .putInt("CapeImageHeight", skin.getCapeData().height)
                .putByteArray("SkinResourcePatch", skin.getSkinResourcePatch().getBytes(StandardCharsets.UTF_8))
                .putString("GeometryName", "geometry.coin-model")
                .putByteArray("GeometryData", skin.getGeometryData().getBytes(StandardCharsets.UTF_8))
                .putByteArray("AnimationData", skin.getAnimationData().getBytes(StandardCharsets.UTF_8))
                .putBoolean("PremiumSkin", skin.isPremium())
                .putBoolean("PersonaSkin", skin.isPersona())
                .putBoolean("IsTrustedSkin", skin.isTrusted())
                .putBoolean("CapeOnClassicSkin", skin.isCapeOnClassic());
        nbt.putString("NameTag", "geometry.coin-model 5");
        nbt.putCompound("Skin", skinTag);
        EntityHuman model = new EntityHuman(spawn.getChunk(), nbt);
        model.spawnToAll();
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
