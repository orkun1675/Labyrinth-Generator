package net.steeleyes.catacombs;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

public class CatGear {
    private Player player;

    private final List<ItemStack> gear = new ArrayList<>(36);
    private final ItemStack[] armor = new ItemStack[4];
    private final Location grave;

    public CatGear(Player player) {
        this.player = player;
        PlayerInventory inv = player.getInventory();
        grave = player.getLocation();
        for (int i = 0; i < armor.length; i++) {
            ItemStack is = player.getInventory().getArmorContents()[i];
            if (is != null) armor[i] = is.clone();
        }
        for (int i = 0; i <= 35; i++) {
            ItemStack is = inv.getItem(i);
            if (is != null) gear.add(is.clone());
        }
    }

    public void dropGear() {
        World world = grave.getWorld();
        for (ItemStack is : armor) if (is != null && is.getType() != Material.AIR) world.dropItem(grave, is);
        for (ItemStack is : gear) if (is != null && is.getType() != Material.AIR) world.dropItem(grave, is);
    }

    public void restoreGear() {
        PlayerInventory inv = player.getInventory();
        ItemStack[] currentArmor = inv.getArmorContents();
        for (int i = 0; i < armor.length; i++)
            if (armor[i] != null && armor[i].getType() != Material.AIR) currentArmor[i] = armor[i];
        inv.setArmorContents(currentArmor);
        player.updateInventory();
        for (int i = 0; i < gear.size(); i++) {
            ItemStack stk = gear.get(i);
            if (stk != null && stk.getType() != Material.AIR) inv.setItem(i, gear.get(i));
        }
    }

}
