package no.hammers.anarchycore.listeners;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;

import java.util.Set;

public class SecurityListener implements Listener {

    // Removed "help" and "?" so our custom HelpCommand works!
    private final Set<String> blockedCommands = Set.of(
            "plugins", "pl",
            "version", "ver", "about",
            "icanhasbukkit",
            "paper", "folia", "bukkit"
    );

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("anarchycore.admin")) return;

        String raw = event.getMessage().toLowerCase().split(" ")[0];
        String cmd = raw.startsWith("/") ? raw.substring(1) : raw;

        if (cmd.contains(":") || blockedCommands.contains(cmd)) {
            event.setCancelled(true);
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Unknown command. Type \"/help\" for help.</red>"));
        }
    }

    @EventHandler
    public void onCommandSend(PlayerCommandSendEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("anarchycore.admin")) return;

        event.getCommands().removeIf(cmd ->
                cmd.contains(":") || blockedCommands.contains(cmd.toLowerCase())
        );
    }

    @EventHandler
    public void onTabComplete(AsyncTabCompleteEvent event) {
        if (!(event.getSender() instanceof Player player)) return;
        if (player.hasPermission("anarchycore.admin")) return;

        String buffer = event.getBuffer().toLowerCase();

        if (buffer.startsWith("/")) {
            event.getCompletions().removeIf(completion -> {
                String clean = completion.startsWith("/") ? completion.substring(1).toLowerCase() : completion.toLowerCase();
                return clean.contains(":") || blockedCommands.contains(clean);
            });
        }
    }
}