package cn.ningmo.pnn.config;

import org.bukkit.configuration.file.FileConfiguration;
import cn.ningmo.pnn.PlayerNickname;

import java.util.List;

public class ConfigManager {
    
    private final PlayerNickname plugin;
    private FileConfiguration config;

    public ConfigManager(PlayerNickname plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public boolean isPlaceholderEnabled() {
        return config.getBoolean("placeholder", false);
    }

    public List<String> getBlockWords() {
        return config.getStringList("Block-words");
    }

    public boolean isCoveringChat() {
        return config.getBoolean("covering-chat", false);
    }

    public String getCoveringChatFormat() {
        return config.getString("covering-chat-formats", "&7%pnn% &f>>> %message%");
    }

    public boolean isCoverTab() {
        return config.getBoolean("cover-tab", false);
    }

    public String getCoverTabFormat() {
        return config.getString("cover-tab-formats", "&7%pnn%");
    }

    public boolean isCoverHead() {
        return config.getBoolean("cover-head", false);
    }

    public String getCoverHeadFormat() {
        return config.getString("cover-head-formats", "&7%pnn%");
    }

    public String getDataSavingMode() {
        return config.getString("data-saving-mode", "yaml").toLowerCase();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
} 