package no.hammers.anarchycore.commands;

import no.hammers.anarchycore.AnarchyCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.OfflinePlayer;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class UnbanCommand extends Command {

    private final AnarchyCore plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public UnbanCommand(AnarchyCore plugin) {
        super("unban");
        this.plugin = plugin;
        this.setPermission("anarchycore.unban");
        this.setUsage("/unban <player>");
        this.setAliases(java.util.List.of("pardon"));
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("anarchycore.unban")) {
            sender.sendMessage(miniMessage.deserialize("<red>No permission.</red>"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(miniMessage.deserialize("<red>Usage: /unban <player></red>"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        
        plugin.getDatabaseManager().removeActivePunishments(target.getUniqueId(), "ban");
        sender.sendMessage(miniMessage.deserialize("<green>Unbanned " + target.getName() + ".</green>"));
        
        return true;
    }
}
