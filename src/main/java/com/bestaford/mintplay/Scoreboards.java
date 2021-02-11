package com.bestaford.mintplay;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerLocallyInitializedEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.utils.ConfigSection;
import de.lucgameshd.scoreboard.api.ScoreboardAPI;
import de.lucgameshd.scoreboard.network.DisplaySlot;
import de.lucgameshd.scoreboard.network.Scoreboard;
import de.lucgameshd.scoreboard.network.ScoreboardDisplay;
import de.lucgameshd.scoreboard.network.SortOrder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scoreboards implements Listener {

    public MintPlay plugin;
    public HashMap<Player, Scoreboard> scoreboards = new HashMap<>();
    public HashMap<Player, HashMap<String, String>> tags = new HashMap<>();

    public Scoreboards(MintPlay plugin) {
        this.plugin = plugin;
        plugin.getServer().getScheduler().scheduleRepeatingTask(plugin, () -> {
            updateTag("time", getTime());
            updateTag("date", getDate());
            updateTag("tps", getTPS());
            updateTag("avg", getAVG());
        }, 20);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        updateTag("online", getOnline());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLocallyInitialized(PlayerLocallyInitializedEvent event) {
        Player player = event.getPlayer();
        tags.put(player, createTags(player));
        Scoreboard scoreboard = createScoreboard(player);
        scoreboard.showFor(player);
        scoreboards.put(player, scoreboard);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        scoreboards.remove(player);
        tags.remove(player);
        updateTag("online", getOnline());
    }

    public HashMap<String, String> createTags(Player player) {
        HashMap<String, String> tags = new HashMap<>();
        tags.put("name", player.getName());
        tags.put("location", plugin.locations.getLocation(player).getName());
        tags.put("online", getOnline());
        tags.put("max", getMax());
        tags.put("time", getTime());
        tags.put("date", getDate());
        tags.put("tps", getTPS());
        tags.put("avg", getAVG());
        return tags;
    }

    public void updateTag(String tag, String value) {
        for(Player player : plugin.getServer().getOnlinePlayers().values()) {
            updateTag(player, tag, value);
        }
    }

    public void updateTag(Player player, String tag, String value) {
        if(tags.containsKey(player)) {
            tags.get(player).put(tag, value);
            updateScoreboard(player);
        }
    }

    public Scoreboard createScoreboard(Player player) {
        ConfigSection scoreboardSection = plugin.getConfig().getSection("scoreboard");
        String title = plugin.replaceAll(scoreboardSection.getString("title"), player);
        List<String> lines = scoreboardSection.getStringList("lines");
        Scoreboard scoreboard = ScoreboardAPI.createScoreboard();
        ScoreboardDisplay scoreboardDisplay = scoreboard.addDisplay(DisplaySlot.SIDEBAR, title, title, SortOrder.ASCENDING);
        for(int line = 0; line < lines.size(); line++) {
            String text = lines.get(line);
            for(Map.Entry<String, String> tagEntry : tags.get(player).entrySet()) {
                text = text.replaceAll("%" + tagEntry.getKey(), tagEntry.getValue());
            }
            scoreboardDisplay.addLine(text + " ", line + 1);
        }
        return scoreboard;
    }

    public void updateScoreboard(Player player) {
        if(scoreboards.containsKey(player)) {
            scoreboards.get(player).hideFor(player);
            Scoreboard scoreboard = createScoreboard(player);
            scoreboard.showFor(player);
            scoreboards.put(player, scoreboard);
        }
    }

    public String getOnline() {
        return String.valueOf(plugin.getServer().getOnlinePlayers().size());
    }

    public String getMax() {
        return String.valueOf(plugin.getServer().getMaxPlayers());
    }

    public String getTime() {
        return new SimpleDateFormat("HH:mm").format(new Date());
    }

    public String getDate() {
        return new SimpleDateFormat("dd.MM.yyyy").format(new Date());
    }

    public String getTPS() {
        return plugin.getServer().getTicksPerSecond() + " (" + plugin.getServer().getTickUsage() + ")";
    }

    public String getAVG() {
        return plugin.getServer().getTicksPerSecondAverage() + " (" + plugin.getServer().getTickUsageAverage() + ")";
    }
}