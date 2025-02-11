package cn.ningmo.pnn.storage;

import cn.ningmo.pnn.PlayerNickname;
import cn.ningmo.pnn.cache.NicknameCache;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.UUID;

public class StorageManager {
    private final Storage storage;
    private final NicknameCache cache;
    private final PlayerNickname plugin;

    public StorageManager(PlayerNickname plugin) {
        this.plugin = plugin;
        this.cache = new NicknameCache(plugin);
        
        String mode = plugin.getConfigManager().getDataSavingMode();
        
        Storage tempStorage = null;
        try {
            switch (mode) {
                case "mysql":
                    tempStorage = new MySQLStorage(plugin);
                    break;
                case "sqlite":
                    tempStorage = new SQLiteStorage(plugin);
                    break;
                case "yaml":
                default:
                    tempStorage = new YamlStorage(plugin);
                    break;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("数据库连接失败! 使用YAML存储作为备选.");
            tempStorage = new YamlStorage(plugin);
        }
        
        storage = tempStorage;
    }

    public void setNickname(UUID uuid, String nickname) {
        // 异步保存到数据库
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            storage.setNickname(uuid, nickname);
            // 更新缓存
            cache.setNickname(uuid, nickname);
        });
    }

    public String getNickname(UUID uuid) {
        // 先从缓存获取
        String nickname = cache.getNickname(uuid);
        if (nickname != null) {
            return nickname;
        }
        
        // 缓存未命中，从存储获取
        nickname = storage.getNickname(uuid);
        if (nickname != null) {
            cache.setNickname(uuid, nickname);
        }
        return nickname;
    }

    public void removeNickname(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            storage.removeNickname(uuid);
            cache.remove(uuid);
        });
    }

    public boolean hasNickname(UUID uuid) {
        return getNickname(uuid) != null;
    }

    public UUID getPlayerByNickname(String nickname) {
        // 先从缓存查找
        UUID uuid = cache.getUUID(nickname);
        if (uuid != null) {
            return uuid;
        }
        
        // 从存储中查找
        uuid = storage.getUUIDByNickname(nickname);
        if (uuid != null) {
            // 更新缓存
            String foundNickname = storage.getNickname(uuid);
            if (foundNickname != null) {
                cache.setNickname(uuid, foundNickname);
            }
        }
        return uuid;
    }

    public void close() {
        storage.close();
        cache.clear();
    }
} 