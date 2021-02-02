package com.bestaford.mintplay;

import cn.nukkit.plugin.PluginBase;

public class MintPlay extends PluginBase {

    public Database database;
    public Authorization authorization;
    public Locations locations;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        database = new Database(this, "players");
        authorization = new Authorization(this);
        locations = new Locations(this);
        getServer().getPluginManager().registerEvents(authorization, this);
        getServer().getPluginManager().registerEvents(locations, this);
    }
}
