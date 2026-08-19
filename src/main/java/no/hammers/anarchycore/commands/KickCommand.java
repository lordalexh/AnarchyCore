package no.hammers.anarchycore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class KickCommand extends Command {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public KickCommand() {
        super("kick");
        this.setPermission("anarchycore.kick");
        this.setUsage("/kick <player> [reason]");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("anarchycore.kick")) {
            sender.sendMessage(miniMessage.deserialize("<red>No permission.</red>"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(miniMessage.deserialize("<red>Usage: /kick <player> [reason]</red>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(miniMessage.deserialize("<red>Player not found.</red>"));
            return true;
        }

        String reason = args.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : "Kicked by an admin.";
        
        target.kick(miniMessage.deserialize("<red>You have been kicked!\nReason: " + reason + "</red>"));
        sender.sendMessage(miniMessage.deserialize("<green>Kicked " + target.getName() + " for: " + reason + "</green>"));
        
        return true;
    }
}
