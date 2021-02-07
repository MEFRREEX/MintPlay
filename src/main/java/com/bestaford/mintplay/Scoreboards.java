package com.bestaford.mintplay;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
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

public class Scoreboards implements Listener {

    public MintPlay plugin;
    public HashMap<Player, Scoreboard> scoreboards = new HashMap<>();

    public Scoreboards(MintPlay plugin) {
        this.plugin = plugin;
//        plugin.getServer().getScheduler().scheduleRepeatingTask(plugin, () -> {
//            for(Player player : plugin.getServer().getOnlinePlayers().values()) {
//                updateScoreboard(player);
//            }
//        }, 20);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLocallyInitialized(PlayerLocallyInitializedEvent event) {
        Player player = event.getPlayer();
        Scoreboard scoreboard = createScoreboard(player);
        scoreboard.showFor(player);
        scoreboards.put(player, scoreboard);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        scoreboards.remove(player);
    }

    public Scoreboard createScoreboard(Player player) {
        ConfigSection scoreboardSection = plugin.getConfig().getSection("scoreboard");
        String title = scoreboardSection.getString("title");
        List<String> lines = scoreboardSection.getStringList("lines");
        Scoreboard scoreboard = ScoreboardAPI.createScoreboard();
        ScoreboardDisplay scoreboardDisplay = scoreboard.addDisplay(DisplaySlot.SIDEBAR, title, title, SortOrder.ASCENDING);
        for(int line = 0; line < lines.size(); line++) {
            String text = lines.get(line);
            text.replaceAll("%name%", player.getName());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
            text.replaceAll("%time%", simpleDateFormat.format(new Date()));
            scoreboardDisplay.addLine(text, line);
        }
        return scoreboard;
    }

    public void updateScoreboard(Player player) {
        scoreboards.get(player).hideFor(player);
        Scoreboard scoreboard = createScoreboard(player);
        scoreboard.showFor(player);
        scoreboards.put(player, scoreboard);
    }
}