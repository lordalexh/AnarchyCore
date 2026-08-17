package no.hammers.anarchycore.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import no.hammers.anarchycore.AnarchyCore;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

public class DeathListener implements Listener {

    private final AnarchyCore plugin;
    private final Random random = new Random();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public DeathListener(AnarchyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.deathMessage(null); // Suppress default system message

        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        EntityDamageEvent lastDamage = victim.getLastDamageCause();

        String rawTemplate;

        // 0. Combat Log Death
        if (plugin.isCombatLogDeath(victim.getUniqueId())) {
            plugin.clearCombatLogDeath(victim.getUniqueId());

            List<String> combatLogMessages = plugin.getConfig().getStringList("messages.combat-log");
            rawTemplate = getRandomMessage(combatLogMessages, "<gray><red><victim></red> logged out in combat and was slain.</gray>");

            broadcast(miniMessage.deserialize(rawTemplate, Placeholder.parsed("victim", victim.getName())));
            return;
        }

        // 1. PvP Death
        if (killer != null && !killer.equals(victim)) {
            ItemStack item = killer.getInventory().getItemInMainHand();
            String weaponName = getFormattedItemName(item);
            List<String> pvpMessages = plugin.getConfig().getStringList("messages.pvp");
            rawTemplate = getRandomMessage(pvpMessages, "<red><victim></red> was slain by <green><killer></green>");

            broadcast(miniMessage.deserialize(rawTemplate,
                    Placeholder.parsed("victim", victim.getName()),
                    Placeholder.parsed("killer", killer.getName()),
                    Placeholder.parsed("weapon", weaponName)
            ));
            return;
        }

        // 2. Direct Suicide
        if (killer != null && killer.equals(victim)) {
            List<String> suicideMessages = plugin.getConfig().getStringList("messages.suicide");
            rawTemplate = getRandomMessage(suicideMessages, "<red><victim></red> killed themselves.");
            broadcast(miniMessage.deserialize(rawTemplate, Placeholder.parsed("victim", victim.getName())));
            return;
        }

        // 3. Mob Kills
        if (lastDamage instanceof EntityDamageByEntityEvent damageByEntity) {
            Entity damager = damageByEntity.getDamager();
            if (damager instanceof LivingEntity && !(damager instanceof Player)) {
                String mobName = getCleanName(damager.getType().name());
                List<String> mobMessages = plugin.getConfig().getStringList("messages.mobs.generic");
                rawTemplate = getRandomMessage(mobMessages, "<red><victim></red> was slain by <mob>.");

                broadcast(miniMessage.deserialize(rawTemplate,
                        Placeholder.parsed("victim", victim.getName()),
                        Placeholder.parsed("mob", mobName)
                ));
                return;
            }
        }

        // 4. Environmental Causes
        if (lastDamage != null) {
            String configKey = switch (lastDamage.getCause()) {
                case DROWNING -> "drowning";
                case FALL -> "fall";
                case FLY_INTO_WALL -> "kinetic";
                case LAVA -> "lava";
                case FIRE, FIRE_TICK -> "fire";
                case VOID -> "void";
                case STARVATION -> "starvation";
                case BLOCK_EXPLOSION, ENTITY_EXPLOSION -> "explosion";
                default -> null;
            };

            if (configKey != null) {
                List<String> envMessages = plugin.getConfig().getStringList("messages." + configKey);
                if (envMessages != null && !envMessages.isEmpty()) {
                    rawTemplate = getRandomMessage(envMessages, "<red><victim></red> died.");
                    broadcast(miniMessage.deserialize(rawTemplate, Placeholder.parsed("victim", victim.getName())));
                    return;
                }
            }
        }

        // 5. Generic Fallback
        List<String> genericMessages = plugin.getConfig().getStringList("messages.generic");
        rawTemplate = getRandomMessage(genericMessages, "<red><victim></red> died.");
        broadcast(miniMessage.deserialize(rawTemplate, Placeholder.parsed("victim", victim.getName())));
    }

    private void broadcast(Component message) {
        plugin.getServer().getConsoleSender().sendMessage(message);
        for (Player recipient : plugin.getServer().getOnlinePlayers()) {
            if (!plugin.isDeathsDisabled(recipient.getUniqueId())) {
                recipient.sendMessage(message);
            }
        }
    }

    private String getFormattedItemName(ItemStack item) {
        if (item == null || item.getType().isAir()) return "Fists";
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().displayName());
        }
        return getCleanName(item.getType().name());
    }

    private String getCleanName(String enumName) {
        String[] words = enumName.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private String getRandomMessage(List<String> list, String fallback) {
        if (list == null || list.isEmpty()) return fallback;
        return list.get(random.nextInt(list.size()));
    }
}