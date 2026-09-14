package com.usernamevalida.teleportbow;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class SafetyChecker {

    private final ConfigManager config;
    private final TeleportBow plugin;

    public SafetyChecker(ConfigManager config, TeleportBow plugin) {
        this.config = config;
        this.plugin = plugin;
    }

    public boolean isSafe(Location loc) {
        World world = loc.getWorld();
        if (world == null) {
            plugin.debug("isSafe: world null");
            return false;
        }

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        plugin.debug("Checking safety at " + x + "," + y + "," + z
                + " world=" + world.getName());

        Block ground = world.getBlockAt(x, y - 1, z);
        Block feet = world.getBlockAt(x, y, z);
        Block head = world.getBlockAt(x, y + 1, z);

        plugin.debug("ground=" + ground.getType()
                + " feet=" + feet.getType()
                + " head=" + head.getType());

        if (!ground.getType().isSolid()) {
            plugin.debug("REJECT: ground not solid");
            return false;
        }

        if (config.getUnsafeBlocks().contains(ground.getType())) {
            plugin.debug("REJECT: ground unsafe");
            return false;
        }

        if (!isPassable(feet.getType())) {
            plugin.debug("REJECT: feet not passable");
            return false;
        }

        if (!isPassable(head.getType())) {
            plugin.debug("REJECT: head not passable");
            return false;
        }

        if (config.getUnsafeBlocks().contains(feet.getType())) {
            plugin.debug("REJECT: feet unsafe");
            return false;
        }

        if (config.getUnsafeBlocks().contains(head.getType())) {
            plugin.debug("REJECT: head unsafe");
            return false;
        }

        int verticalSafe = 0;
        for (int i = 0; i < config.getMinVertical(); i++) {
            Block check = world.getBlockAt(x, y + i, z);
            if (isPassable(check.getType())) {
                verticalSafe++;
            }
        }
        plugin.debug("verticalSafe=" + verticalSafe + "/" + config.getMinVertical());
        if (verticalSafe < config.getMinVertical()) {
            plugin.debug("REJECT: not enough vertical space");
            return false;
        }

        int horizontalSafe = 0;
        int[][] offsets = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] off : offsets) {
            Block feetAdj = world.getBlockAt(x + off[0], y, z + off[1]);
            Block headAdj = world.getBlockAt(x + off[0], y + 1, z + off[1]);
            if (isPassable(feetAdj.getType()) && isPassable(headAdj.getType())) {
                horizontalSafe++;
            }
        }
        plugin.debug("horizontalSafe=" + horizontalSafe + "/" + config.getMinHorizontal());
        if (horizontalSafe < config.getMinHorizontal()) {
            plugin.debug("REJECT: not enough horizontal space");
            return false;
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Block b = world.getBlockAt(x + dx, y - 1, z + dz);
                if (config.getUnsafeBlocks().contains(b.getType())) {
                    plugin.debug("REJECT: unsafe block nearby " + b.getType());
                    return false;
                }
            }
        }

        plugin.debug("ACCEPT: safe location");
        return true;
    }

    /**
     * Returns true if at least one of the 6 cardinal directions from the given
     * block has `open-air-requirement` contiguous passable blocks.
     */
    public boolean hasOpenDirection(World world, int x, int y, int z) {
        int required = config.getOpenAirRequirement();
        plugin.debug("Checking open direction at " + x + "," + y + "," + z
                + " (need " + required + " passable blocks)");

        int[][] dirs = {{1,0,0}, {-1,0,0}, {0,1,0}, {0,-1,0}, {0,0,1}, {0,0,-1}};
        String[] names = {"+X", "-X", "+Y", "-Y", "+Z", "-Z"};

        for (int d = 0; d < dirs.length; d++) {
            int[] dir = dirs[d];
            int count = 0;
            for (int i = 1; i <= required; i++) {
                Block b = world.getBlockAt(x + dir[0]*i, y + dir[1]*i, z + dir[2]*i);
                if (isPassable(b.getType())) {
                    count++;
                } else {
                    plugin.debug("  " + names[d] + ": stopped at " + i
                            + " (" + b.getType() + ")");
                    break;
                }
            }
            if (count >= required) {
                plugin.debug("  " + names[d] + ": " + count + " passable blocks — ACCEPT");
                return true;
            }
        }

        plugin.debug("  No direction has " + required + " passable blocks — REJECT");
        return false;
    }

    private boolean isPassable(Material mat) {
        if (mat.isAir()) return true;
        if (config.getSafeBlocks().contains(mat)) return true;
        if (config.isPassableByPattern(mat)) return true;
        return false;
    }

}