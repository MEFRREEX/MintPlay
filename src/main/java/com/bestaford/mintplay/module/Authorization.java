package com.bestaford.mintplay.module;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import com.bestaford.mintplay.MintPlay;
import ru.nukkitx.forms.elements.CustomForm;
import ru.nukkitx.forms.elements.ModalForm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.UUID;

public class Authorization implements Listener {

    public MintPlay plugin;
    public HashMap<String, UUID> players = new HashMap<>();

    public Authorization(MintPlay plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerLocallyInitialized(PlayerLocallyInitializedEvent event) {
        Player player = event.getPlayer();
        player.setImmobile(true);
        if(isRegistered(player)) {
            if(isLogined(player)) {
                welcome(player);
            } else {
                sendLoginForm(player);
            }
        } else {
            sendRegistrationForm(player);
        }
    }

    public void sendRegistrationForm(Player player) {
        sendRegistrationForm(player, false, "");
    }

    public void sendRegistrationForm(Player player, boolean error, String oldPassword) {
        ConfigSection authorizationSection = plugin.getConfig().getSection("authorization");
        ConfigSection inputSection = authorizationSection.getSection("input");
        ConfigSection registrationSection = authorizationSection.getSection("registration");
        CustomForm form = new CustomForm();
        form.setTitle(registrationSection.getString("title"));
        form.addLabel(registrationSection.getString("label").replaceAll("%server", plugin.getConfig().getString("server")));
        if(error) {
            form.addLabel(TextFormat.RED + registrationSection.getString("error"));
        }
        form.addInput(inputSection.getString("name"), inputSection.getString("placeholder"), oldPassword);
        form.send(player, (targetPlayer, targetForm, data) -> {
            if(data == null) {
                sendRegistrationExitForm(targetPlayer);
                return;
            }
            String password = (String) data.get(error ? 2 : 1);
            if(register(targetPlayer, password)) {
                targetPlayer.sendMessage(TextFormat.GREEN + registrationSection.getString("success"));
                login(player, password);
            } else {
                sendRegistrationForm(targetPlayer, true, password);
            }
        });
    }

    public void sendRegistrationExitForm(Player player) {
        ConfigSection authorizationSection = plugin.getConfig().getSection("authorization");
        ConfigSection exitSection = authorizationSection.getSection("exit");
        ModalForm form = new ModalForm(exitSection.getString("title"), exitSection.getString("content"), "Да", "Нет");
        form.send(player, (targetPlayer, targetForm, data) -> {
            if(data == -1) {
                sendRegistrationForm(targetPlayer, false, "");
                return;
            }
            if(data == 0) {
                targetPlayer.close();
            } else {
                sendRegistrationForm(targetPlayer, false, "");
            }
        });
    }

    public boolean register(Player player, String password) {
        if(!password.matches("^(?=.*[a-zA-Z])(?=.*[0-9])(?=\\S+$).{8,}$")) {
            return false;
        }
        try {
            PreparedStatement preparedStatement = plugin.database.connection.prepareStatement("INSERT INTO players (name, password, address, uuid, x, y, z, yaw, pitch, location, level, experience) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            preparedStatement.setString(1, player.getName());
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, player.getAddress());
            preparedStatement.setString(4, player.getUniqueId().toString());
            preparedStatement.setDouble(5, player.getX());
            preparedStatement.setDouble(6, player.getY());
            preparedStatement.setDouble(7, player.getZ());
            preparedStatement.setDouble(8, player.getYaw());
            preparedStatement.setDouble(9, player.getPitch());
            preparedStatement.setString(10, player.getLevel().getName());
            preparedStatement.setInt(11, 1);
            preparedStatement.setInt(12, 0);
            preparedStatement.execute();
            return true;
        } catch (Exception exception) {
            plugin.getLogger().error(exception.getMessage());
        }
        return false;
    }

    public boolean isRegistered(Player player) {
        try {
            PreparedStatement preparedStatement = plugin.database.connection.prepareStatement("SELECT * FROM players WHERE name = ?");
            preparedStatement.setString(1, player.getName());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (Exception exception) {
            plugin.getLogger().error(exception.getMessage());
        }
        return false;
    }

    public void sendLoginForm(Player player) {
        sendLoginForm(player, false);
    }

    public void sendLoginForm(Player player, boolean error) {
        ConfigSection authorizationSection = plugin.getConfig().getSection("authorization");
        ConfigSection inputSection = authorizationSection.getSection("input");
        ConfigSection loginSection = authorizationSection.getSection("login");
        CustomForm form = new CustomForm();
        form.setTitle(loginSection.getString("title"));
        form.addLabel(loginSection.getString("label"));
        if(error) {
            form.addLabel(TextFormat.RED + loginSection.getString("error"));
        }
        form.addInput(inputSection.getString("name"), inputSection.getString("placeholder"), "");
        form.send(player, (targetPlayer, targetForm, data) -> {
            if(data == null) {
                sendLoginExitForm(targetPlayer);
                return;
            }
            String password = (String) data.get(error ? 2 : 1);
            if(login(targetPlayer, password)) {
                targetPlayer.sendMessage(TextFormat.GREEN + loginSection.getString("success"));
            } else {
                sendLoginForm(targetPlayer, true);
            }
        });
    }

    public void sendLoginExitForm(Player player) {
        ConfigSection authorizationSection = plugin.getConfig().getSection("authorization");
        ConfigSection exitSection = authorizationSection.getSection("exit");
        ModalForm form = new ModalForm(exitSection.getString("title"), exitSection.getString("content"), "Да", "Нет");
        form.send(player, (targetPlayer, targetForm, data) -> {
            if(data == -1) {
                sendLoginForm(targetPlayer, false);
                return;
            }
            if(data == 0) {
                targetPlayer.close();
            } else {
                sendLoginForm(targetPlayer, false);
            }
        });
    }

    public boolean login(Player player, String password) {
        try {
            PreparedStatement preparedStatement = plugin.database.connection.prepareStatement("SELECT password FROM players WHERE name = ?");
            preparedStatement.setString(1, player.getName());
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            if(resultSet.getString("password").equals(password)) {
                players.put(player.getName(), player.getUniqueId());
                player.setImmobile(false);
                return true;
            }
        } catch (Exception exception) {
            plugin.getLogger().error(exception.getMessage());
        }
        return false;
    }

    public boolean isLogined(Player player) {
        String name = player.getName();
        if(players.containsKey(name)) {
            return players.get(name).equals(player.getUniqueId());
        }
        return false;
    }

    public void welcome(Player player) {
        ConfigSection authorizationSection = plugin.getConfig().getSection("authorization");
        ConfigSection welcomeSection = authorizationSection.getSection("welcome");
        player.sendTitle(welcomeSection.getString("title").replaceAll("%server", plugin.getConfig().getString("server")), welcomeSection.getString("subtitle"));
        player.setImmobile(false);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        event.setCancelled(!isLogined(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerEatFood(PlayerEatFoodEvent event) {
        event.setCancelled(!isLogined(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerBlockPick(PlayerBlockPickEvent event) {
        event.setCancelled(!isLogined(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(PlayerChatEvent event) {
        event.setCancelled(!isLogined(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerAnimation(PlayerAnimationEvent event) {
        event.setCancelled(!isLogined(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        event.setCancelled(!isLogined(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        event.setCancelled(!isLogined(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        event.setCancelled(!isLogined(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        event.setCancelled(!isLogined(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChangeSkin(PlayerChangeSkinEvent event) {
        event.setCancelled(!isLogined(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        event.setCancelled(!isLogined(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setCancelled(!isLogined(event.getEntity()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.setCancelled(!isLogined(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerFoodLevelChange(PlayerFoodLevelChangeEvent event) {
        event.setCancelled(!isLogined(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        event.setCancelled(!isLogined(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        event.setCancelled(!isLogined(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        event.setCancelled(!isLogined(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        event.setCancelled(!isLogined(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        event.setCancelled(!isLogined(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        event.setCancelled(!isLogined(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        event.setCancelled(!isLogined(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
        event.setCancelled(!isLogined(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerToggleGlide(PlayerToggleGlideEvent event) {
        event.setCancelled(!isLogined(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerToggleSwim(PlayerToggleSwimEvent event) {
        event.setCancelled(!isLogined(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof Player) {
            event.setCancelled(!isLogined((Player) event.getEntity()));
        }
    }
}
