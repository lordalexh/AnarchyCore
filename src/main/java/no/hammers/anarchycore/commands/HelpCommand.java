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
        List<String> lines = plugin.getConfig().getStringList("help-message");
        if (lines.isEmpty()) {
            sender.sendMessage(miniMessage.deserialize("<gold>Type /msg, /kill, /tps, /ping, /stats, or /toggledeaths.</gold>"));
            return true;
        }

        for (String line : lines) {
            sender.sendMessage(miniMessage.deserialize(line));
        }
        return true;
    }
}