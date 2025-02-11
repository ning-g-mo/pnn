package cn.ningmo.pnn.storage;

import java.util.UUID;

public interface Storage {
    void setNickname(UUID uuid, String nickname);
    String getNickname(UUID uuid);
    void removeNickname(UUID uuid);
    boolean hasNickname(UUID uuid);
    UUID getUUIDByNickname(String nickname);
    void close();
} 