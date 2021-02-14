package com.bestaford.mintplay.location;

import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.ConfigSection;

public class Portal {

    private final AxisAlignedBB boundingBox;
    private final Vector3 floatingTextPosition;
    private final Spawn spawn;

    public Portal(ConfigSection portalSection) {
        ConfigSection boundingBoxSection = portalSection.getSection("boundingBox");
        this.boundingBox = new SimpleAxisAlignedBB(boundingBoxSection.getDouble("minX"), boundingBoxSection.getDouble("minY"), boundingBoxSection.getDouble("minZ"), boundingBoxSection.getDouble("maxX"), boundingBoxSection.getDouble("maxY"), boundingBoxSection.getDouble("maxZ"));
        ConfigSection floatingTextSection = portalSection.getSection("floatingText");
        this.floatingTextPosition = new Vector3(floatingTextSection.getDouble("x"), floatingTextSection.getDouble("y"), floatingTextSection.getDouble("z"));
        this.spawn = new Spawn(portalSection.getSection("spawn"));
    }

    public AxisAlignedBB getBoundingBox() {
        return boundingBox;
    }

    public Vector3 getFloatingTextPosition() {
        return floatingTextPosition;
    }

    public Spawn getSpawn() {
        return spawn;
    }
}
