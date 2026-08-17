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
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can execute /kill.");
            return true;
        }

        player.setHealth(0.0);
        return true;
    }
}