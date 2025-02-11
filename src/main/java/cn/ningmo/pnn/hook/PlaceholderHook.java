package cn.ningmo.pnn.hook;

import cn.ningmo.pnn.PlayerNickname;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderHook extends PlaceholderExpansion {
    private final PlayerNickname plugin;

    public PlaceholderHook(PlayerNickname plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "pnn";
    }

    @Override
    public @NotNull String getAuthor() {
        return "柠枺";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        String nickname = plugin.getStorageManager().getNickname(player.getUniqueId());
        if (nickname == null) {
            nickname = player.getName();
        }

        switch (params.toLowerCase()) {
            case "pnn":
                return nickname;
            case "pnn_id":
                return nickname + player.getName();
            case "id_pnn":
                return player.getName() + nickname;
            default:
                return null;
        }
    }
} 