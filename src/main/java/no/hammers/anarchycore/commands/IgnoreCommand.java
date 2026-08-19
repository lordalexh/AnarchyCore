package no.hammers.anarchycore.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import no.hammers.anarchycore.AnarchyCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class IgnoreCommand extends Command {

    private final AnarchyCore plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public IgnoreCommand(AnarchyCore plugin) {
        super("ignore", "Ignore or unignore a player's private messages.", "/ignore <player>", List.of("unignore"));
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length < 1) {
            // Show ignore list
            Set<UUID> ignored = plugin.getIgnoredPlayers().get(player.getUniqueId());
            if (ignored == null || ignored.isEmpty()) {
                player.sendMessage(miniMessage.deserialize("<gray>You are not ignoring anyone.</gray>"));
            } else {
                StringBuilder sb = new StringBuilder("<gray>Ignored players: </gray><white>");
                boolean first = true;
                for (UUID id : ignored) {
                    String name = Bukkit.getOfflinePlayer(id).getName();
                    if (!first) sb.append("<gray>, </gray>");
                    sb.append(name != null ? name : id.toString());
                    first = false;
                }
                sb.append("</white>");
                player.sendMessage(miniMessage.deserialize(sb.toString()));
            }
            return true;
        }

        @SuppressWarnings("deprecation")
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(miniMessage.deserialize("<red>Player not found.</red>"));
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(miniMessage.deserialize("<red>You cannot ignore yourself.</red>"));
            return true;
        }

        boolean nowIgnoring = plugin.toggleIgnore(player.getUniqueId(), target.getUniqueId());
        if (nowIgnoring) {
            player.sendMessage(miniMessage.deserialize(String.format("<green>Now ignoring %s.</green>", target.getName())));
        } else {
            player.sendMessage(miniMessage.deserialize(String.format("<green>No longer ignoring %s.</green>", target.getName())));
        }
        return true;
    }
}
