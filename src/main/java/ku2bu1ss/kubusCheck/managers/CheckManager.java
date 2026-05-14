package ku2bu1ss.kubusCheck.managers;

import ku2bu1ss.kubusCheck.KubusCheck;
import ku2bu1ss.kubusCheck.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CheckManager {

    private final KubusCheck plugin;
    private final Map<UUID, UUID> checkedPlayers = new HashMap<>();
    private final Map<UUID, BossBar> bossBars = new HashMap<>();
    private final Map<UUID, Location> originalLocations = new HashMap<>();

    public CheckManager(KubusCheck plugin) {
        this.plugin = plugin;
    }

    public void startCheck(Player admin, Player target) {
        String locationString = plugin.getConfig().getString("locations.check-location");
        if (locationString == null || locationString.isEmpty()) {
            admin.sendMessage(ChatUtil.format(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.check-location-not-set")));
            return;
        }

        Location checkLocation = ChatUtil.deserializeLocation(locationString);
        if (checkLocation == null) {
            admin.sendMessage(ChatUtil.format("&cWystapil blad z wczytywaniem lokalizacji sprawdzania. Sprawdz konsole."));
            return;
        }

        checkedPlayers.put(target.getUniqueId(), admin.getUniqueId());
        originalLocations.put(target.getUniqueId(), target.getLocation());
        target.teleport(checkLocation);

        String bossBarMessage = ChatUtil.format(plugin.getConfig().getString("bossbar.message"));
        BarColor color = BarColor.valueOf(plugin.getConfig().getString("bossbar.color", "RED"));
        BarStyle style = BarStyle.valueOf(plugin.getConfig().getString("bossbar.style", "SOLID"));
        BossBar bossBar = Bukkit.createBossBar(bossBarMessage, color, style);
        bossBar.addPlayer(target);
        bossBars.put(target.getUniqueId(), bossBar);

        admin.sendMessage(ChatUtil.format(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.checking-player").replace("{player}", target.getName())));
        target.sendMessage(ChatUtil.format(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.is-being-checked").replace("{admin}", admin.getName())));
        admin.sendMessage(ChatUtil.format(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.gui-usage-info")));
    }

    public void stopCheck(Player target, boolean isClean) {
        if (!isBeingChecked(target.getUniqueId())) {
            return;
        }

        UUID adminUUID = checkedPlayers.remove(target.getUniqueId());
        originalLocations.remove(target.getUniqueId());

        BossBar bossBar = bossBars.remove(target.getUniqueId());
        if (bossBar != null) {
            bossBar.removeAll();
        }

        Player admin = Bukkit.getPlayer(adminUUID);
        if (admin != null && isClean) {
            admin.sendMessage(ChatUtil.format(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.check-ended-clean").replace("{player}", target.getName())));
        }

        String spawnLocationString = plugin.getConfig().getString("locations.spawn-location");
        if (spawnLocationString != null && !spawnLocationString.isEmpty()) {
            Location spawnLocation = ChatUtil.deserializeLocation(spawnLocationString);
            if (spawnLocation != null) {
                target.teleport(spawnLocation);
                target.sendMessage(ChatUtil.format(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.player-teleported-to-spawn")));
            }
        }

        target.sendMessage(ChatUtil.format(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.no-longer-checked")));
    }

    public boolean isBeingChecked(UUID uuid) {
        return checkedPlayers.containsKey(uuid);
    }

    public UUID getAdminChecker(UUID playerUUID) {
        return checkedPlayers.get(playerUUID);
    }

    public Player getCheckedPlayer(Player admin) {
        for (Map.Entry<UUID, UUID> entry : checkedPlayers.entrySet()) {
            if (entry.getValue().equals(admin.getUniqueId())) {
                return Bukkit.getPlayer(entry.getKey());
            }
        }
        return null;
    }

    public void endAllChecks() {
        for (UUID playerUUID : checkedPlayers.keySet()) {
            Player p = Bukkit.getPlayer(playerUUID);
            if (p != null) {
                stopCheck(p, true);
            }
        }
        checkedPlayers.clear();
        bossBars.clear();
        originalLocations.clear();
    }
}
