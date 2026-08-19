package no.hammers.anarchycore.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class TpCommand extends Command {
    public TpCommand() {
        super("tp", "Teleport to a player", "/tp <player>", List.of("teleport"));
        setPermission("anarchycore.admin");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!testPermission(sender)) return true;
        if (!(sender instanceof Player player)) return true;
        if (args.length != 1) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Usage: /tp <player></red>"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Player not found.</red>"));
            return true;
        }
        
        player.teleport(target.getLocation());
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Teleported to " + target.getName() + ".</green>"));
        return true;
    }
}
