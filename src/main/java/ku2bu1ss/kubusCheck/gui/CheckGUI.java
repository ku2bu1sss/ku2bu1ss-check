package ku2bu1ss.kubusCheck.gui;

import ku2bu1ss.kubusCheck.KubusCheck;
import ku2bu1ss.kubusCheck.managers.CheckManager;
import ku2bu1ss.kubusCheck.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class CheckGUI {

    private final KubusCheck plugin;
    private final CheckManager checkManager;

    public CheckGUI(KubusCheck plugin, CheckManager checkManager) {
        this.plugin = plugin;
        this.checkManager = checkManager;
    }

    public void open(Player admin, Player target) {
        String title = ChatUtil.format(plugin.getConfig().getString("gui.title").replace("{player}", target.getName()));
        Inventory gui = Bukkit.createInventory(null, 27, title);

        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, filler);
        }

        gui.setItem(10, createGuiItem("clean"));
        gui.setItem(12, createGuiItem("cheater"));
        gui.setItem(14, createGuiItem("logout"));
        gui.setItem(16, createGuiItem("no-cooperation"));

        admin.openInventory(gui);
    }

    private ItemStack createGuiItem(String key) {
        Material material = Material.valueOf(plugin.getConfig().getString("gui." + key + ".material", "STONE"));
        String name = ChatUtil.format(plugin.getConfig().getString("gui." + key + ".name"));
        List<String> lore = plugin.getConfig().getStringList("gui." + key + ".lore").stream()
                .map(ChatUtil::format)
                .collect(Collectors.toList());
        return createItem(material, name, lore);
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
