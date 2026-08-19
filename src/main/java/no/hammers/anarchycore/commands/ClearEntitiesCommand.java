package no.hammers.anarchycore.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClearEntitiesCommand extends Command {
    public ClearEntitiesCommand() {
        super("clearentities", "Clear entities in radius", "/clearentities <radius>", List.of("ce"));
        setPermission("anarchycore.admin");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!testPermission(sender)) return true;
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Usage: /clearentities <radius></red>"));
            return true;
        }
        try {
            double radius = Double.parseDouble(args[0]);
            int count = 0;
            for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                if (!(entity instanceof Player)) {
                    entity.remove();
                    count++;
                }
            }
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Cleared " + count + " entities.</green>"));
        } catch (NumberFormatException e) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Invalid radius.</red>"));
        }
        return true;
    }
}
