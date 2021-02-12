package com.bestaford.mintplay;

import cn.nukkit.Player;
import cn.nukkit.plugin.PluginBase;
import com.bestaford.mintplay.utils.Database;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    }
}