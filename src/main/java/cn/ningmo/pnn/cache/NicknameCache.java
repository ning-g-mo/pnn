package cn.ningmo.pnn.cache;

import cn.ningmo.pnn.PlayerNickname;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NicknameCache {
    private final PlayerNickname plugin;
    private final Map<UUID, String> uuidToNickname;
    private final Map<String, UUID> nicknameToUuid;
    
    public NicknameCache(PlayerNickname plugin) {
        this.plugin = plugin;
        this.uuidToNickname = new ConcurrentHashMap<>();
        this.nicknameToUuid = new ConcurrentHashMap<>();
    }
    
    public void setNickname(UUID uuid, String nickname) {
        // 异步更新缓存
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String oldNickname = uuidToNickname.get(uuid);
            if (oldNickname != null) {
                nicknameToUuid.remove(oldNickname);
            }
            
            if (nickname != null) {
                uuidToNickname.put(uuid, nickname);
                nicknameToUuid.put(nickname.toLowerCase(), uuid);
            } else {
                uuidToNickname.remove(uuid);
            }
        });
    }
    
    public String getNickname(UUID uuid) {
        return uuidToNickname.get(uuid);
    }
    
    public UUID getUUID(String nickname) {
        return nicknameToUuid.get(nickname.toLowerCase());
    }
    
    public void remove(UUID uuid) {
        String nickname = uuidToNickname.remove(uuid);
        if (nickname != null) {
            nicknameToUuid.remove(nickname.toLowerCase());
        }
    }
    
    public void clear() {
        uuidToNickname.clear();
        nicknameToUuid.clear();
    }
} 