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

import java.util.HashMap;
import java.util.List;

public class Scoreboards implements Listener {

    public MintPlay plugin;
    public HashMap<Player, Scoreboard> scoreboards = new HashMap<>();

    public Scoreboards(MintPlay plugin) {
        this.plugin = plugin;
        plugin.getServer().getScheduler().scheduleRepeatingTask(plugin, () -> {
            for(Player player : plugin.getServer().getOnlinePlayers().values()) {
                updateScoreboard(player);
            }
        }, 20);
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
        String title = plugin.replaceAll(scoreboardSection.getString("title"), player);
        List<String> lines = scoreboardSection.getStringList("lines");
        Scoreboard scoreboard = ScoreboardAPI.createScoreboard();
        ScoreboardDisplay scoreboardDisplay = scoreboard.addDisplay(DisplaySlot.SIDEBAR, title, title, SortOrder.ASCENDING);
        for(int line = 0; line < lines.size(); line++) {
            scoreboardDisplay.addLine(plugin.replaceAll(lines.get(line), player) + " ", line + 1);
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
}