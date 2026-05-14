package ku2bu1ss.kubusCheck.commands;

import ku2bu1ss.kubusCheck.KubusCheck;
import ku2bu1ss.kubusCheck.gui.CheckGUI;
import ku2bu1ss.kubusCheck.managers.CheckManager;
import ku2bu1ss.kubusCheck.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CheckCommand implements CommandExecutor {

    private final KubusCheck plugin;
    private final CheckManager checkManager;

    public CheckCommand(KubusCheck plugin, CheckManager checkManager) {
        this.plugin = plugin;
        this.checkManager = checkManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Komenda tylko dla graczy.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("kubuscheck.admin")) {
            player.sendMessage(ChatUtil.format(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        if (args.length == 0) {
            Player target = checkManager.getCheckedPlayer(player);
            if (target != null) {
                new CheckGUI(plugin, checkManager).open(player, target);
            } else {
                player.sendMessage(ChatUtil.format("&cPoprawne uzycie: /sprawdz <gracz|lokalizacja|setspawn>"));
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "lokalizacja":
                handleLocationCommand(player, args);
                break;
            case "setspawn":
                handleSetSpawnCommand(player);
                break;
            default:
                handleCheckPlayerCommand(player, args[0]);
                break;
        }

        return true;
    }

    private void handleCheckPlayerCommand(Player admin, String targetName) {
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            admin.sendMessage(ChatUtil.format(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.player-not-found")));
            return;
        }
        if (target.equals(admin)) {
            admin.sendMessage(ChatUtil.format(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.cannot-check-yourself")));
            return;
        }
        if (target.hasPermission("kubuscheck.bypass")) {
            admin.sendMessage(ChatUtil.format(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.player-cannot-be-checked")));
            return;
        }
        if (checkManager.isBeingChecked(target.getUniqueId())) {
            admin.sendMessage(ChatUtil.format(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.player-already-checked")));
            return;
        }

        checkManager.startCheck(admin, target);
        new CheckGUI(plugin, checkManager).open(admin, target);
    }

    private void handleLocationCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatUtil.format("&cPoprawne uzycie: /sprawdz lokalizacja <ustaw|usun>"));
            return;
        }
        String action = args[1].toLowerCase();
        if (action.equals("ustaw")) {
            String locationString = ChatUtil.serializeLocation(player.getLocation());
            plugin.getConfig().set("locations.check-location", locationString);
            plugin.saveConfig();
            player.sendMessage(ChatUtil.format(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.check-location-set")));
        } else if (action.equals("usun")) {
            plugin.getConfig().set("locations.check-location", "");
            plugin.saveConfig();
            player.sendMessage(ChatUtil.format(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.check-location-removed")));
        } else {
            player.sendMessage(ChatUtil.format("&cPoprawne uzycie: /sprawdz lokalizacja <ustaw|usun>"));
        }
    }

    private void handleSetSpawnCommand(Player player) {
        String locationString = ChatUtil.serializeLocation(player.getLocation());
        plugin.getConfig().set("locations.spawn-location", locationString);
        plugin.saveConfig();
        player.sendMessage(ChatUtil.format(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.spawn-location-set")));
    }
}
