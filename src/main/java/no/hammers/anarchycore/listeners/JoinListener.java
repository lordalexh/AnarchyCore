package no.hammers.anarchycore.listeners;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import no.hammers.anarchycore.AnarchyCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class JoinListener implements Listener {

    private final AnarchyCore plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public JoinListener(AnarchyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String path = "players." + player.getUniqueId() + ".join-number";

        int joinNum = plugin.getDatabaseManager().getJoinNumber(player.getUniqueId());
        
        // Record join number
        if (joinNum == -1) {
            // Might exist in config but not DB yet (migration)
            if (plugin.getConfig().contains(path)) {
                joinNum = plugin.getConfig().getInt(path);
                plugin.getConfig().set(path, null); // Clear from config to save space
                plugin.saveConfig();
            } else {
                joinNum = plugin.incrementUniquePlayers();
            }
            
            if (!player.hasPlayedBefore()) {
                plugin.getServer().broadcast(miniMessage.deserialize(
                        "<yellow><player> joined for the first time! Unique player #<count></yellow>",
                        Placeholder.parsed("player", player.getName()),
                        Placeholder.parsed("count", String.valueOf(joinNum))
                ));
            }
        }
        
        // Save to DB
        plugin.getDatabaseManager().registerPlayerJoin(player.getUniqueId(), player.getName(), joinNum);

        // Send PRIVATE welcome message directly to player
        List<String> welcomeLines = plugin.getConfig().getStringList("welcome-message");
        for (String line : welcomeLines) {
            player.sendMessage(miniMessage.deserialize(line));
        }

        // Send relog notice if player combat logged in their previous session
        if (plugin.checkAndRemoveRelogNotice(player.getUniqueId())) {
            player.sendMessage(miniMessage.deserialize(
                    "<red><b>You logged out during combat in your previous session and were killed!</b></red>"
            ));
        }

        // Instant tablist update on join
        double rawMspt = Bukkit.getAverageTickTime();
        double roundedMspt = Math.round(rawMspt * 10.0) / 10.0;
        plugin.updatePlayerTablist(player, roundedMspt + "ms");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoinMessage(PlayerJoinEvent event) {
        if (plugin.isVanished(event.getPlayer().getUniqueId())) {
            event.joinMessage(null);
        }
        
        Player player = event.getPlayer();
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (plugin.isVanished(online.getUniqueId()) && !player.hasPermission("anarchycore.vanish.see")) {
                player.hidePlayer(plugin, online);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuitMessage(org.bukkit.event.player.PlayerQuitEvent event) {
        if (plugin.isVanished(event.getPlayer().getUniqueId())) {
            event.quitMessage(null);
        }
    }
}