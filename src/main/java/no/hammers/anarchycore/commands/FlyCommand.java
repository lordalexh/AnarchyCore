package no.hammers.anarchycore.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class FlyCommand extends Command {
    public FlyCommand() {
        super("fly", "Toggle flight", "/fly [player]", List.of());
        setPermission("anarchycore.admin");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!testPermission(sender)) return true;
        
        Player target;
        if (args.length == 0) {
            if (!(sender instanceof Player)) return true;
            target = (Player) sender;
        } else {
            target = Bukkit.getPlayer(args[0]);
        }
        
        if (target == null) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Player not found.</red>"));
            return true;
        }
        
        target.setAllowFlight(!target.getAllowFlight());
        if (target.getAllowFlight()) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Flight enabled for " + target.getName() + ".</green>"));
        } else {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Flight disabled for " + target.getName() + ".</red>"));
        }
        return true;
    }
}
