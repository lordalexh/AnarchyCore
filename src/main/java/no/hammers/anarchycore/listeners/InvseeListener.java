package no.hammers.anarchycore.listeners;

import net.kyori.adventure.text.minimessage.MiniMessage;
import no.hammers.anarchycore.util.OfflineInvseeHolder;
import no.hammers.anarchycore.util.OfflineInvseeUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Saves offline player inventory changes when an admin closes
 * the virtual invsee GUI.
 */
public class InvseeListener implements Listener {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        if (!(event.getPlayer() instanceof Player admin)) return;

        if (inv.getHolder() instanceof OfflineInvseeHolder invHolder) {
            try {
                ItemStack[] contents = inv.getContents();
                OfflineInvseeUtil.saveOfflineInventory(invHolder.getTargetUuid(), contents);
                admin.sendMessage(miniMessage.deserialize(
                        "<green>Saved offline inventory of " + invHolder.getTargetName() + ".</green>"));
            } catch (Exception e) {
                admin.sendMessage(miniMessage.deserialize(
                        "<red>Failed to save offline inventory: " + e.getMessage() + "</red>"));
                e.printStackTrace();
            }
        } else if (inv.getHolder() instanceof no.hammers.anarchycore.util.OfflineEnderchestHolder ecHolder) {
            try {
                ItemStack[] contents = inv.getContents();
                OfflineInvseeUtil.saveOfflineEnderchest(ecHolder.getTargetUuid(), contents);
                admin.sendMessage(miniMessage.deserialize(
                        "<green>Saved offline enderchest of " + ecHolder.getTargetName() + ".</green>"));
            } catch (Exception e) {
                admin.sendMessage(miniMessage.deserialize(
                        "<red>Failed to save offline enderchest: " + e.getMessage() + "</red>"));
                e.printStackTrace();
            }
        }
    }
}
