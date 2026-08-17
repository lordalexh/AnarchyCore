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
import java.util.concurrent.CompletableFuture;

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

        // Do not override if the player has a valid bed or respawn anchor
        if (event.isBedSpawn() || event.isAnchorSpawn()) {
            return;
        }

        Location randomSpawn = getRandomSpawnLocationSync();
        if (randomSpawn != null) {
            event.setRespawnLocation(randomSpawn);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Handle initial first-join spawn ring asynchronously
        if (!player.hasPlayedBefore() && plugin.getConfig().getBoolean("spawn.enabled", true)) {
            getRandomSpawnLocationAsync().thenAccept(location -> {
                if (location != null && player.isOnline()) {
                    player.teleportAsync(location);
                }
            });
        }
    }

    /**
     * Non-blocking async chunk load & location calculation for first joins.
     */
    private CompletableFuture<Location> getRandomSpawnLocationAsync() {
        World world = getSpawnWorld();
        int[] coords = calculateRandomXZ();
        int x = coords[0];
        int z = coords[1];

        // Load/generate chunk asynchronously via Paper/Folia API (x >> 4 converts block coord to chunk coord)
        return world.getChunkAtAsync(x >> 4, z >> 4).thenApply(chunk -> {
            int y = world.getHighestBlockYAt(x, z) + 1;
            return new Location(world, x + 0.5, y, z + 0.5);
        });
    }

    /**
     * Synchronous calculation for immediate respawn location override.
     */
    private Location getRandomSpawnLocationSync() {
        World world = getSpawnWorld();
        int[] coords = calculateRandomXZ();
        int x = coords[0];
        int z = coords[1];
        int y = world.getHighestBlockYAt(x, z) + 1;

        return new Location(world, x + 0.5, y, z + 0.5);
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