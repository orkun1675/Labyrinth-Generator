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

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.World;
import org.bukkit.block.Block;

import net.steeleyes.data.CatSQL;

public class Dungeons {
    private final HashMap<String, Dungeon> dungeons = new HashMap<>();
    private Catacombs plugin;
    private CatSQL sql;

    public Dungeons(Catacombs plugin, CatSQL sql) {
        this.plugin = plugin;
        this.sql = sql;
        if (sql != null) try {
            ResultSet rs = sql.query("SELECT did,version,dname,wname,pname,major,minor FROM dungeons");
            while (rs.next()) {
                Dungeon dung = new Dungeon(plugin, rs);
                if (dung.getWorld() != null) dungeons.put(dung.getName(), dung);
            }
        } catch (Exception e) {
            System.err.println("[Catacombs] ERROR: " + e.getMessage());
        }
    }

    public void loadDungeon(String name) {
        if (sql != null) {
            try {
                ResultSet rs = sql.query("SELECT did,version,dname,wname,pname,major,minor FROM dungeons WHERE dname='" + name + "'");
                while (rs.next()) {
                    Dungeon dung = new Dungeon(plugin, rs);
                    if (dung.getWorld() != null) dungeons.put(dung.getName(), dung);
                }
            } catch (Exception e) {
                System.err.println("[Catacombs] ERROR: " + e.getMessage());
            }
        }
    }

    public void unloadDungeon(String name) {
        if (dungeons.containsKey(name)) {
            Dungeon dung = dungeons.remove(name);
            dung.setWorld(null);
        } else System.err.println("[Catacombs] ERROR: No dungeon called '" + name + "' is loaded");
    }

    public int loadWorld(String name) {
        int cnt = 0;
        if (sql != null) try {
            ResultSet rs = sql.query("SELECT did,version,dname,wname,pname,major,minor FROM dungeons WHERE wname='" + name + "'");
            while (rs.next()) {
                Dungeon dung = new Dungeon(plugin, rs);
                if (dung.getWorld() != null) {
                    String dname = dung.getName();
                    //if(!dungeons.containsKey(dname)) {
                    dungeons.put(dname, dung);
                    cnt++;
                    //}
                }
            }
        } catch (Exception e) {
            System.err.println("[Catacombs] ERROR: " + e.getMessage());
        }
        return cnt;
    }

    public void unloadWorld(String name) {
        List<String> remove = new ArrayList<>();
        for (Entry<String, Dungeon> e : dungeons.entrySet()) {
            String dname = e.getKey();
            Dungeon dung = e.getValue();
            World world = dung.getWorld();
            if (world != null && world.getName().equals(name)) remove.add(dname);
        }
        for (String dname : remove) unloadDungeon(dname);
    }

    public Boolean exists(String dname) {
        return dungeons.containsKey(dname);
    }

    public Boolean isPlanned(String dname) {
        if (dungeons.containsKey(dname)) {
            Dungeon dung = dungeons.get(dname);
            return dung.isOk() && !dung.isBuilt();
        }
        return false;
    }

    public Boolean isBuilt(String dname) {
        if (dungeons.containsKey(dname)) {
            Dungeon dung = dungeons.get(dname);
            return dung.isBuilt();
        }
        return false;
    }

    public Boolean isOk(String dname) {
        if (dungeons.containsKey(dname)) {
            Dungeon dung = dungeons.get(dname);
            return dung.isOk();
        }
        return false;
    }

    public Dungeon which(Block blk) {
        for (Entry<String, Dungeon> e : dungeons.entrySet()) {
            Dungeon dung = e.getValue();
            if (dung.isInRaw(blk)) {
                return dung;
            }
        }
        return null;
    }

    public List<Dungeon> all() {
        return new ArrayList<>(dungeons.values());
    }

    public void add(String dname, Dungeon d) {
        if (dungeons.containsKey(dname) && dungeons.get(dname).isBuilt())
            System.out.println("[catacombs] Already a built dungeon '" + dname + "'");
        else dungeons.put(dname, d);
    }

    public Boolean isInRaw(Block blk) {
        for (String name : dungeons.keySet()) if (dungeons.get(name).isInRaw(blk)) return true;
        return false;
    }

    public Boolean isProtected(Block blk) {
        for (String name : dungeons.keySet()) if (dungeons.get(name).isProtected(blk)) return true;
        return false;
    }

    public Boolean isSuspended(Block blk) {
        for (String name : dungeons.keySet()) if (dungeons.get(name).isSuspended(blk)) return true;
        return false;
    }

    public Dungeon getDungeon(Block blk) {
        for (String name : dungeons.keySet()) {
            Dungeon x = dungeons.get(name);
            if (x.isInRaw(blk)) return x;
        }
        return null;
    }

    public Dungeon getOverlap(Dungeon dung) {
        for (String name : dungeons.keySet()) {
            Dungeon x = dungeons.get(name);
            if (!dung.equals(x)) if (dung.overlaps(x)) return x;
        }
        return null;
    }

    public void suspend(String name) {
        if (dungeons.containsKey(name)) {
            Dungeon dung = dungeons.get(name);
            dung.suspend();
        }
    }

    public void enable(String name) {
        if (dungeons.containsKey(name)) {
            Dungeon dung = dungeons.get(name);
            dung.enable();
        }
    }

    public void remove(String name) {
        if (dungeons.containsKey(name)) {
            Dungeon dung = dungeons.get(name);
            dung.remove();
            dungeons.remove(name);
        }
    }

    public void clearMonsters(Catacombs plugin) {
        for (Entry<String, Dungeon> e : dungeons.entrySet()) {
            e.getValue().clearMonsters(plugin);
        }
    }

    public void fixSecretDoors() {
        for (Entry<String, Dungeon> e : dungeons.entrySet()) {
            Dungeon dung = e.getValue();
            int cnt = dung.fixSecretDoors();
            if (cnt > 0) System.out.println("[Catacombs]   '" + dung.getName() + "' fixed " + cnt + " secret doors");
        }
    }

    public void fixDoors() {
        for (Entry<String, Dungeon> e : dungeons.entrySet()) {
            Dungeon dung = e.getValue();
            int cnt = dung.fixDoors();
            if (cnt > 0)
                System.out.println("[Catacombs]   '" + dung.getName() + "' fixed or closed " + cnt + " wooden/iron doors");
        }
    }

    public int changeDoorsToIron() {
        int num = 0;
        for (Entry<String, Dungeon> e : dungeons.entrySet()) {
            Dungeon dung = e.getValue();
            int cnt = dung.changeDoorsToIron();
            if (cnt > 0)
                System.out.println("[Catacombs]   '" + dung.getName() + "' converted " + cnt + " doors to iron");
            num++;
        }
        return num;
    }

    public List<String> getNames() {
        List<String> list = new ArrayList<>(dungeons.keySet());
        Collections.sort(list);
        return list;
    }

    public Dungeon get(String dname) {
        if (dungeons.containsKey(dname)) return dungeons.get(dname);
        return null;
    }

    public List<String> getinfo(String dname) {
        if (dungeons.containsKey(dname)) return dungeons.get(dname).getinfo();
        return null;
    }

    public List<String> dump(String dname) {
        if (dungeons.containsKey(dname)) return dungeons.get(dname).dump();
        return null;
    }

    public List<String> map(String dname) {
        if (dungeons.containsKey(dname)) return dungeons.get(dname).map();
        return null;
    }
}
