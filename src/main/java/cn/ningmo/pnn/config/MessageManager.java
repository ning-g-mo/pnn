package cn.ningmo.pnn.config;

import cn.ningmo.pnn.PlayerNickname;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MessageManager {
    private final PlayerNickname plugin;
    private FileConfiguration messages;
    private File messagesFile;

    public MessageManager(PlayerNickname plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    private void loadMessages() {
        if (messagesFile == null) {
            messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        }

        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messages = YamlConfiguration.loadConfiguration(messagesFile);

        // 加载默认配置作为备选
        try (InputStreamReader reader = new InputStreamReader(
                plugin.getResource("messages.yml"), StandardCharsets.UTF_8)) {
            YamlConfiguration defaultMessages = YamlConfiguration.loadConfiguration(reader);
            messages.setDefaults(defaultMessages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMessage(String path) {
        String message = messages.getString(path);
        if (message == null) {
            return "Missing message: " + path;
        }
        return ChatColor.translateAlternateColorCodes('&', 
            messages.getString("prefix", "") + message);
    }

    public String getMessage(String path, String... replacements) {
        String message = getMessage(path);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        return message;
    }
} 