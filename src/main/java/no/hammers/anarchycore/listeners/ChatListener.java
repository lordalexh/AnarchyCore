package no.hammers.anarchycore.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import no.hammers.anarchycore.AnarchyCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

    private final AnarchyCore plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ChatListener(AnarchyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getDatabaseManager().hasActivePunishment(player.getUniqueId(), "mute")) {
            event.setCancelled(true);
            String reason = plugin.getDatabaseManager().getActivePunishmentReason(player.getUniqueId(), "mute");
            player.sendMessage(miniMessage.deserialize("<red>You cannot speak because you are muted.\nReason: " + reason + "</red>"));
            return;
        }

        if (plugin.getChatSlowdown() > 0 && !player.hasPermission("anarchycore.slowchat.bypass")) {
            long last = plugin.getLastChatTime().getOrDefault(player.getUniqueId(), 0L);
            long now = System.currentTimeMillis();
            long waitTime = plugin.getChatSlowdown() * 1000L;
            if (now - last < waitTime) {
                event.setCancelled(true);
                long remaining = (waitTime - (now - last)) / 1000L;
                player.sendMessage(miniMessage.deserialize("<red>Chat is slowed. Please wait " + remaining + " seconds.</red>"));
                return;
            }
            plugin.getLastChatTime().put(player.getUniqueId(), now);
        }
        
        String plain = PlainTextComponentSerializer.plainText().serialize(event.message());

        // 1. Format 4chan Greentext
        Component finalMessage = event.message();
        if (plain.startsWith(">")) {
            finalMessage = miniMessage.deserialize("<green>" + plain + "</green>");
        }
        event.message(finalMessage);

        // 2. Custom Chat Renderer (OG Prefix + Player Name + Message)
        event.renderer((source, sourceDisplayName, msg, viewer) -> {
            Component prefix = Component.empty();

            if (plugin.isOG(source)) {
                String ogPrefixStr = plugin.getConfig().getString("og-prefix", "<gray>[<gold>OG</gold>]</gray> ");
                prefix = miniMessage.deserialize(ogPrefixStr);
            }

            return prefix
                    .append(sourceDisplayName)
                    .append(miniMessage.deserialize("<gray>: </gray>"))
                    .append(msg);
        });
    }
}