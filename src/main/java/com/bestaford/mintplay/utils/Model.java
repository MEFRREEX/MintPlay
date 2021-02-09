package com.bestaford.mintplay.utils;

import cn.nukkit.entity.data.Skin;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Model {

    public static Skin createSkin(String name, Path skinPath, Path geometryPath) throws IOException {
        Skin skin = new Skin();
        skin.setSkinData(ImageIO.read(skinPath.toFile()));
        skin.setGeometryName("geometry." + name);
        skin.setGeometryData(new String(Files.readAllBytes(geometryPath)));
        return skin;
    }
}