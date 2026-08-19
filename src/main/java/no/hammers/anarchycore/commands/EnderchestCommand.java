package no.hammers.anarchycore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class EnderchestCommand extends Command {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public EnderchestCommand() {
        super("enderchest");
        this.setPermission("anarchycore.enderchest");
        this.setUsage("/enderchest <player>");
        this.setAliases(java.util.List.of("ec"));
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("anarchycore.enderchest")) {
            player.sendMessage(miniMessage.deserialize("<red>No permission.</red>"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(miniMessage.deserialize("<red>Usage: /enderchest <player></red>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(miniMessage.deserialize("<red>Player not found.</red>"));
            return true;
        }

        player.openInventory(target.getEnderChest());
        player.sendMessage(miniMessage.deserialize("<green>Opening enderchest of " + target.getName() + ".</green>"));
        
        return true;
    }
}
