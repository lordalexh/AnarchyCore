package no.hammers.anarchycore.util;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Marks a virtual inventory as an offline-invsee session,
 * so InvseeListener can save items back on close.
 */
public class OfflineInvseeHolder implements InventoryHolder {

    private final UUID targetUuid;
    private final String targetName;
    private Inventory inventory;

    public OfflineInvseeHolder(UUID targetUuid, String targetName) {
        this.targetUuid = targetUuid;
        this.targetName = targetName;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
