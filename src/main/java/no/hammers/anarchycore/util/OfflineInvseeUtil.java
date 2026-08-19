package no.hammers.anarchycore.util;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTCompoundList;
import de.tr7zw.changeme.nbtapi.NBTFile;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.UUID;

public class OfflineInvseeUtil {

    public static File getPlayerDataFile(UUID uuid) {
        File worldFolder = new File(Bukkit.getServer().getWorldContainer(), Bukkit.getWorlds().get(0).getName());
        
        File f1 = new File(worldFolder, String.format("playerdata/%s.dat", uuid));
        if (f1.exists()) return f1;
        
        File f2 = new File(worldFolder, String.format("players/data/%s.dat", uuid));
        if (f2.exists()) return f2;
        
        // Default to playerdata if neither exists (for saving new files, though unlikely for invsee)
        return f1;
    }

    public static ItemStack[] loadOfflineInventory(UUID uuid) throws Exception {
        File dataFile = getPlayerDataFile(uuid);
        if (!dataFile.exists()) return null;

        NBTFile nbtFile = new NBTFile(dataFile);
        NBTCompoundList inventoryList = nbtFile.getCompoundList("Inventory");
        
        ItemStack[] items = new ItemStack[45];
        
        for (de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT itemComp : inventoryList) {
            byte slot = itemComp.getByte("Slot");
            int bukkit = nbtSlotToBukkit(slot);
            if (bukkit < 0 || bukkit > 40) continue;
            
            ItemStack item = NBTItem.convertNBTtoItem((NBTCompound) itemComp);
            if (item != null) {
                items[bukkit] = item;
            }
        }
        
        return items;
    }

    public static void saveOfflineInventory(UUID uuid, ItemStack[] items) throws Exception {
        File dataFile = getPlayerDataFile(uuid);
        if (!dataFile.exists()) return;

        NBTFile nbtFile = new NBTFile(dataFile);
        
        // Remove old inventory
        nbtFile.removeKey("Inventory");
        NBTCompoundList newList = nbtFile.getCompoundList("Inventory");
        
        for (int bukkit = 0; bukkit <= 40; bukkit++) {
            ItemStack item = (bukkit < items.length) ? items[bukkit] : null;
            if (item == null || item.getType().isAir()) continue;
            
            int nbtSlot = bukkitSlotToNbt(bukkit);
            if (nbtSlot == Integer.MIN_VALUE) continue;
            
            NBTCompound itemNbt = NBTItem.convertItemtoNBT(item);
            if (itemNbt != null) {
                itemNbt.setByte("Slot", (byte) nbtSlot);
                newList.addCompound(itemNbt);
            }
        }
        
        nbtFile.save();
    }

    public static ItemStack[] loadOfflineEnderchest(UUID uuid) throws Exception {
        File dataFile = getPlayerDataFile(uuid);
        if (!dataFile.exists()) return null;

        NBTFile nbtFile = new NBTFile(dataFile);
        NBTCompoundList enderList = nbtFile.getCompoundList("EnderItems");
        
        ItemStack[] items = new ItemStack[27];
        
        for (de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT itemComp : enderList) {
            byte slot = itemComp.getByte("Slot");
            if (slot < 0 || slot >= 27) continue;
            
            ItemStack item = NBTItem.convertNBTtoItem((NBTCompound) itemComp);
            if (item != null) {
                items[slot] = item;
            }
        }
        
        return items;
    }

    public static void saveOfflineEnderchest(UUID uuid, ItemStack[] items) throws Exception {
        File dataFile = getPlayerDataFile(uuid);
        if (!dataFile.exists()) return;

        NBTFile nbtFile = new NBTFile(dataFile);
        
        nbtFile.removeKey("EnderItems");
        NBTCompoundList newList = nbtFile.getCompoundList("EnderItems");
        
        for (int i = 0; i < 27; i++) {
            ItemStack item = (i < items.length) ? items[i] : null;
            if (item == null || item.getType().isAir()) continue;
            
            NBTCompound itemNbt = NBTItem.convertItemtoNBT(item);
            if (itemNbt != null) {
                itemNbt.setByte("Slot", (byte) i);
                newList.addCompound(itemNbt);
            }
        }
        
        nbtFile.save();
    }

    private static int nbtSlotToBukkit(byte nbt) {
        if (nbt >= 0 && nbt <= 35) return nbt;
        return switch (nbt) {
            case 100  -> 36;
            case 101  -> 37;
            case 102  -> 38;
            case 103  -> 39;
            case -106 -> 40;
            default   -> -1;
        };
    }

    private static int bukkitSlotToNbt(int bukkit) {
        if (bukkit >= 0 && bukkit <= 35) return bukkit;
        return switch (bukkit) {
            case 36 -> 100;
            case 37 -> 101;
            case 38 -> 102;
            case 39 -> 103;
            case 40 -> -106;
            default -> Integer.MIN_VALUE;
        };
    }
}
