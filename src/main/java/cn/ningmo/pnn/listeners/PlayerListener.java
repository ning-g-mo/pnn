package cn.ningmo.pnn.listeners;

import cn.ningmo.pnn.PlayerNickname;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class PlayerListener implements Listener {
    private final PlayerNickname plugin;

    public PlayerListener(PlayerNickname plugin) {
        this.plugin = plugin;
    }

    private String formatNickname(String format, Player player, String nickname) {
        return format.replace("{pnn}", nickname)
                    .replace("{pnn_ID}", nickname + player.getName())
                    .replace("{ID_pnn}", player.getName() + nickname)
                    .replace("{message}", "%2$s")
                    .replace("%player%", player.getName())
                    .replace("%nickname%", nickname)
                    .replace("%message%", "%2$s");
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!plugin.getConfigManager().isCoveringChat()) {
            return;
        }

        Player player = event.getPlayer();
        String nickname = plugin.getStorageManager().getNickname(player.getUniqueId());
        if (nickname == null) {
            nickname = player.getName();
        }

        String format = plugin.getConfigManager().getCoveringChatFormat();
        event.setFormat(ChatColor.translateAlternateColorCodes('&', 
            formatNickname(format, player, nickname)));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        updatePlayerNickname(player);

        // 设置加入消息
        String nickname = plugin.getStorageManager().getNickname(player.getUniqueId());
        if (nickname != null) {
            event.setJoinMessage(plugin.getMessageManager().getMessage("events.join",
                "%player%", nickname));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String nickname = plugin.getStorageManager().getNickname(player.getUniqueId());
        if (nickname != null) {
            event.setQuitMessage(plugin.getMessageManager().getMessage("events.quit",
                "%player%", nickname));
        }
    }

    private void updatePlayerNickname(Player player) {
        String nickname = plugin.getStorageManager().getNickname(player.getUniqueId());
        if (nickname == null) {
            return;
        }

        // 更新Tab栏显示
        if (plugin.getConfigManager().isCoverTab()) {
            String format = plugin.getConfigManager().getCoverTabFormat();
            player.setPlayerListName(ChatColor.translateAlternateColorCodes('&',
                formatNickname(format, player, nickname)));
        }

        // 更新头顶显示
        if (plugin.getConfigManager().isCoverHead()) {
            String format = plugin.getConfigManager().getCoverHeadFormat();
            String headDisplay = ChatColor.translateAlternateColorCodes('&',
                formatNickname(format, player, nickname));
            
            Scoreboard scoreboard = player.getScoreboard();
            if (scoreboard == null) {
                scoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();
                player.setScoreboard(scoreboard);
            }

            String teamName = "pnn_" + player.getName();
            Team team = scoreboard.getTeam(teamName);
            if (team == null) {
                team = scoreboard.registerNewTeam(teamName);
            }

            team.setPrefix(headDisplay);
            team.addEntry(player.getName());
        }
    }
} 