package no.hammers.anarchycore.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import no.hammers.anarchycore.AnarchyCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class FreezeCommand extends Command {
    private final AnarchyCore plugin;
    public FreezeCommand(AnarchyCore plugin) {
        super("freeze", "Freeze a player", "/freeze <player>", List.of("ss"));
        this.plugin = plugin;
        setPermission("anarchycore.admin");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!testPermission(sender)) return true;
        if (args.length != 1) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Usage: /freeze <player></red>"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Player not found.</red>"));
            return true;
        }
        
        if (plugin.getFrozenPlayers().contains(target.getUniqueId())) {
            plugin.getFrozenPlayers().remove(target.getUniqueId());
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Unfrozen " + target.getName() + ".</green>"));
            target.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have been unfrozen.</green>"));
        } else {
            plugin.getFrozenPlayers().add(target.getUniqueId());
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Frozen " + target.getName() + ".</green>"));
            target.sendMessage(MiniMessage.miniMessage().deserialize("<red>You have been frozen by an admin!</red>"));
        }
        return true;
    }
}
