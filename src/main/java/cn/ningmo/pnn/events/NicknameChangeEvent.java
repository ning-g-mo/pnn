package cn.ningmo.pnn.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NicknameChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String oldNickname;
    private final String newNickname;

    public NicknameChangeEvent(Player player, String oldNickname, String newNickname) {
        this.player = player;
        this.oldNickname = oldNickname;
        this.newNickname = newNickname;
    }

    public Player getPlayer() {
        return player;
    }

    public String getOldNickname() {
        return oldNickname;
    }

    public String getNewNickname() {
        return newNickname;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
} 