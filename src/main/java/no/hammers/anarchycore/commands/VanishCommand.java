package no.hammers.anarchycore.commands;

import no.hammers.anarchycore.AnarchyCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class VanishCommand extends Command {

    private final AnarchyCore plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public VanishCommand(AnarchyCore plugin) {
        super("vanish");
        this.plugin = plugin;
        this.setPermission("anarchycore.vanish");
        this.setUsage("/vanish");
        this.setAliases(java.util.List.of("v"));
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("anarchycore.vanish")) {
            player.sendMessage(miniMessage.deserialize("<red>No permission.</red>"));
            return true;
        }

        boolean isVanished = plugin.isVanished(player.getUniqueId());
        plugin.setVanished(player, !isVanished);

        if (!isVanished) {
            player.sendMessage(miniMessage.deserialize("<green>You are now vanished.</green>"));
        } else {
            player.sendMessage(miniMessage.deserialize("<green>You are no longer vanished.</green>"));
        }
        
        return true;
    }
}
