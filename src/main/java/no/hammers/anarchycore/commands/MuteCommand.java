package no.hammers.anarchycore.commands;

import no.hammers.anarchycore.AnarchyCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;
import java.util.List;

public class MuteCommand extends Command {

    private final AnarchyCore plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MuteCommand(AnarchyCore plugin) {
        super("mute");
        this.plugin = plugin;
        this.setPermission("anarchycore.mute");
        this.setUsage("/mute <player> [reason]");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("anarchycore.mute")) {
            sender.sendMessage(miniMessage.deserialize("<red>No permission.</red>"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(miniMessage.deserialize("<red>Usage: /mute <player> [reason]</red>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(miniMessage.deserialize("<red>Player not found.</red>"));
            return true;
        }

        String reason = args.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : "No reason provided.";
        
        // Save to DB (expiration -1 for permanent for now, or could implement time)
        plugin.getDatabaseManager().addPunishment(target.getUniqueId(), "mute", reason, -1);
        
        sender.sendMessage(miniMessage.deserialize("<green>Muted " + target.getName() + " for: " + reason + "</green>"));
        target.sendMessage(miniMessage.deserialize("<red>You have been muted for: " + reason + "</red>"));
        
        return true;
    }
}
