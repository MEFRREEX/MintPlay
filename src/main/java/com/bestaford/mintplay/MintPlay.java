package com.bestaford.mintplay;

import cn.nukkit.plugin.PluginBase;
import com.bestaford.mintplay.module.*;
import com.bestaford.mintplay.util.Database;

public class MintPlay extends PluginBase {

    public Database database;
    public Authorization authorization;
    public Locations locations;
    public Scoreboards scoreboards;
    public Models models;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        database = new Database(this, "players");
        authorization = new Authorization(this);
        locations = new Locations(this);
        scoreboards = new Scoreboards(this);
        models = new Models(this);
        getServer().getPluginManager().registerEvents(authorization, this);
        getServer().getPluginManager().registerEvents(locations, this);
        getServer().getPluginManager().registerEvents(scoreboards, this);
        //TODO: remove Models module, move static methods to Model util
        //TODO: move welcome text to config
        //TODO: Locations neighbors update in separate method
        //TODO: reduce scoreboard update times
        //TODO: check time and date updates
        //TODO: fix spawn under location when quit on slab
        //TODO: restrict world editing
        //TODO: teleport to location on join
        //TODO: location.getSpawn(Vector3 position) returns spawn with level
        //TODO: location.getSpawn(portal.getSpawn())
    }
}