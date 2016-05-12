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

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.plugin.PluginManager;

import java.util.HashMap;
import java.util.Map;

public class Clan implements Listener {
    private Catacombs plugin;
    private final Map<LivingEntity, Mob> members = new HashMap<>();
    private Dungeon notify;

    public Clan(Catacombs plugin, Dungeon notify) {
        this.plugin = plugin;
        this.notify = notify;
        registerListener();
    }

    private void registerListener() {
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(this, plugin);
    }

    /////////////////////////////////////////////////////////////////////////////
    //
    //    Entity Events
    //
    /////////////////////////////////////////////////////////////////////////////

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityTarget(EntityTargetEvent evt) {
        Entity e = evt.getEntity();
        if (evt.isCancelled()
                || !(e instanceof LivingEntity)
                || !members.containsKey(e)) {
            return;
        }
        evt.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamage(EntityDamageEvent evt) {
        Entity e = evt.getEntity();
        if (evt.isCancelled()
                || !(e instanceof LivingEntity)
                || !members.containsKey(e)) return;
        Mob mob = members.get(e);
        mob.damage(evt);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDeath(EntityDeathEvent evt) {
        LivingEntity le = evt.getEntity();
        if (!members.containsKey(le)) return;
        Mob mob = members.remove(le);
        System.out.println("[Catacombs] Death " + mob.getType().getName());
        if (mob.getNotify())             notify.regionMobDeath(mob.getEnt());
    }
}
