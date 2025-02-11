package cn.ningmo.pnn.commands;

import cn.ningmo.pnn.PlayerNickname;
import cn.ningmo.pnn.events.NicknameChangeEvent;
import org.bukkit.Bukkit;
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
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "set":
                if (!(sender instanceof Player)) {
                    if (args.length < 3) {
                        sender.sendMessage("用法: /pnn set <玩家> <昵称>");
                        return true;
                    }
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage("玩家不在线!");
                        return true;
                    }
                    setNickname(target, args[2]);
                    sender.sendMessage("已设置玩家 " + target.getName() + " 的昵称为: " + args[2]);
                } else {
                    Player player = (Player) sender;
                    if (!player.hasPermission("pnn.set")) {
                        player.sendMessage(plugin.getMessageManager().getMessage("errors.no-permission"));
                        return true;
                    }
                    if (args.length < 2) {
                        player.sendMessage(plugin.getMessageManager().getMessage("errors.missing-nickname"));
                        return true;
                    }
                    setNickname(player, args[1]);
                }
                break;
            case "get":
                if (!sender.hasPermission("pnn.get")) {
                    sender.sendMessage(plugin.getMessageManager().getMessage("errors.no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    listAllNicknames(sender);
                } else {
                    getNickname(sender, args[1]);
                }
                break;
            case "remove":
                if (!sender.hasPermission("pnn.remove")) {
                    sender.sendMessage(plugin.getMessageManager().getMessage("errors.no-permission"));
                    return true;
                }
                removeNickname(sender);
                break;
            default:
                sendHelp(sender);
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

    private void getNickname(CommandSender sender, String nickname) {
        UUID uuid = plugin.getStorageManager().getPlayerByNickname(nickname);
        if (uuid != null) {
            Player target = plugin.getServer().getPlayer(uuid);
            if (target != null) {
                sender.sendMessage(plugin.getMessageManager().getMessage("info.nickname-found",
                    "%nickname%", nickname,
                    "%player%", target.getName()));
            } else {
                sender.sendMessage(plugin.getMessageManager().getMessage("errors.player-offline"));
            }
        } else {
            sender.sendMessage(plugin.getMessageManager().getMessage("errors.player-not-found"));
        }
    }

    private void listAllNicknames(CommandSender sender) {
        sender.sendMessage(plugin.getMessageManager().getMessage("info.nickname-list-header"));
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            String nickname = plugin.getStorageManager().getNickname(online.getUniqueId());
            if (nickname != null) {
                sender.sendMessage(plugin.getMessageManager().getMessage("info.nickname-list-format",
                    "%player%", online.getName(),
                    "%nickname%", nickname));
            }
        }
    }

    private void removeNickname(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessageManager().getMessage("errors.player-only"));
            return;
        }

        Player player = (Player) sender;
        String oldNickname = plugin.getStorageManager().getNickname(player.getUniqueId());
        if (oldNickname != null) {
            NicknameChangeEvent event = new NicknameChangeEvent(player, oldNickname, null);
            plugin.getServer().getPluginManager().callEvent(event);
        }

        plugin.getStorageManager().removeNickname(player.getUniqueId());
        sender.sendMessage(plugin.getMessageManager().getMessage("success.nickname-removed"));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.getMessageManager().getMessage("info.help-header"));
        if (sender.hasPermission("pnn.set")) {
            sender.sendMessage(plugin.getMessageManager().getMessage("info.help-set"));
        }
        if (sender.hasPermission("pnn.get")) {
            sender.sendMessage(plugin.getMessageManager().getMessage("info.help-get"));
        }
        if (sender.hasPermission("pnn.remove")) {
            sender.sendMessage(plugin.getMessageManager().getMessage("info.help-remove"));
        }
    }
} 