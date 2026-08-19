package no.hammers.anarchycore.listeners;

import net.kyori.adventure.text.minimessage.MiniMessage;
import no.hammers.anarchycore.AnarchyCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class BanListener implements Listener {

    private final AnarchyCore plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public BanListener(AnarchyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (plugin.getDatabaseManager().hasActivePunishment(event.getUniqueId(), "ban")) {
            String reason = plugin.getDatabaseManager().getActivePunishmentReason(event.getUniqueId(), "ban");
            event.disallow(
                    AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                    miniMessage.deserialize("<red>You are banned from this server!\nReason: " + reason + "</red>")
            );
        }
    }
}
