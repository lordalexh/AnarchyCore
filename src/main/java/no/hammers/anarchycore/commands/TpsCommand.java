package no.hammers.anarchycore.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.List;

public class TpsCommand extends Command {

    public TpsCommand() {
        super("tps", "Check server performance.", "/tps", List.of("ticks"));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        double[] tps = null;

        if (sender instanceof Player player) {
            tps = fetchRegionTps(player.getLocation());
        }

        if (tps == null || tps.length == 0) {
            tps = Bukkit.getTPS();
        }

        String tps1 = formatTps(tps[0]);
        String tps2 = tps.length > 1 ? formatTps(tps[1]) : tps1;
        String tps3 = tps.length > 2 ? formatTps(tps[2]) : tps2;

        sender.sendMessage(MiniMessage.miniMessage().deserialize(
                "<gray>Region TPS (5s, 15s, 1m): " + tps1 + "<gray>, </gray>" + tps2 + "<gray>, </gray>" + tps3 + "</gray>"
        ));
        return true;
    }

    private double[] fetchRegionTps(Location loc) {
        if (loc == null) return null;
        try {
            Method method = Bukkit.getServer().getClass().getMethod("getRegionTPS", Location.class);
            Object result = method.invoke(Bukkit.getServer(), loc);
            if (result instanceof double[] array && array.length > 0) {
                return array;
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private String formatTps(double tps) {
        double rounded = Math.min(20.0, Math.round(tps * 100.0) / 100.0);
        String tag = rounded >= 18.0 ? "green" : (rounded >= 13.0 ? "yellow" : "red");
        return "<" + tag + ">" + rounded + "</" + tag + ">";
    }
}