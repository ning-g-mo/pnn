package cn.ningmo.pnn.listeners;

import cn.ningmo.pnn.PlayerNickname;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import me.clip.placeholderapi.PlaceholderAPI;

public class PlayerListener implements Listener {
    private final PlayerNickname plugin;

    public PlayerListener(PlayerNickname plugin) {
        this.plugin = plugin;
    }

    private String processPlaceholders(String text, Player player) {
        if (text == null) {
            return "";
        }
        
        // 先处理PAPI变量
        if (plugin.getConfigManager().isPlaceholderEnabled() && 
            Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }
        
        // 再处理颜色代码
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private String formatNickname(String format, Player player, String nickname) {
        // 先处理昵称中的PAPI变量和颜色代码
        nickname = processPlaceholders(nickname, player);
        
        // 处理格式中的PAPI变量和颜色代码
        format = processPlaceholders(format, player);
        
        // 处理占位符
        String result = format;
        
        // 先处理复合格式的占位符
        result = result.replace("%pnn_ID%", nickname + player.getName())
                      .replace("%ID_pnn%", player.getName() + nickname);
        
        // 再处理简单格式的占位符
        result = result.replace("%pnn%", nickname)
                      .replace("%message%", "%2$s")
                      .replace("%player%", player.getName())
                      .replace("%nickname%", nickname);
        
        // 确保消息占位符正确
        if (!result.contains("%2$s")) {
            result = result.trim() + " %2$s";
        }
        
        return result;
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
        String displayName = formatNickname(format, player, nickname);
        
        // 处理格式字符串，确保安全
        displayName = displayName.replace("%", "%%")  // 转义所有%
                                .replace("%%2$s", "%2$s"); // 还原消息占位符
        
        try {
            event.setFormat(displayName);
        } catch (Exception e) {
            // 如果格式设置失败，使用安全的默认格式
            plugin.getLogger().warning("聊天格式设置失败，使用默认格式");
            event.setFormat("%2$s");
        }
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
            String tabDisplay = formatNickname(format, player, nickname);
            player.setPlayerListName(tabDisplay);
        }

        // 更新头顶显示
        if (plugin.getConfigManager().isCoverHead()) {
            String format = plugin.getConfigManager().getCoverHeadFormat();
            String headDisplay = formatNickname(format, player, nickname);
            
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