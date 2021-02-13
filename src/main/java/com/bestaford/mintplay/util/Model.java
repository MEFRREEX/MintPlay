package com.bestaford.mintplay.util;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Model extends EntityHuman {

    public Model(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        source.setCancelled(true);
        return false;
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