package cn.ningmo.pnn.commands;

import cn.ningmo.pnn.PlayerNickname;
import cn.ningmo.pnn.events.NicknameChangeEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class NicknameCommand implements CommandExecutor {
    private final PlayerNickname plugin;

    public NicknameCommand(PlayerNickname plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessageManager().getMessage("errors.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "set":
                if (!player.hasPermission("pnn.set")) {
                    player.sendMessage(plugin.getMessageManager().getMessage("errors.no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(plugin.getMessageManager().getMessage("errors.missing-nickname"));
                    return true;
                }
                setNickname(player, args[1]);
                break;
            case "get":
                if (!player.hasPermission("pnn.get")) {
                    player.sendMessage(plugin.getMessageManager().getMessage("errors.no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    listAllNicknames(player);
                } else {
                    getNickname(player, args[1]);
                }
                break;
            case "remove":
                if (!player.hasPermission("pnn.remove")) {
                    player.sendMessage(plugin.getMessageManager().getMessage("errors.no-permission"));
                    return true;
                }
                removeNickname(player);
                break;
            default:
                sendHelp(player);
                break;
        }
        return true;
    }

    private void setNickname(Player player, String nickname) {
        if (nickname.length() > 16) {
            player.sendMessage(plugin.getMessageManager().getMessage("errors.nickname-too-long"));
            return;
        }

        for (String blocked : plugin.getConfigManager().getBlockWords()) {
            if (nickname.toLowerCase().contains(blocked.toLowerCase())) {
                player.sendMessage(plugin.getMessageManager().getMessage("errors.contains-blocked-word"));
                return;
            }
        }

        String oldNickname = plugin.getStorageManager().getNickname(player.getUniqueId());
        
        NicknameChangeEvent event = new NicknameChangeEvent(player, oldNickname, nickname);
        plugin.getServer().getPluginManager().callEvent(event);

        plugin.getStorageManager().setNickname(player.getUniqueId(), nickname);
        player.sendMessage(plugin.getMessageManager().getMessage("success.nickname-set"));
    }

    private void getNickname(Player player, String nickname) {
        UUID uuid = plugin.getStorageManager().getPlayerByNickname(nickname);
        if (uuid != null) {
            Player target = plugin.getServer().getPlayer(uuid);
            if (target != null) {
                player.sendMessage(plugin.getMessageManager().getMessage("info.nickname-found",
                    "%nickname%", nickname,
                    "%player%", target.getName()));
            } else {
                player.sendMessage(plugin.getMessageManager().getMessage("errors.player-offline"));
            }
        } else {
            player.sendMessage(plugin.getMessageManager().getMessage("errors.player-not-found"));
        }
    }

    private void listAllNicknames(Player player) {
        player.sendMessage(plugin.getMessageManager().getMessage("info.nickname-list-header"));
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            String nickname = plugin.getStorageManager().getNickname(online.getUniqueId());
            if (nickname != null) {
                player.sendMessage(plugin.getMessageManager().getMessage("info.nickname-list-format",
                    "%player%", online.getName(),
                    "%nickname%", nickname));
            }
        }
    }

    private void removeNickname(Player player) {
        String oldNickname = plugin.getStorageManager().getNickname(player.getUniqueId());
        if (oldNickname != null) {
            NicknameChangeEvent event = new NicknameChangeEvent(player, oldNickname, null);
            plugin.getServer().getPluginManager().callEvent(event);
        }

        plugin.getStorageManager().removeNickname(player.getUniqueId());
        player.sendMessage(plugin.getMessageManager().getMessage("success.nickname-removed"));
    }

    private void sendHelp(Player player) {
        player.sendMessage(plugin.getMessageManager().getMessage("info.help-header"));
        if (player.hasPermission("pnn.set")) {
            player.sendMessage(plugin.getMessageManager().getMessage("info.help-set"));
        }
        if (player.hasPermission("pnn.get")) {
            player.sendMessage(plugin.getMessageManager().getMessage("info.help-get"));
        }
        if (player.hasPermission("pnn.remove")) {
            player.sendMessage(plugin.getMessageManager().getMessage("info.help-remove"));
        }
    }
} 