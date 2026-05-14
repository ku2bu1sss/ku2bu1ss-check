package ku2bu1ss.kubusCheck;

import ku2bu1ss.kubusCheck.commands.CheckCommand;
import ku2bu1ss.kubusCheck.listeners.CheckListener;
import ku2bu1ss.kubusCheck.managers.CheckManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class KubusCheck extends JavaPlugin {

    private CheckManager checkManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        this.checkManager = new CheckManager(this);

        getCommand("sprawdz").setExecutor(new CheckCommand(this, checkManager));
        getServer().getPluginManager().registerEvents(new CheckListener(this, checkManager), this);

        getLogger().info("kubus-check zostal wlaczony!");
    }

    @Override
    public void onDisable() {
        if (checkManager != null) {
            checkManager.endAllChecks();
        }
        getLogger().info("kubus-check zostal wylaczony!");
    }
}
