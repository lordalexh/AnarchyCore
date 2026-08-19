package no.hammers.anarchycore.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import no.hammers.anarchycore.AnarchyCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class SocialSpyCommand extends Command {
    private final AnarchyCore plugin;
    public SocialSpyCommand(AnarchyCore plugin) {
        super("socialspy", "Toggle spy on private messages", "/socialspy", List.of("spy"));
        this.plugin = plugin;
        setPermission("anarchycore.admin");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!testPermission(sender)) return true;
        if (!(sender instanceof Player player)) return true;
        
        if (plugin.getSocialSpyEnabled().contains(player.getUniqueId())) {
            plugin.getSocialSpyEnabled().remove(player.getUniqueId());
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>SocialSpy disabled.</red>"));
        } else {
            plugin.getSocialSpyEnabled().add(player.getUniqueId());
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>SocialSpy enabled.</green>"));
        }
        return true;
    }
}
