package cn.ningmo.pnn.storage;

import cn.ningmo.pnn.PlayerNickname;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class YamlStorage implements Storage {
    private final File file;
    private final FileConfiguration data;

    public YamlStorage(PlayerNickname plugin) {
        file = new File(plugin.getDataFolder(), "nicknames.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("无法创建昵称数据文件!");
                e.printStackTrace();
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public void setNickname(UUID uuid, String nickname) {
        data.set(uuid.toString(), nickname);
        saveData();
    }

    @Override
    public String getNickname(UUID uuid) {
        return data.getString(uuid.toString());
    }

    @Override
    public void removeNickname(UUID uuid) {
        data.set(uuid.toString(), null);
        saveData();
    }

    @Override
    public boolean hasNickname(UUID uuid) {
        return data.contains(uuid.toString());
    }

    @Override
    public void close() {
        saveData();
    }

    @Override
    public UUID getUUIDByNickname(String nickname) {
        // 遍历所有条目查找匹配的昵称
        for (String uuidStr : data.getKeys(false)) {
            String storedNickname = data.getString(uuidStr);
            if (storedNickname != null && storedNickname.equalsIgnoreCase(nickname)) {
                try {
                    return UUID.fromString(uuidStr);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private void saveData() {
        try {
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 