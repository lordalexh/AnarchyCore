package no.hammers.anarchycore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class EnderchestCommand extends Command {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public EnderchestCommand() {
        super("enderchest");
        this.setPermission("anarchycore.enderchest");
        this.setUsage("/enderchest <player>");
        this.setAliases(java.util.List.of("ec"));
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("anarchycore.enderchest")) {
            player.sendMessage(miniMessage.deserialize("<red>No permission.</red>"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(miniMessage.deserialize("<red>Usage: /enderchest <player></red>"));
            return true;
        }

        Player onlineTarget = Bukkit.getPlayer(args[0]);
        if (onlineTarget != null) {
            player.openInventory(onlineTarget.getEnderChest());
            player.sendMessage(miniMessage.deserialize(String.format("<green>Opening enderchest of %s (online).</green>", onlineTarget.getName())));
            return true;
        }

        @SuppressWarnings("deprecation")
        org.bukkit.OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(args[0]);
        if (!offlineTarget.hasPlayedBefore()) {
            player.sendMessage(miniMessage.deserialize("<red>Player has never joined this server.</red>"));
            return true;
        }

        try {
            org.bukkit.inventory.ItemStack[] items = no.hammers.anarchycore.util.OfflineInvseeUtil.loadOfflineEnderchest(offlineTarget.getUniqueId());
            if (items == null) {
                player.sendMessage(miniMessage.deserialize("<red>No player data found.</red>"));
                return true;
            }

            String name = offlineTarget.getName() != null ? offlineTarget.getName() : args[0];

            no.hammers.anarchycore.util.OfflineEnderchestHolder holder = new no.hammers.anarchycore.util.OfflineEnderchestHolder(offlineTarget.getUniqueId(), name);
            org.bukkit.inventory.Inventory inv = Bukkit.createInventory(holder, 27, miniMessage.deserialize(String.format("<dark_gray>Player: %s</dark_gray>", name)));
            holder.setInventory(inv);

            for (int i = 0; i < 27 && i < items.length; i++) {
                if (items[i] != null) {
                    inv.setItem(i, items[i]);
                }
            }

            player.openInventory(inv);
            player.sendMessage(miniMessage.deserialize(String.format("<green>Opening offline enderchest of %s.</green>", name)));
        } catch (Exception e) {
            player.sendMessage(miniMessage.deserialize(String.format("<red>Failed to load offline enderchest: %s</red>", e.getMessage())));
            e.printStackTrace();
        }
        
        return true;
    }
}
