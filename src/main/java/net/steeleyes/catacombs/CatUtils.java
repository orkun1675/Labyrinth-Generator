/*  This file is part of Catacombs.

Catacombs is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Catacombs is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Catacombs.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author John Keay  <>(@Steeleyes, @Blockhead2)
 * @copyright Copyright (C) 2011
 * @license GNU GPL <http://www.gnu.org/licenses/>
 */
package net.steeleyes.catacombs;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class CatUtils {
    // Is a Block soild? (this is a partial list)
    public static Boolean isSolid(Block blk) {
        Material mat = blk.getType();
        return mat != Material.WOODEN_DOOR && mat != Material.IRON_DOOR_BLOCK && mat != Material.AIR
                && mat != Material.TORCH && mat != Material.WEB && mat != Material.RED_MUSHROOM
                && mat != Material.BROWN_MUSHROOM;
    }

    public static List<String> getKeys(FileConfiguration config, String path) {
        if (config.contains(path)) {
            List<String> list = new ArrayList<>();
            list.addAll(config.getConfigurationSection(path).getKeys(false));
            return list;
        }
        return null;
    }

    public static String giveCash(CatConfig cnf, Entity ent, double gold) {
        if (cnf == null || cnf.GoldOff()) return null;
        Economy economy = Catacombs.getEconomy();
        if (ent instanceof Player && economy != null) {
            String name = ent.getName();
            EconomyResponse resp = Catacombs.getEconomy().depositPlayer(name, gold);
            if (!resp.transactionSuccess()) {
                System.err.println("[Catacombs] Problem giving cash to " + name);
                return " error";
            } else return economy.format(economy.getBalance(name));
        }
        return null;
    }

    public static Boolean takeCash(Entity ent, int gold, String reason) {
        if (gold == 0) return true;
        Boolean res = false;
        if (ent instanceof Player) {
            Player player = (Player) ent;
            Economy economy = Catacombs.getEconomy();
            if (economy != null) {
                EconomyResponse resp = economy.withdrawPlayer(player.getName(), gold);
                double bal = economy.getBalance(player.getName());
                if (resp.transactionSuccess()) {
                    player.sendMessage("It costs you " + gold + " " + reason + " (" + economy.format(bal) + ")");
                    res = true;
                } else player.sendMessage("Not enough money " + reason + " (" + economy.format(bal) + ")");
            }
        }
        return res;
    }

    public static Boolean toggleSecretDoor(Block blk) {
        Block piston = null;
        for (int i = 1; i <= 3; i++) {
            piston = blk.getRelative(BlockFace.DOWN, i);
            if (piston.getType() == Material.PISTON_STICKY_BASE) break;
            piston = null;
        }
        // Piston needs to point up
        if (piston == null || (piston.getData() & 7) != 1) return false;

        Block power = piston.getRelative(BlockFace.DOWN, 1);

        if (power.getType() == Material.REDSTONE_TORCH_ON) {
            Block upper_door = piston.getRelative(BlockFace.UP, 3);
            Material m = upper_door.getType();
            byte code = upper_door.getData();
            power.setTypeIdAndData(m.getId(), code, false);
            upper_door.setType(Material.AIR);
            return true;
        } else {
            Block upper_door = piston.getRelative(BlockFace.UP, 3);
            Material m = power.getType();
            byte code = power.getData();
            power.setType(Material.REDSTONE_TORCH_ON);
            upper_door.setTypeIdAndData(m.getId(), code, false);
            return true;
        }
    }

    // Just a simple on surface check for the moment
    // ToDo: count under trees and shallow overhangs as surface too
    public static Boolean onSurface(Block blk) {
        Location loc = blk.getLocation();
        Block spawn = loc.getBlock();
        Block surface = blk.getWorld().getHighestBlockAt(loc);
        return spawn.getY() == surface.getY();
    }

    public static int countPlayerNear(Entity ent, double h, double v) {
        int cnt = 0;
        if (ent != null) for (Entity e : ent.getNearbyEntities(h, v, h)) if (e instanceof Player) cnt++;
        return cnt;
    }

    public static LivingEntity getDamager(EntityDamageEvent evt) {
        Entity damager = null;

        if (evt instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) evt;
            damager = e.getDamager();
            if (damager instanceof Projectile) damager = (Entity) ((Projectile) damager).getShooter();
        }
        if (damager instanceof LivingEntity) return (LivingEntity) damager;
        return null;
    }

    public static long parseTime(String s) {
        Pattern p = Pattern.compile("(\\d+)([smhd])");
        Matcher m = p.matcher(s);
        long num = 0;
        while (m.find()) {
            int i = Integer.parseInt(m.group(1));
            String unit = m.group(2);
            //    System.out.println("[Catacombs]   parse "+unit+" "+i);

            if (unit.equals("m")) i = i * 60;
            if (unit.equals("h")) i = i * 60 * 60;
            if (unit.equals("d")) i = i * 60 * 60 * 24;
            num += i;
        }
        //System.out.println("[Catacombs] Parse "+s+" = "+num+"sec(s)");
        return num * 1000;
    }

    public static String formatTime(Long num) {
        if (num <= 0) return "never";
        String str = "";
        num = num / 1000;
        if (num >= 60 * 60 * 24) {
            int d = (int) (num / (60 * 60 * 24));
            str += d + "d";
            num = num - (d * 60 * 60 * 24);
        }
        if (num >= 60 * 60) {
            int d = (int) (num / (60 * 60));
            str += d + "h";
            num = num - (d * 60 * 60);
        }
        if (num >= 60) {
            int d = (int) (num / (60));
            str += d + "m";
            num = num - (d * 60);
        }
        if (num > 0) str += num + "s";
        return str;
    }

    public static <T extends Enum<T>> T getEnumFromString(Class<T> c, String string) {
        if (c != null && string != null) try {
            return Enum.valueOf(c, string.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
        }
        return null;
    }

    public static <K, V extends Comparable<V>> List<Entry<K, V>> sortByValue(Map<K, V> map) {
        List<Entry<K, V>> entries = new ArrayList<>(map.entrySet());
        Collections.sort(entries, new ByValue<K, V>());
        return entries;
    }

    private static class ByValue<K, V extends Comparable<V>> implements Comparator<Entry<K, V>> {
        public int compare(Entry<K, V> o1, Entry<K, V> o2) {
            return o2.getValue().compareTo(o1.getValue());
        }
    }
}

