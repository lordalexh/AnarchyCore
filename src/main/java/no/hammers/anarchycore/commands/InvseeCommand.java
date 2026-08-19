package no.hammers.anarchycore.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import no.hammers.anarchycore.util.OfflineInvseeHolder;
import no.hammers.anarchycore.util.OfflineInvseeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class InvseeCommand extends Command {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public InvseeCommand() {
        super("invsee");
        this.setPermission("anarchycore.invsee");
        this.setUsage("/invsee <player>");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("anarchycore.invsee")) {
            player.sendMessage(miniMessage.deserialize("<red>No permission.</red>"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(miniMessage.deserialize("<red>Usage: /invsee <player></red>"));
            return true;
        }

        // 1) Try online player first
        Player onlineTarget = Bukkit.getPlayer(args[0]);
        if (onlineTarget != null) {
            player.openInventory(onlineTarget.getInventory());
            player.sendMessage(miniMessage.deserialize(String.format("<green>Opening inventory of %s (online).</green>", onlineTarget.getName())));
            return true;
        }

        // 2) Fall back to offline player

        @SuppressWarnings("deprecation")
        OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(args[0]);
        if (!offlineTarget.hasPlayedBefore()) {
            player.sendMessage(miniMessage.deserialize("<red>Player has never joined this server.</red>"));
            return true;
        }

        try {
            ItemStack[] items = OfflineInvseeUtil.loadOfflineInventory(offlineTarget.getUniqueId());
            if (items == null) {
                player.sendMessage(miniMessage.deserialize("<red>No player data found.</red>"));
                return true;
            }

            String name = offlineTarget.getName() != null ? offlineTarget.getName() : args[0];

            // Create a tagged virtual inventory
            OfflineInvseeHolder holder = new OfflineInvseeHolder(offlineTarget.getUniqueId(), name);
            Inventory inv = Bukkit.createInventory(holder, 45, miniMessage.deserialize(String.format("<dark_gray>Player: %s</dark_gray>", name)));
            holder.setInventory(inv);

            // Populate slots 0-40 with player items
            for (int i = 0; i <= 40 && i < items.length; i++) {
                if (items[i] != null) {
                    inv.setItem(i, items[i]);
                }
            }

            // Fill decoration slots 41-44 with gray glass panes
            ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = filler.getItemMeta();
            if (meta != null) {
                meta.displayName(miniMessage.deserialize("<dark_gray> </dark_gray>"));
                filler.setItemMeta(meta);
            }
            for (int i = 41; i < 45; i++) {
                inv.setItem(i, filler);
            }

            player.openInventory(inv);
            player.sendMessage(miniMessage.deserialize(String.format("<green>Opening offline inventory of %s.</green>", name)));
        } catch (Exception e) {
            player.sendMessage(miniMessage.deserialize(String.format("<red>Failed to load offline inventory: %s</red>", e.getMessage())));
            e.printStackTrace();
        }

        return true;
    }
}
