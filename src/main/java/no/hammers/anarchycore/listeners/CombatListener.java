package no.hammers.anarchycore.listeners;

import net.kyori.adventure.text.minimessage.MiniMessage;
import no.hammers.anarchycore.AnarchyCore;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class CombatListener implements Listener {

    private final AnarchyCore plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public CombatListener(AnarchyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPvPDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Player attacker = null;

        if (event.getDamager() instanceof Player directAttacker) {
            attacker = directAttacker;
        } else if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
            attacker = shooter;
        }

        if (attacker != null && !attacker.equals(victim)) {
            plugin.tagCombat(victim);
            plugin.tagCombat(attacker);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (plugin.isCombatTagged(player)) {
            String rawCmd = event.getMessage().toLowerCase().split(" ")[0];
            String cmd = rawCmd.startsWith("/") ? rawCmd.substring(1) : rawCmd;

            List<String> blockedCmds = plugin.getConfig().getStringList("blocked-combat-commands");
            if (blockedCmds.contains(cmd)) {
                event.setCancelled(true);
                long seconds = plugin.getCombatTimeRemaining(player);
                player.sendMessage(miniMessage.deserialize(
                        "<red>You cannot use /" + cmd + " while in combat! (<yellow>" + seconds + "s</yellow> remaining)</red>"
                ));
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (plugin.isCombatTagged(player)) {
            plugin.markCombatLogDeath(player.getUniqueId());
            plugin.removeCombatTag(player);

            // Execute player on combat log (drops items and triggers DeathListener)
            player.setHealth(0.0);
        }
    }
}