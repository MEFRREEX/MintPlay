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
}
