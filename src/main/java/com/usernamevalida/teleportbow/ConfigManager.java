package com.usernamevalida.teleportbow;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class ConfigManager {

    private final TeleportBow plugin;

    private int minVertical;
    private int minHorizontal;
    private int maxDistance;
    private int openAirRequirement;
    private int searchRadius;
    private long cooldownMs;
    private Set<Material> safeBlocks;
    private Set<Material> unsafeBlocks;
    private List<String> passablePatterns;

    public ConfigManager(TeleportBow plugin) {
        this.plugin = plugin;
    }

    public void load() {
        FileConfiguration cfg = plugin.getConfig();

        minVertical = cfg.getInt("minimum-air-blocks-vertical", 2);
        minHorizontal = cfg.getInt("minimum-air-blocks-horizontal", 2);
        maxDistance = cfg.getInt("maximum-distance", -1);
        openAirRequirement = cfg.getInt("open-air-requirement", 30);
        searchRadius = cfg.getInt("search-radius", 1);

        double cooldownSeconds = cfg.getDouble("teleport-cooldown", 1.5);
        cooldownMs = (long) (cooldownSeconds * 1000L);

        safeBlocks = parseMaterials(cfg.getStringList("safe-blocks"));
        unsafeBlocks = parseMaterials(cfg.getStringList("unsafe-blocks"));

        passablePatterns = cfg.getStringList("passable-name-patterns");
        if (passablePatterns == null) passablePatterns = new ArrayList<>();
        passablePatterns.replaceAll(String::toUpperCase);

        plugin.debug("Config loaded: minVertical=" + minVertical
                + " minHorizontal=" + minHorizontal
                + " maxDistance=" + maxDistance
                + " openAirRequirement=" + openAirRequirement
                + " searchRadius=" + searchRadius
                + " cooldownMs=" + cooldownMs
                + " safeBlocks=" + safeBlocks.size()
                + " unsafeBlocks=" + unsafeBlocks.size()
                + " passablePatterns=" + passablePatterns);
    }

    private Set<Material> parseMaterials(List<String> names) {
        Set<Material> set = EnumSet.noneOf(Material.class);
        for (String name : names) {
            Material mat = Material.matchMaterial(name.toUpperCase());
            if (mat != null) {
                set.add(mat);
            } else {
                plugin.getLogger().warning("Unknown material in config: " + name);
            }
        }
        return set;
    }

    public boolean isPassableByPattern(Material mat) {
        String name = mat.name();
        for (String pattern : passablePatterns) {
            if (name.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    public int getMinVertical() {
        return minVertical;
    }

    public int getMinHorizontal() {
        return minHorizontal;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public int getOpenAirRequirement() {
        return openAirRequirement;
    }

    public int getSearchRadius() {
        return searchRadius;
    }

    public long getCooldownMs() {
        return cooldownMs;
    }

    public Set<Material> getSafeBlocks() {
        return safeBlocks;
    }

    public Set<Material> getUnsafeBlocks() {
        return unsafeBlocks;
    }

}