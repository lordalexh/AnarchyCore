package no.hammers.anarchycore.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PingCommand extends Command {

    public PingCommand() {
        super("ping", "Check your connection ping.", "/ping", List.of("latency"));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can execute /ping.");
            return true;
        }

        int ping = player.getPing();
        String tag = ping < 80 ? "green" : (ping < 180 ? "yellow" : "red");

        player.sendMessage(MiniMessage.miniMessage().deserialize(
                "<gray>Your ping is <" + tag + ">" + ping + "ms</" + tag + ">.</gray>"
        ));
        return true;
    }
}