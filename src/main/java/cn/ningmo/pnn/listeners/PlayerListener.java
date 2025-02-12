package cn.ningmo.pnn.listeners;

import cn.ningmo.pnn.PlayerNickname;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
        
        // 每秒检查一次tab显示
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (plugin.getConfigManager().isCoverTab()) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    String nickname = plugin.getStorageManager().getNickname(player.getUniqueId());
                    if (nickname != null) {
                        String format = plugin.getConfigManager().getCoverTabFormat();
                        String tabDisplay = formatNickname(format, player, nickname);
                        if (!tabDisplay.equals(player.getPlayerListName())) {
                            player.setPlayerListName(tabDisplay);
                        }
                    }
                }
            }
        }, 20L, 20L);  // 20tick = 1秒
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
        
        // 移除多余的格式化符号
        result = result.replace("%2$sNingMo_yanyan", "")
                      .replace("%2$s", "");
        
        return result.trim();  // 移除首尾空格
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
        
        // 设置聊天格式，确保消息正确显示
        event.setFormat(displayName + " %2$s");
    }

    @EventHandler(priority = EventPriority.HIGHEST)  // 使用最高优先级
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // 设置加入消息
        String nickname = plugin.getStorageManager().getNickname(player.getUniqueId());
        if (nickname != null) {
            event.setJoinMessage(plugin.getMessageManager().getMessage("events.join",
                "%player%", nickname));
        }

        // 延迟更长时间更新昵称显示，确保在其他插件之后
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            updatePlayerNickname(player);
        }, 5L);  // 延迟5tick而不是1tick
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
            return;  // 如果没有昵称，不进行任何更改
        }

        // 更新Tab栏显示
        if (plugin.getConfigManager().isCoverTab()) {
            String format = plugin.getConfigManager().getCoverTabFormat();
            String tabDisplay = formatNickname(format, player, nickname);
            
            // 强制更新tab显示
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.setPlayerListName(tabDisplay);
                // 刷新玩家显示
                player.hidePlayer(plugin, player);
                player.showPlayer(plugin, player);
            });
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