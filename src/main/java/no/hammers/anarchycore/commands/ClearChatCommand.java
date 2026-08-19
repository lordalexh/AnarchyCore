package no.hammers.anarchycore.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class ClearChatCommand extends Command {
    public ClearChatCommand() {
        super("clearchat", "Clear the server chat", "/clearchat", List.of("cc"));
        setPermission("anarchycore.admin");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!testPermission(sender)) return true;
        for (int i = 0; i < 100; i++) {
            Bukkit.broadcastMessage("");
        }
        Bukkit.broadcast(MiniMessage.miniMessage().deserialize("<green>Chat has been cleared by an admin.</green>"));
        return true;
    }
}
