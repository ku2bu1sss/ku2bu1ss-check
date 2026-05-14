package ku2bu1ss.kubusCheck.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;

public class ChatUtil {

    public static String format(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String serializeLocation(Location location) {
        if (location == null) return "";
        return location.getWorld().getName() + ";" +
                location.getX() + ";" +
                location.getY() + ";" +
                location.getZ() + ";" +
                location.getYaw() + ";" +
                location.getPitch();
    }

    public static Location deserializeLocation(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        final String[] parts = s.split(";");
        if (parts.length == 6) {
            try {
                World w = Bukkit.getServer().getWorld(parts[0]);
                if (w == null) return null;
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                double z = Double.parseDouble(parts[3]);
                float yaw = Float.parseFloat(parts[4]);
                float pitch = Float.parseFloat(parts[5]);
                return new Location(w, x, y, z, yaw, pitch);
            } catch (NumberFormatException e) {
                Bukkit.getLogger().warning("Failed to deserialize location: " + s);
                return null;
            }
        }
        return null;
    }
}
