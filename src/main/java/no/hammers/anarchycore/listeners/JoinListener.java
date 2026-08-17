package no.hammers.anarchycore.listeners;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import no.hammers.anarchycore.AnarchyCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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

        // Record join number
        if (!plugin.getConfig().contains(path)) {
            int count = plugin.incrementUniquePlayers();
            plugin.getConfig().set(path, count);
            plugin.saveConfig();

            if (!player.hasPlayedBefore()) {
                plugin.getServer().broadcast(miniMessage.deserialize(
                        "<yellow><player> joined for the first time! Unique player #<count></yellow>",
                        Placeholder.parsed("player", player.getName()),
                        Placeholder.parsed("count", String.valueOf(count))
                ));
            }
        }

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
        plugin.updatePlayerTablist(player);
    }
}