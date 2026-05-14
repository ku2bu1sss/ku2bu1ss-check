package ku2bu1ss.kubusCheck.listeners;

import ku2bu1ss.kubusCheck.KubusCheck;
import ku2bu1ss.kubusCheck.managers.CheckManager;
import ku2bu1ss.kubusCheck.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.UUID;

public class CheckListener implements Listener {

    private final KubusCheck plugin;
    private final CheckManager checkManager;

    public CheckListener(KubusCheck plugin, CheckManager checkManager) {
        this.plugin = plugin;
        this.checkManager = checkManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (checkManager.isBeingChecked(player.getUniqueId())) {
            UUID adminUUID = checkManager.getAdminChecker(player.getUniqueId());
            Player admin = (adminUUID != null) ? Bukkit.getPlayer(adminUUID) : null;
            String adminName = (admin != null) ? admin.getName() : "CONSOLE";

            String command = plugin.getConfig().getString("decision-commands.logout", "ban {player} Logout during check")
                    .replace("{player}", player.getName())
                    .replace("{admin}", adminName);

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            checkManager.stopCheck(player, false);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (checkManager.isBeingChecked(event.getPlayer().getUniqueId())) {
            if (event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                event.getPlayer().teleport(event.getFrom());
            }
        }
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (checkManager.isBeingChecked(player.getUniqueId())) {
            String command = event.getMessage().substring(1).split(" ")[0];
            List<String> allowedCommands = plugin.getConfig().getStringList("allowed-commands-while-checked");

            if (!allowedCommands.contains(command.toLowerCase())) {
                event.setCancelled(true);
                player.sendMessage(ChatUtil.format(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.command-blocked")));
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (checkManager.isBeingChecked(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (checkManager.isBeingChecked(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (checkManager.isBeingChecked(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            if (checkManager.isBeingChecked(p.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        String title = event.getView().getTitle();

        String configTitleTemplate = ChatUtil.format(plugin.getConfig().getString("gui.title", ""));

        if (clickedInventory != null && title.startsWith(configTitleTemplate.replace("{player}", ""))) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;

            Player target = checkManager.getCheckedPlayer(player);
            if (target == null) return;

            String displayName = event.getCurrentItem().getItemMeta().getDisplayName();
            String cleanName = ChatUtil.format(plugin.getConfig().getString("gui.clean.name"));
            String cheaterName = ChatUtil.format(plugin.getConfig().getString("gui.cheater.name"));
            String logoutName = ChatUtil.format(plugin.getConfig().getString("gui.logout.name"));
            String noCoopName = ChatUtil.format(plugin.getConfig().getString("gui.no-cooperation.name"));

            String command = "";
            boolean isClean = false;

            if (displayName.equals(cleanName)) {
                isClean = true;
            } else if (displayName.equals(cheaterName)) {
                command = plugin.getConfig().getString("decision-commands.cheater");
            } else if (displayName.equals(logoutName)) {
                command = plugin.getConfig().getString("decision-commands.logout");
            } else if (displayName.equals(noCoopName)) {
                command = plugin.getConfig().getString("decision-commands.no-cooperation");
            }

            player.closeInventory();
            checkManager.stopCheck(target, isClean);

            if (!command.isEmpty()) {
                command = command.replace("{player}", target.getName()).replace("{admin}", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        }
    }
}
