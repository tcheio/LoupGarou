package fr.leomelki.loupgarou.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public class VariousUtils {
	public static double distanceSquaredXZ(Location from, Location to) {
		return Math.pow(from.getX()-to.getX(), 2)+Math.pow(from.getZ()-to.getZ(), 2);
	}
	public static void setWarning(Player p, boolean warning) {
		WorldBorder wb = p.getWorld().getWorldBorder();
		WorldBorder border = Bukkit.createWorldBorder();

		border.setCenter(p.getLocation().getX(), p.getLocation().getZ());
		border.setSize(wb.getSize());
		border.setWarningDistance((int) (warning ? wb.getSize() : wb.getWarningDistance()));
		border.setWarningTime(0);

		p.setWorldBorder(border);
	}
	public static boolean isCustomInventory(Inventory inventory) {
		return inventory.getHolder() == null && inventory.getType() == InventoryType.CHEST;
	}
	private static char[] hex = "0123456789abcdef".toCharArray();
	public static char toHex(int i) {
		return hex[i];
	}

}
