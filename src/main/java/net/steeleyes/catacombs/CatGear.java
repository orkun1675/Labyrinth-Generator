package net.steeleyes.catacombs;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class CatGear {
    private Player player;

    private final ItemStack[] storage;
    private final ItemStack[] armor;
    private final ItemStack[] extra;
    private final Location grave;

    public CatGear(Player player) {
        this.player = player;
        grave = player.getLocation();
        PlayerInventory inv = player.getInventory();
        storage = new ItemStack[inv.getStorageContents().length];
        for (int i = 0; i < storage.length; i++) {
            ItemStack is = inv.getStorageContents()[i];
            if (is != null) storage[i] = is.clone();
        }
        armor = new ItemStack[inv.getArmorContents().length];
        for (int i = 0; i < armor.length; i++) {
            ItemStack is = inv.getArmorContents()[i];
            if (is != null) armor[i] = is.clone();
        }
        extra = new ItemStack[inv.getExtraContents().length];
        for (int i = 0; i < extra.length; i++) {
            ItemStack is = inv.getExtraContents()[i];
            if (is != null) extra[i] = is.clone();
        }
    }

    public void dropGear() {
        World world = grave.getWorld();
        for (ItemStack is : storage) if (is != null && is.getType() != Material.AIR) world.dropItem(grave, is);
        for (ItemStack is : armor) if (is != null && is.getType() != Material.AIR) world.dropItem(grave, is);
        for (ItemStack is : extra) if (is != null && is.getType() != Material.AIR) world.dropItem(grave, is);
    }

    public void restoreGear() {
        PlayerInventory inv = player.getInventory();
        inv.setStorageContents(storage);
        inv.setArmorContents(armor);
        inv.setExtraContents(extra);
        player.updateInventory();
    }

}
