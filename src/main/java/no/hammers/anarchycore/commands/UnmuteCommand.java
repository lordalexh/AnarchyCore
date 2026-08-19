package no.hammers.anarchycore.commands;

import no.hammers.anarchycore.AnarchyCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class UnmuteCommand extends Command {

    private final AnarchyCore plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public UnmuteCommand(AnarchyCore plugin) {
        super("unmute");
        this.plugin = plugin;
        this.setPermission("anarchycore.unmute");
        this.setUsage("/unmute <player>");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("anarchycore.unmute")) {
            sender.sendMessage(miniMessage.deserialize("<red>No permission.</red>"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(miniMessage.deserialize("<red>Usage: /unmute <player></red>"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        
        plugin.getDatabaseManager().removeActivePunishments(target.getUniqueId(), "mute");
        sender.sendMessage(miniMessage.deserialize("<green>Unmuted " + target.getName() + ".</green>"));
        
        if (target.isOnline()) {
            target.getPlayer().sendMessage(miniMessage.deserialize("<green>You have been unmuted.</green>"));
        }
        
        return true;
    }
}
