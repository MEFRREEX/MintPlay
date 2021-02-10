package com.bestaford.mintplay;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.nbt.tag.CompoundTag;
import com.bestaford.mintplay.utils.Model;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Models implements Listener {

    public MintPlay plugin;

    public Models(MintPlay plugin) {
        this.plugin = plugin;
        Entity.registerEntity("Model", Model.class);
    }

    public void clean() {
        for(Level level : plugin.getServer().getLevels().values()) {
            for(Entity entity : level.getEntities()) {
                if(entity instanceof Model) {
                    level.removeEntity(entity);
                }
            }
            level.save();
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageEvent event) {
        event.setCancelled(event.getEntity() instanceof Model);
    }

    public static Skin createSkin(String name, Path skinPath, Path geometryPath) throws IOException {
        Skin skin = new Skin();
        skin.setSkinData(ImageIO.read(skinPath.toFile()));
        skin.setGeometryName("geometry." + name);
        skin.setGeometryData(new String(Files.readAllBytes(geometryPath)));
        return skin;
    }

    public static Model createModel(Position position, Skin skin) {
        CompoundTag nbt = Entity.getDefaultNBT(position);
        CompoundTag skinTag = new CompoundTag()
                .putByteArray("Data", skin.getSkinData().data)
                .putInt("SkinImageWidth", skin.getSkinData().width)
                .putInt("SkinImageHeight", skin.getSkinData().height)
                .putString("ModelId", skin.getSkinId())
                .putByteArray("SkinResourcePatch", skin.getSkinResourcePatch().getBytes(StandardCharsets.UTF_8))
                .putByteArray("GeometryData", skin.getGeometryData().getBytes(StandardCharsets.UTF_8))
                .putBoolean("IsTrustedSkin", true);
        nbt.putCompound("Skin", skinTag);
        return new Model(position.getChunk(), nbt);
    }
}