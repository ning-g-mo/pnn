package cn.ningmo.pnn;

import org.bukkit.plugin.java.JavaPlugin;
import cn.ningmo.pnn.commands.NicknameCommand;
import cn.ningmo.pnn.listeners.PlayerListener;
import cn.ningmo.pnn.storage.StorageManager;
import cn.ningmo.pnn.config.ConfigManager;
import cn.ningmo.pnn.config.MessageManager;
import org.bukkit.event.player.PlayerJoinEvent;
import cn.ningmo.pnn.hook.PlaceholderHook;

public class PlayerNickname extends JavaPlugin {
    
    private static PlayerNickname instance;
    private StorageManager storageManager;
    private ConfigManager configManager;
    private MessageManager messageManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // 加载配置
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        
        // 初始化存储管理器
        storageManager = new StorageManager(this);
        
        // 注册命令
        getCommand("pnn").setExecutor(new NicknameCommand(this));
        
        // 注册监听器
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        // 注册PAPI扩展
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderHook(this).register();
            getLogger().info("已挂钩 PlaceholderAPI!");
        }
        
        getLogger().info("玩家昵称插件已启用！");
    }

    @Override
    public void onDisable() {
        if (storageManager != null) {
            storageManager.close();
        }
        getLogger().info("玩家昵称插件已禁用！");
    }

    public static PlayerNickname getInstance() {
        return instance;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public void reloadPlugin() {
        // 重载配置
        reloadConfig();
        configManager.reloadConfig();
        messageManager.reloadMessages();
        
        // 重载存储
        storageManager.reload();
        
        // 重新应用所有在线玩家的昵称
        getServer().getOnlinePlayers().forEach(player -> {
            getServer().getPluginManager().callEvent(new PlayerJoinEvent(player, null));
        });
    }
} 