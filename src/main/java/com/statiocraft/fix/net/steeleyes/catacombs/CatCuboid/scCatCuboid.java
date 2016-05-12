package com.statiocraft.fix.net.steeleyes.catacombs.CatCuboid;

import net.steeleyes.catacombs.CatCuboid;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class scCatCuboid {

    public scCatCuboid(CatCuboid c) {
        if (c == null) throw new NullPointerException();
    }

    public void forceLightLevel(int l, World w, int xl, int xh, int yl, int yh, int zl, int zh) throws Exception {
        //Current version
//        Bukkit.getLogger().severe(Bukkit.getServer().getClass().getName());
        String v = Bukkit.getServer().getClass().getName().split("\\.")[3];

        //Minecraft world
        Object mw = w.getClass().getDeclaredMethod("getHandle", new Class[0]).invoke(w);

        //Method to set light
        Method m = Class.forName("net.minecraft.server." + v + ".World").getDeclaredMethod("c", Class.forName("net.minecraft.server." + v + ".EnumSkyBlock"),
                Class.forName("net.minecraft.server." + v + ".BlockPosition"));

        //BlockPosition constructor
        Constructor<?> c = Class.forName("net.minecraft.server." + v + ".BlockPosition").getDeclaredConstructor(int.class, int.class, int.class);

        //EnumSkyBlock
        @SuppressWarnings("rawtypes")
        Enum esb = (Enum) Class.forName("net.minecraft.server." + v + ".EnumSkyBlock").getDeclaredField("BLOCK").get(null);

        for (int x = xl; x <= xh; x++)
            for (int y = yl; y <= yh; y++)
                for (int z = zl; z <= zh; z++) m.invoke(mw, esb, c.newInstance(x, y, z));
    }
}
