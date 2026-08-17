package no.hammers.anarchycore.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PingCommand extends Command {

    public PingCommand() {
        super("ping", "Check your or another player's latency.", "/ping [player]", List.of("latency"));
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        // 1. Check self ping (/ping)
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Console must specify a target player: /ping <player>");
                return true;
            }

            int ping = player.getPing();
            player.sendMessage(ChatColor.GRAY + "Your latency is " + ChatColor.GREEN + ping + "ms" + ChatColor.GRAY + ".");
            return true;
        }

        // 2. Check another player's ping (/ping <player>)
        if (args.length == 1) {
            if (!sender.hasPermission("anarchycore.admin") && !sender.hasPermission("anarchycore.ping.others")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to check other players' ping.");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player '" + args[0] + "' is not online.");
                return true;
            }

            int ping = target.getPing();
            sender.sendMessage(ChatColor.GRAY + target.getName() + "'s latency is " + ChatColor.GREEN + ping + "ms" + ChatColor.GRAY + ".");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Usage: /ping [player]");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        if (args.length == 1 && (sender.hasPermission("anarchycore.admin") || sender.hasPermission("anarchycore.ping.others"))) {
            List<String> completions = new ArrayList<>();
            String currentArg = args[0].toLowerCase();

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(currentArg)) {
                    completions.add(player.getName());
                }
            }
            return completions;
        }
        return List.of();
    }
}
