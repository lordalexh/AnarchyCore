package no.hammers.anarchycore.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class GamemodeCommand extends Command {
    public GamemodeCommand() {
        super("gm", "Change gamemode", "/gm <mode> [player]", List.of("gmc", "gms", "gma", "gmsp"));
        setPermission("anarchycore.admin");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!testPermission(sender)) return true;
        
        Player target = null;
        GameMode mode = null;
        
        if (label.equalsIgnoreCase("gmc")) mode = GameMode.CREATIVE;
        else if (label.equalsIgnoreCase("gms")) mode = GameMode.SURVIVAL;
        else if (label.equalsIgnoreCase("gma")) mode = GameMode.ADVENTURE;
        else if (label.equalsIgnoreCase("gmsp")) mode = GameMode.SPECTATOR;
        
        int pIndex = 0;
        if (mode == null) {
            if (args.length == 0) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Usage: /gm <0/1/2/3> [player]</red>"));
                return true;
            }
            switch (args[0].toLowerCase()) {
                case "0": case "survival": mode = GameMode.SURVIVAL; break;
                case "1": case "creative": mode = GameMode.CREATIVE; break;
                case "2": case "adventure": mode = GameMode.ADVENTURE; break;
                case "3": case "spectator": mode = GameMode.SPECTATOR; break;
                default: 
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Invalid mode.</red>"));
                    return true;
            }
            pIndex = 1;
        }
        
        if (args.length > pIndex) {
            target = Bukkit.getPlayer(args[pIndex]);
        } else if (sender instanceof Player) {
            target = (Player) sender;
        }
        
        if (target == null) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Player not found.</red>"));
            return true;
        }
        
        target.setGameMode(mode);
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Set gamemode of " + target.getName() + " to " + mode.name() + ".</green>"));
        return true;
    }
}
