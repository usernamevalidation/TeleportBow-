package com.usernamevalida.teleportbow;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class TeleportBow extends JavaPlugin {

    private ConfigManager configManager;
    private SafetyChecker safetyChecker;
    private boolean debug;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.configManager.load();

        this.debug = getConfig().getBoolean("debug", false);

        this.safetyChecker = new SafetyChecker(configManager, this);

        getServer().getPluginManager().registerEvents(
                new ArrowListener(this, safetyChecker), this);

        getLogger().info("TeleportBow enabled. debug=" + debug);
    }

    @Override
    public void onDisable() {
        getLogger().info("TeleportBow disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("teleportbow")) return false;

        if (!sender.hasPermission("teleportbow.reload")) {
            sender.sendMessage("§cYou don't have permission.");
            return true;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage("§eUsage: /teleportbow reload");
            return true;
        }

        reloadConfig();
        configManager.load();
        this.debug = getConfig().getBoolean("debug", false);
        sender.sendMessage("§aTeleportBow config reloaded. debug=" + debug);
        return true;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public boolean isDebug() {
        return debug;
    }

    public void debug(String msg) {
        if (debug) getLogger().info("[DEBUG] " + msg);
    }

}