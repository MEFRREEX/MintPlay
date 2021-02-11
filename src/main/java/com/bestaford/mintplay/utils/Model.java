package com.bestaford.mintplay.utils;

import cn.nukkit.entity.EntityHuman;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

public class Model extends EntityHuman {

    public Model(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        source.setCancelled(true);
        return false;
    }
}