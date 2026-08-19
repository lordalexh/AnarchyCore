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

        if (event.isBedSpawn() || event.isAnchorSpawn()) {
            return;
        }

        Player player = event.getPlayer();
        World world = getSpawnWorld();
        int[] coords = calculateRandomXZ();
        int x = coords[0];
        int z = coords[1];

        int skyY = Math.max(world.getMaxHeight() - 2, 255);
        Location skySpawn = new Location(world, x + 0.5, skyY, z + 0.5);

        // 1. Immediately set the respawn location so they don't flash at world spawn
        event.setRespawnLocation(skySpawn);

        // 2. Schedule the ground teleport to happen after they have respawned
        Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task -> {
            if (!player.isOnline()) return;

            // 3. teleportAsync natively forces the chunk to load and switches the thread.
            player.teleportAsync(skySpawn).thenAccept(success -> {
                if (success && player.isOnline()) {
                    // 4. This callback strictly runs on the target chunk's region thread.
                    // The chunk is guaranteed to be loaded here, so getHighestBlockYAt is 100% safe.
                    int highestY = world.getHighestBlockYAt(x, z);
                    int safeY = Math.max(world.getMinHeight() + 1, highestY + 1);

                    Location groundLoc = new Location(world, x + 0.5, safeY, z + 0.5,
                            player.getLocation().getYaw(), player.getLocation().getPitch());

                    player.setFallDistance(0); // Prevent any fall damage
                    player.teleportAsync(groundLoc);
                }
            });
        }, 5L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPlayedBefore() && plugin.getConfig().getBoolean("spawn.enabled", true)) {
            World world = getSpawnWorld();
            int[] coords = calculateRandomXZ();
            int x = coords[0];
            int z = coords[1];

            int skyY = Math.max(world.getMaxHeight() - 2, 255);
            Location skySpawn = new Location(world, x + 0.5, skyY, z + 0.5);

            // Same logic as respawn, but no delay is needed because the player is already fully joined.
            player.teleportAsync(skySpawn).thenAccept(success -> {
                if (success && player.isOnline()) {
                    int highestY = world.getHighestBlockYAt(x, z);
                    int safeY = Math.max(world.getMinHeight() + 1, highestY + 1);

                    Location groundLoc = new Location(world, x + 0.5, safeY, z + 0.5,
                            player.getLocation().getYaw(), player.getLocation().getPitch());

                    player.setFallDistance(0);
                    player.teleportAsync(groundLoc);
                }
            });
        }
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
