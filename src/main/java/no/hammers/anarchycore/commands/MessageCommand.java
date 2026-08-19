package no.hammers.anarchycore.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import no.hammers.anarchycore.AnarchyCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class MessageCommand extends Command {

    private final AnarchyCore plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MessageCommand(AnarchyCore plugin) {
        super("msg", "Send a private message.", "/msg <player> <message>", List.of("tell", "w", "whisper"));
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can send private messages.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(miniMessage.deserialize("<red>Usage: /msg <player> <message></red>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(miniMessage.deserialize("<red>Player not found.</red>"));
            return true;
        }

        String body = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        plugin.setReplyTarget(player.getUniqueId(), target.getUniqueId());

        player.sendMessage(miniMessage.deserialize("<gray>to <light_purple><target></light_purple>: <text></gray>",
                Placeholder.parsed("target", target.getName()), Placeholder.parsed("text", body)));

        target.sendMessage(miniMessage.deserialize("<gray>from <light_purple><sender></light_purple>: <text></gray>",
                Placeholder.parsed("sender", player.getName()), Placeholder.parsed("text", body)));

        for (java.util.UUID spyId : plugin.getSocialSpyEnabled()) {
            if (spyId.equals(player.getUniqueId()) || spyId.equals(target.getUniqueId())) continue;
            Player spy = Bukkit.getPlayer(spyId);
            if (spy != null && spy.isOnline()) {
                spy.sendMessage(miniMessage.deserialize("<dark_gray>[Spy] " + player.getName() + " -> " + target.getName() + ": " + body + "</dark_gray>"));
            }
        }

        return true;
    }
}