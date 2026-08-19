package no.hammers.anarchycore.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import no.hammers.anarchycore.AnarchyCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class ReplyCommand extends Command {

    private final AnarchyCore plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ReplyCommand(AnarchyCore plugin) {
        super("r", "Reply to the last private message.", "/r <message>", List.of("reply"));
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can reply.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(miniMessage.deserialize("<red>Usage: /r <message></red>"));
            return true;
        }

        UUID targetUUID = plugin.getReplyTarget(player.getUniqueId());
        if (targetUUID == null) {
            player.sendMessage(miniMessage.deserialize("<red>You have nobody to reply to.</red>"));
            return true;
        }

        Player target = Bukkit.getPlayer(targetUUID);
        if (target == null || !target.isOnline()) {
            player.sendMessage(miniMessage.deserialize("<red>That player is no longer online.</red>"));
            return true;
        }

        String body = String.join(" ", args);

        if (plugin.isIgnoring(target.getUniqueId(), player.getUniqueId())) {
            player.sendMessage(miniMessage.deserialize("<red>This player is ignoring you.</red>"));
            return true;
        }

        plugin.setReplyTarget(player.getUniqueId(), target.getUniqueId());

        player.sendMessage(miniMessage.deserialize("<gray>to <light_purple><target></light_purple>: <text></gray>",
                Placeholder.parsed("target", target.getName()), Placeholder.parsed("text", body)));

        target.sendMessage(miniMessage.deserialize("<gray>from <light_purple><sender></light_purple>: <text></gray>",
                Placeholder.parsed("sender", player.getName()), Placeholder.parsed("text", body)));

        for (UUID spyId : plugin.getSocialSpyEnabled()) {
            if (spyId.equals(player.getUniqueId()) || spyId.equals(target.getUniqueId())) continue;
            Player spy = Bukkit.getPlayer(spyId);
            if (spy != null && spy.isOnline()) {
                spy.sendMessage(miniMessage.deserialize("<dark_gray>[Spy] " + player.getName() + " -> " + target.getName() + ": " + body + "</dark_gray>"));
            }
        }

        return true;
    }
}