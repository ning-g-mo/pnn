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
        // 处理颜色代码
        text = ChatColor.translateAlternateColorCodes('&', text);
        
        // 如果启用了PAPI且已安装PAPI
        if (plugin.getConfigManager().isPlaceholderEnabled() && 
            Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }
        
        return text;
    }

    private String formatNickname(String format, Player player, String nickname) {
        // 处理昵称中的颜色代码和PAPI变量
        nickname = processPlaceholders(nickname, player);
        
        // 处理占位符
        String result = format
            .replace("%pnn%", nickname)
            .replace("%pnn_ID%", nickname + player.getName())
            .replace("%ID_pnn%", player.getName() + nickname)
            .replace("%message%", "%2$s")
            .replace("%player%", player.getName())
            .replace("%nickname%", nickname);
        
        // 处理格式中的颜色代码和PAPI变量
        result = processPlaceholders(result, player);
        
        // 确保消息占位符正确
        if (!result.contains("%2$s")) {
            result += " %2$s";
        }
        
        // 添加玩家名占位符
        if (!result.contains("%1$s")) {
            result = "%1$s " + result;
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
        
        // 使用安全的格式化方式，确保中文正常显示
        event.setFormat(displayName.replace("%", "%%").replace("%%2$s", "%2$s").replace("%%1$s", "%1$s"));
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