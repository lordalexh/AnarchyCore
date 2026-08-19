package no.hammers.anarchycore.listeners;

import net.kyori.adventure.text.minimessage.MiniMessage;
import no.hammers.anarchycore.AnarchyCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class FreezeListener implements Listener {

    private final AnarchyCore plugin;

    public FreezeListener(AnarchyCore plugin) {
        this.plugin = plugin;
    }

    private boolean isFrozen(Player player) {
        return plugin.getFrozenPlayers().contains(player.getUniqueId());
    }

    private void sendFrozenMessage(Player player) {
        player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You are frozen and cannot do this!</red>"));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (isFrozen(event.getPlayer())) {
            if (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ()) {
                event.setTo(event.getFrom());
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (isFrozen(event.getPlayer())) {
            event.setCancelled(true);
            sendFrozenMessage(event.getPlayer());
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isFrozen(event.getPlayer())) {
            event.setCancelled(true);
            sendFrozenMessage(event.getPlayer());
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player && isFrozen(player)) {
            event.setCancelled(true);
            sendFrozenMessage(player);
        }
    }
}
