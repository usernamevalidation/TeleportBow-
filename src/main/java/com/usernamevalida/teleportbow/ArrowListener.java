package com.usernamevalida.teleportbow;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ArrowListener implements Listener {

    private final TeleportBow plugin;
    private final SafetyChecker safetyChecker;
    private final Map<UUID, Long> lastTeleport = new ConcurrentHashMap<>();

    public ArrowListener(TeleportBow plugin, SafetyChecker safetyChecker) {
        this.plugin = plugin;
        this.safetyChecker = safetyChecker;
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        plugin.debug("ProjectileHitEvent fired: " + event.getEntity().getType());

        if (!(event.getEntity() instanceof Arrow arrow)) {
            plugin.debug("Not an Arrow, ignoring");
            return;
        }

        if (!(arrow.getShooter() instanceof Player player)) {
            plugin.debug("Shooter is not a Player, ignoring");
            return;
        }

        plugin.debug("Arrow from " + player.getName()
                + " landed at " + arrow.getLocation());

        if (!player.hasPermission("teleportbow.use")) {
            plugin.debug("Player lacks teleportbow.use");
            return;
        }

        long cooldownMs = plugin.getConfigManager().getCooldownMs();
        if (cooldownMs > 0) {
            long now = System.currentTimeMillis();
            Long last = lastTeleport.get(player.getUniqueId());
            if (last != null && now - last < cooldownMs) {
                long remainingMs = cooldownMs - (now - last);
                double remainingSec = remainingMs / 1000.0;
                plugin.debug("Cooldown active: " + remainingSec + "s remaining");
                player.sendMessage("§cTeleport bow on cooldown for §e"
                        + String.format("%.1f", remainingSec) + "s§c.");
                arrow.remove();
                return;
            }
        }

        Location landLoc = arrow.getLocation();
        Location target = landLoc.clone();

        int maxDist = plugin.getConfigManager().getMaxDistance();
        if (maxDist > 0) {
            double dist = player.getLocation().distance(target);
            plugin.debug("Distance to landing: " + dist + " (max " + maxDist + ")");
            if (dist > maxDist) {
                plugin.debug("REJECT: too far");
                arrow.remove();
                return;
            }
        }

        arrow.remove();

        Location found = findSafeSpot(target);
        if (found == null) {
            player.sendMessage("§cNo safe landing spot found — teleport cancelled.");
            return;
        }

        found.setX(found.getBlockX() + 0.5);
        found.setZ(found.getBlockZ() + 0.5);

        plugin.debug("Teleporting " + player.getName() + " to " + found);
        player.teleport(found);
        lastTeleport.put(player.getUniqueId(), System.currentTimeMillis());
        player.sendMessage("§aTeleported!");
    }

    private Location findSafeSpot(Location center) {
        World world = center.getWorld();
        if (world == null) return null;

        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int r = plugin.getConfigManager().getSearchRadius();

        plugin.debug("Searching for safe spot around " + cx + "," + cy + "," + cz
                + " radius=" + r);

        if (safetyChecker.isSafe(center)
                && safetyChecker.hasOpenDirection(world, cx, cy, cz)) {
            plugin.debug("Exact landing spot is safe");
            return center;
        }

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;

                    int x = cx + dx;
                    int y = cy + dy;
                    int z = cz + dz;

                    Location candidate = new Location(world, x, y, z);
                    if (!safetyChecker.isSafe(candidate)) continue;
                    if (!safetyChecker.hasOpenDirection(world, x, y, z)) continue;

                    plugin.debug("Found safe spot at offset "
                            + dx + "," + dy + "," + dz);
                    return candidate;
                }
            }
        }

        plugin.debug("No safe spot found in "
                + (2*r+1) + "x" + (2*r+1) + "x" + (2*r+1) + " cube");
        return null;
    }

}