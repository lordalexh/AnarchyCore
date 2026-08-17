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