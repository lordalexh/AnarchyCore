package no.hammers.anarchycore.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import no.hammers.anarchycore.AnarchyCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class SlowChatCommand extends Command {
    private final AnarchyCore plugin;
    public SlowChatCommand(AnarchyCore plugin) {
        super("slowchat", "Set chat delay", "/slowchat <seconds>", List.of("sc"));
        this.plugin = plugin;
        setPermission("anarchycore.admin");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!testPermission(sender)) return true;
        if (args.length != 1) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Usage: /slowchat <seconds> (0 to disable)</red>"));
            return true;
        }
        try {
            int seconds = Integer.parseInt(args[0]);
            plugin.setChatSlowdown(seconds);
            if (seconds > 0) {
                Bukkit.broadcast(MiniMessage.miniMessage().deserialize("<yellow>Chat has been slowed to " + seconds + " seconds.</yellow>"));
            } else {
                Bukkit.broadcast(MiniMessage.miniMessage().deserialize("<green>Chat slow has been disabled.</green>"));
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Invalid number.</red>"));
        }
        return true;
    }
}
