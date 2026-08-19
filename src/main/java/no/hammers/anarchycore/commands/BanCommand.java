package no.hammers.anarchycore.commands;

import no.hammers.anarchycore.AnarchyCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class BanCommand extends Command {

    private final AnarchyCore plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public BanCommand(AnarchyCore plugin) {
        super("ban");
        this.plugin = plugin;
        this.setPermission("anarchycore.ban");
        this.setUsage("/ban <player> [reason]");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("anarchycore.ban")) {
            sender.sendMessage(miniMessage.deserialize("<red>No permission.</red>"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(miniMessage.deserialize("<red>Usage: /ban <player> [reason]</red>"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        String reason = args.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : "The Ban Hammer has spoken.";
        
        plugin.getDatabaseManager().addPunishment(target.getUniqueId(), "ban", reason, -1);
        
        if (target.isOnline()) {
            target.getPlayer().kick(miniMessage.deserialize("<red>You are banned from this server!\nReason: " + reason + "</red>"));
        }
        
        sender.sendMessage(miniMessage.deserialize("<green>Banned " + target.getName() + " for: " + reason + "</green>"));
        
        return true;
    }
}
