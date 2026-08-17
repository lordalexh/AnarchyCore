package no.hammers.anarchycore.listeners;

import no.hammers.anarchycore.AnarchyCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Random;

public class SpawnListener implements Listener {

    private final AnarchyCore plugin;
    private final Random random = new Random();

    public SpawnListener(AnarchyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!plugin.getConfig().getBoolean("spawn.enabled", true)) {
            return;
        }

        // Respect valid bed or anchor spawn points
        if (event.isBedSpawn() || event.isAnchorSpawn()) {
            return;
        }

        Player player = event.getPlayer();

        // Delay 1 tick on the player's Folia entity scheduler to allow default respawn to complete
        player.getScheduler().runDelayed(plugin, task -> teleportToRandomRingSpawn(player), null, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Handle initial first-join spawn ring
        if (!player.hasPlayedBefore() && plugin.getConfig().getBoolean("spawn.enabled", true)) {
            // Delay 1 tick on the player's Folia entity scheduler to avoid join packet overrides
            player.getScheduler().runDelayed(plugin, task -> teleportToRandomRingSpawn(player), null, 1L);
        }
    }

    /**
     * Safely calculates a random ring location, asynchronously loads the chunk,
     * and teleports the player on their Folia entity thread.
     */
    private void teleportToRandomRingSpawn(Player player) {
        if (!player.isOnline()) {
            return;
        }

        World world = getSpawnWorld();
        int[] coords = calculateRandomXZ();
        int x = coords[0];
        int z = coords[1];

        // 1. Asynchronously load/generate the target chunk (x >> 4 converts block to chunk coord)
        world.getChunkAtAsync(x >> 4, z >> 4).thenAccept(chunk -> {
            // 2. Compute highest block Y now that the chunk is loaded into memory
            int highestY = world.getHighestBlockYAt(x, z);
            int safeY = Math.max(world.getMinHeight() + 1, highestY + 1);

            Location targetLocation = new Location(world, x + 0.5, safeY, z + 0.5);

            // 3. Dispatch teleport back to the player's Folia entity scheduler
            player.getScheduler().run(plugin, task -> {
                if (player.isOnline()) {
                    player.teleportAsync(targetLocation);
                }
            }, null);
        });
    }

    private World getSpawnWorld() {
        String worldName = plugin.getConfig().getString("spawn.world", "world");
        World world = Bukkit.getWorld(worldName);
        return (world != null) ? world : Bukkit.getWorlds().get(0);
    }

    private int[] calculateRandomXZ() {
        int minRadius = plugin.getConfig().getInt("spawn.min-radius", 500);
        int maxRadius = plugin.getConfig().getInt("spawn.max-radius", 2000);

        if (minRadius > maxRadius) {
            int temp = minRadius;
            minRadius = maxRadius;
            maxRadius = temp;
        }

        double radius = minRadius + (maxRadius - minRadius) * random.nextDouble();
        double angle = random.nextDouble() * 2 * Math.PI;

        int x = (int) (radius * Math.cos(angle));
        int z = (int) (radius * Math.sin(angle));

        return new int[]{x, z};
    }
}