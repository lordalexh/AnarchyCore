package no.hammers.anarchycore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class InvseeCommand extends Command {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public InvseeCommand() {
        super("invsee");
        this.setPermission("anarchycore.invsee");
        this.setUsage("/invsee <player>");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("anarchycore.invsee")) {
            player.sendMessage(miniMessage.deserialize("<red>No permission.</red>"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(miniMessage.deserialize("<red>Usage: /invsee <player></red>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(miniMessage.deserialize("<red>Player not found.</red>"));
            return true;
        }

        player.openInventory(target.getInventory());
        player.sendMessage(miniMessage.deserialize("<green>Opening inventory of " + target.getName() + ".</green>"));
        
        return true;
    }
}
