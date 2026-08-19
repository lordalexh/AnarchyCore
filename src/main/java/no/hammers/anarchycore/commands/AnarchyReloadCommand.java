package no.hammers.anarchycore.commands;

import no.hammers.anarchycore.AnarchyCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class AnarchyReloadCommand extends Command {

    private final AnarchyCore plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public AnarchyReloadCommand(AnarchyCore plugin) {
        super("reloadconfig");
        this.plugin = plugin;
        this.setPermission("anarchycore.reload");
        this.setUsage("/reloadconfig");
        this.setAliases(java.util.List.of("anarchyreload"));
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("anarchycore.reload")) {
            sender.sendMessage(miniMessage.deserialize("<red>No permission.</red>"));
            return true;
        }

        plugin.reloadConfig();
        sender.sendMessage(miniMessage.deserialize("<green>AnarchyCore configuration reloaded.</green>"));
        
        return true;
    }
}
