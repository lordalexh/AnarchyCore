package no.hammers.anarchycore.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SuicideCommand extends Command {

    public SuicideCommand() {
        super("kill", "Self-terminate to escape a trap.", "/kill", List.of("suicide", "die"));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("anarchycore.admin")) {
            Player target = org.bukkit.Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Player not found.</red>"));
                return true;
            }
            target.setHealth(0.0);
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Killed " + target.getName() + ".</green>"));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can execute /kill.");
            return true;
        }

        player.setHealth(0.0);
        return true;
    }
}