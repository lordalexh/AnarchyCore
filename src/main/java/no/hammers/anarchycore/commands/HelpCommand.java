package no.hammers.anarchycore.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import no.hammers.anarchycore.AnarchyCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HelpCommand extends Command {

    private final AnarchyCore plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public HelpCommand(AnarchyCore plugin) {
        super("help", "View available server commands.", "/help", List.of("?"));
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        boolean isAdmin = sender.hasPermission("anarchycore.admin");
        
        List<String> lines = isAdmin 
                ? plugin.getConfig().getStringList("help-message-admin") 
                : plugin.getConfig().getStringList("help-message");

        if (lines.isEmpty()) {
            if (isAdmin) {
                sender.sendMessage(miniMessage.deserialize("<gold>Admin Commands:</gold>"));
                sender.sendMessage(miniMessage.deserialize("<gray>/ce, /ci, /kill, /cc, /sc, /socialspy, /tp, /tphere, /freeze, /gm, /heal, /feed, /fly, /ban, /mute, /kick, /vanish, /invsee, /ec, /areload</gray>"));
                sender.sendMessage(miniMessage.deserialize(" "));
            }
            sender.sendMessage(miniMessage.deserialize("<gold>Player Commands:</gold>"));
            sender.sendMessage(miniMessage.deserialize("<gray>/msg, /reply, /suicide, /tps, /ping, /stats, /toggledeaths, /help</gray>"));
            return true;
        }

        for (String line : lines) {
            sender.sendMessage(miniMessage.deserialize(line));
        }
        return true;
    }
}