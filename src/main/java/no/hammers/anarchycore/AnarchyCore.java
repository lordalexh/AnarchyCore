package no.hammers.anarchycore;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import net.kyori.adventure.text.minimessage.MiniMessage;
import no.hammers.anarchycore.commands.*;
import no.hammers.anarchycore.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import no.hammers.anarchycore.database.DatabaseManager;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class AnarchyCore extends JavaPlugin {

    private final Set<UUID> disabledDeaths = ConcurrentHashMap.newKeySet();
    private final Map<UUID, UUID> replyMap = new ConcurrentHashMap<>();
    private final Map<UUID, Long> combatTags = new ConcurrentHashMap<>();
    private final Set<UUID> combatLogDeaths = ConcurrentHashMap.newKeySet();
    private final Set<UUID> pendingRelogNotices = ConcurrentHashMap.newKeySet();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final AtomicInteger uniquePlayers = new AtomicInteger(0);
    private final Set<UUID> vanishedPlayers = ConcurrentHashMap.newKeySet();
    private DatabaseManager databaseManager;

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public boolean isVanished(UUID uuid) {
        return vanishedPlayers.contains(uuid);
    }
    
    public void setVanished(Player player, boolean vanished) {
        if (vanished) {
            vanishedPlayers.add(player.getUniqueId());
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.hasPermission("anarchycore.vanish.see")) {
                    online.hidePlayer(this, player);
                }
            }
        } else {
            vanishedPlayers.remove(player.getUniqueId());
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.showPlayer(this, player);
            }
        }
    }

    @Override
    public void onLoad() {
        // Build and load PacketEvents before plugins enable
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().checkForUpdates(false);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.initialize();

        // Ensure join counter syncs with existing players in config.yml on startup
        loadUniquePlayerCount();

        // Initialize PacketEvents & Register Network Listener
        PacketEvents.getAPI().init();
        PacketEvents.getAPI().getEventManager().registerListener(new PacketSecurityListener());

        // Register Commands with Forced Override for built-in Paper commands
        registerCommand(new HelpCommand(this));
        registerCommand(new SuicideCommand());
        registerCommand(new ToggleDeathsCommand(this));
        registerCommand(new PingCommand());
        registerCommand(new TpsCommand());
        registerCommand(new MessageCommand(this));
        registerCommand(new ReplyCommand(this));
        registerCommand(new StatsCommand(this));
        registerCommand(new MuteCommand(this));
        registerCommand(new UnmuteCommand(this));
        registerCommand(new BanCommand(this));
        registerCommand(new UnbanCommand(this));
        registerCommand(new KickCommand());
        registerCommand(new VanishCommand(this));
        registerCommand(new InvseeCommand());
        registerCommand(new EnderchestCommand());
        registerCommand(new AnarchyReloadCommand(this));

        // Register Event Listeners
        var pm = getServer().getPluginManager();
        pm.registerEvents(new SpawnListener(this), this);
        pm.registerEvents(new DeathListener(this), this);
        pm.registerEvents(new ChatListener(this), this);
        pm.registerEvents(new JoinListener(this), this);
        pm.registerEvents(new BanListener(this), this);
        pm.registerEvents(new ItemCleanerListener(this), this);
        pm.registerEvents(new SecurityListener(), this);
        pm.registerEvents(new CombatListener(this), this);

        // Start dynamic tablist & combat actionbar updater
        startTablistUpdater();

        getLogger().info("AnarchyCore fully initialized for 2b2t.no!");
    }

    @Override
    public void onDisable() {
        // Terminate PacketEvents pipeline cleanly on shutdown
        PacketEvents.getAPI().terminate();
        
        if (this.databaseManager != null) {
            this.databaseManager.close();
        }
    }

    /**
     * Scans config.yml on startup to seed the highest existing join number.
     * Prevents duplicate join-number #1 and #2 on server restarts.
     */
    private void loadUniquePlayerCount() {
        int max = getConfig().getInt("unique-player-count", 0);

        if (getConfig().isConfigurationSection("players")) {
            for (String uuidStr : getConfig().getConfigurationSection("players").getKeys(false)) {
                int joinNum = getConfig().getInt("players." + uuidStr + ".join-number", 0);
                if (joinNum > max) {
                    max = joinNum;
                }
            }
        }
        
        int dbMax = this.databaseManager.getMaxJoinNumber();
        if (dbMax > max) {
            max = dbMax;
        }

        uniquePlayers.set(max);
        getConfig().set("unique-player-count", max);
        saveConfig();
    }

    private void registerCommand(Command command) {
        var commandMap = getServer().getCommandMap();
        commandMap.register("anarchycore", command);

        // Override Paper/Vanilla built-in commands in knownCommands map
        if (commandMap instanceof SimpleCommandMap simpleMap) {
            Map<String, Command> knownCommands = simpleMap.getKnownCommands();
            knownCommands.put(command.getName().toLowerCase(), command);
            knownCommands.put("anarchycore:" + command.getName().toLowerCase(), command);

            for (String alias : command.getAliases()) {
                knownCommands.put(alias.toLowerCase(), command);
                knownCommands.put("anarchycore:" + alias.toLowerCase(), command);
            }
        }
    }

    private void startTablistUpdater() {
        getServer().getAsyncScheduler().runAtFixedRate(this, task -> {
            double rawMspt = fetchMspt();
            double roundedMspt = Math.round(rawMspt * 10.0) / 10.0;
            String msptFormatted = roundedMspt + "ms";

            for (Player player : getServer().getOnlinePlayers()) {
                player.getScheduler().run(this, scheduledTask -> {
                    updatePlayerTablist(player, msptFormatted);
                    updateCombatActionBar(player);
                }, null);
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void markCombatLogDeath(UUID uuid) {
        combatLogDeaths.add(uuid);
        pendingRelogNotices.add(uuid);
    }

    public boolean isCombatLogDeath(UUID uuid) {
        return combatLogDeaths.contains(uuid);
    }

    public void clearCombatLogDeath(UUID uuid) {
        combatLogDeaths.remove(uuid);
    }

    public boolean checkAndRemoveRelogNotice(UUID uuid) {
        return pendingRelogNotices.remove(uuid);
    }

    public void tagCombat(Player player) {
        int combatSeconds = getConfig().getInt("combat-tag-seconds", 15);
        boolean wasTagged = isCombatTagged(player);

        combatTags.put(player.getUniqueId(), System.currentTimeMillis() + (combatSeconds * 1000L));

        if (!wasTagged) {
            player.sendMessage(miniMessage.deserialize("<red>You are now in combat! Do not log out or use commands.</red>"));
        }
    }

    public boolean isCombatTagged(Player player) {
        Long expireTime = combatTags.get(player.getUniqueId());
        if (expireTime == null) return false;
        if (System.currentTimeMillis() >= expireTime) {
            combatTags.remove(player.getUniqueId());
            player.sendMessage(miniMessage.deserialize("<green>You are no longer in combat.</green>"));
            return false;
        }
        return true;
    }

    public long getCombatTimeRemaining(Player player) {
        Long expireTime = combatTags.get(player.getUniqueId());
        if (expireTime == null) return 0;
        long remaining = (expireTime - System.currentTimeMillis()) / 1000L;
        return Math.max(0, remaining);
    }

    public void removeCombatTag(Player player) {
        combatTags.remove(player.getUniqueId());
    }

    private void updateCombatActionBar(Player player) {
        if (isCombatTagged(player)) {
            long remaining = getCombatTimeRemaining(player);
            player.sendActionBar(miniMessage.deserialize(
                    "<red><b>IN COMBAT</b></red> <gray>-</gray> <yellow>" + remaining + "s</yellow> <gray>remaining</gray>"
            ));
        }
    }

    public void updatePlayerTablist(Player player, String msptFormatted) {
        if (!player.isOnline()) return;

        double[] regionTps = fetchRegionTps(player.getLocation());
        double rawTps = (regionTps != null && regionTps.length > 0) ? regionTps[0] : 20.0;
        double roundedTps = Math.min(20.0, Math.round(rawTps * 10.0) / 10.0);

        String tpsColorTag = roundedTps >= 18.0 ? "green" : (roundedTps >= 13.0 ? "yellow" : "red");
        String pingColorTag = player.getPing() < 80 ? "green" : (player.getPing() < 180 ? "yellow" : "red");

        String headerRaw = getConfig().getString("tablist.header", "<gold><b>2B2T.NO</b></gold>\n<gray>The Nordic Anarchy Server</gray>");
        String footerTemplate = getConfig().getString("tablist.footer", "<gray>Ping: <ping> | TPS: <tps> <dark_gray>(<mspt>)</dark_gray></gray>");

        String pingFormatted = "<" + pingColorTag + ">" + player.getPing() + "ms</" + pingColorTag + ">";
        String tpsFormatted = "<" + tpsColorTag + ">" + roundedTps + "</" + tpsColorTag + ">";

        String footerRaw = footerTemplate
                .replace("<ping>", pingFormatted)
                .replace("<tps>", tpsFormatted)
                .replace("<mspt>", msptFormatted);

        try {
            player.sendPlayerListHeaderAndFooter(
                    miniMessage.deserialize(headerRaw),
                    miniMessage.deserialize(footerRaw)
            );
        } catch (Exception e) {
            getLogger().warning("Failed to update tablist for " + player.getName() + ": " + e.getMessage());
        }
    }

    public boolean isOG(Player player) {
        if (player.hasPermission("anarchycore.og")) return true;

        var ogList = getConfig().getStringList("og-players");
        if (ogList.contains(player.getName()) || ogList.contains(player.getUniqueId().toString())) return true;

        int ogThreshold = getConfig().getInt("og-threshold", 0);
        if (ogThreshold > 0) {
            int joinNumber = databaseManager.getJoinNumber(player.getUniqueId());
            if (joinNumber == -1) {
                // Fallback to config if not migrated
                joinNumber = getConfig().getInt("players." + player.getUniqueId() + ".join-number", -1);
            }
            if (joinNumber > 0 && joinNumber <= ogThreshold) return true;
        }

        return false;
    }

    private Method regionTpsMethod = null;
    private boolean regionTpsMethodFetched = false;

    private double[] fetchRegionTps(Location loc) {
        if (loc == null) return null;
        try {
            if (!regionTpsMethodFetched) {
                try {
                    regionTpsMethod = getServer().getClass().getMethod("getRegionTPS", Location.class);
                } catch (NoSuchMethodException ignored) {}
                regionTpsMethodFetched = true;
            }
            if (regionTpsMethod != null) {
                Object result = regionTpsMethod.invoke(getServer(), loc);
                if (result instanceof double[] array && array.length > 0) return array;
            }
        } catch (Throwable ignored) {}
        return Bukkit.getTPS();
    }

    private double fetchMspt() {
        try {
            return Bukkit.getAverageTickTime();
        } catch (Throwable ignored) {
            return 0.0;
        }
    }

    public boolean isDeathsDisabled(UUID uuid) {
        return disabledDeaths.contains(uuid);
    }

    public boolean toggleDeaths(UUID uuid) {
        if (disabledDeaths.contains(uuid)) {
            disabledDeaths.remove(uuid);
            return true;
        } else {
            disabledDeaths.add(uuid);
            return false;
        }
    }

    public void setReplyTarget(UUID sender, UUID target) {
        replyMap.put(sender, target);
        replyMap.put(target, sender);
    }

    public UUID getReplyTarget(UUID sender) {
        return replyMap.get(sender);
    }

    public synchronized int incrementUniquePlayers() {
        int count = uniquePlayers.incrementAndGet();
        getConfig().set("unique-player-count", count);
        saveConfig();
        return count;
    }
}