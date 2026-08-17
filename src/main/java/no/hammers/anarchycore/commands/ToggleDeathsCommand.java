package no.hammers.anarchycore.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import no.hammers.anarchycore.AnarchyCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ToggleDeathsCommand extends Command {

    private final AnarchyCore plugin;

    public ToggleDeathsCommand(AnarchyCore plugin) {
        super("toggledeaths", "Toggle receiving death messages.", "/toggledeaths", List.of("deathmessages", "toggledeathmessages"));
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can toggle death messages.");
            return true;
        }

        boolean nowEnabled = plugin.toggleDeaths(player.getUniqueId());
        String msg = nowEnabled ? "<gray>Death messages <green>ENABLED</green>.</gray>" : "<gray>Death messages <red>DISABLED</red>.</gray>";
        player.sendMessage(MiniMessage.miniMessage().deserialize(msg));
        return true;
    }
}