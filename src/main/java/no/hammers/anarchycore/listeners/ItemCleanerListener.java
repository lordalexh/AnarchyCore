package no.hammers.anarchycore.listeners;

import no.hammers.anarchycore.AnarchyCore;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class ItemCleanerListener implements Listener {

    private final Set<Material> illegalMaterials = new HashSet<>();

    public ItemCleanerListener(AnarchyCore plugin) {
        for (String matName : plugin.getConfig().getStringList("illegal-items")) {
            try {
                illegalMaterials.add(Material.valueOf(matName.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        cleanInventory(event.getPlayer());
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack item = event.getItem().getItemStack();
            if (isIllegal(item)) {
                event.setCancelled(true);
                event.getItem().remove();
                player.sendMessage("§cIllegal item removed.");
            }
        }
    }

    private void cleanInventory(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isIllegal(item)) {
                player.getInventory().remove(item);
            }
        }
    }

    private boolean isIllegal(ItemStack item) {
        if (item == null) return false;
        return illegalMaterials.contains(item.getType()) || item.getAmount() > item.getMaxStackSize();
    }
}