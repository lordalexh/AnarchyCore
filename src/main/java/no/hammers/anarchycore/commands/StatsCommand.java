package no.hammers.anarchycore.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import no.hammers.anarchycore.AnarchyCore;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StatsCommand extends Command {

    private final AnarchyCore plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public StatsCommand(AnarchyCore plugin) {
        super("stats", "Check player statistics.", "/stats [player]", List.of("playtime", "joindate"));
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        OfflinePlayer target;

        if (args.length > 0) {
            target = Bukkit.getOfflinePlayer(args[0]);
            if (!target.hasPlayedBefore() && !target.isOnline()) {
                sender.sendMessage(miniMessage.deserialize("<red>Player has never joined 2b2t.no.</red>"));
                return true;
            }
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            sender.sendMessage("Console must specify a player name: /stats <player>");
            return true;
        }

        // 1. Fetch Statistics
        long ticksPlayed = target.getStatistic(Statistic.PLAY_ONE_MINUTE);
        long totalSeconds = ticksPlayed / 20;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;

        int playerKills = target.getStatistic(Statistic.PLAYER_KILLS);
        int deaths = target.getStatistic(Statistic.DEATHS);
        int mobKills = target.getStatistic(Statistic.MOB_KILLS);

        double kdr = deaths == 0 ? playerKills : Math.round((double) playerKills / deaths * 100.0) / 100.0;
        int joinNumber = plugin.getConfig().getInt("players." + target.getUniqueId() + ".join-number", -1);

        // 2. Render Output
        sender.sendMessage(miniMessage.deserialize(
                "<gold><b>=== Statistics for " + target.getName() + " ===</b></gold>\n" +
                        "<gray>Unique Join ID: <white>#" + (joinNumber > 0 ? joinNumber : "Unknown") + "</white></gray>\n" +
                        "<gray>Playtime: <white>" + hours + "h " + minutes + "m</white></gray>\n" +
                        "<gray>Player Kills: <green>" + playerKills + "</green></gray>\n" +
                        "<gray>Deaths: <red>" + deaths + "</red></gray>\n" +
                        "<gray>K/D Ratio: <yellow>" + kdr + "</yellow></gray>\n" +
                        "<gray>Mob Kills: <white>" + mobKills + "</white></gray>"
        ));

        return true;
    }
}