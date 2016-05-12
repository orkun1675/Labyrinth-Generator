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

import net.steeleyes.data.CatSQL;
import net.steeleyes.maps.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "incomplete-switch"})
public class CatLevel {
    private int roomDepth;
    private int roofDepth;
    private int floorDepth;
    private int levelDepth;

    private CatConfig cnf = null;

    private int coal = 0;
    private int iron = 0;
    private int redstone = 0;
    private int lapis = 0;
    private int gold = 0;
    private int diamond = 0;

    private World world;
    private Level level = null;
    private CatCuboid cube = null;
    private Vector top = null;
    private Vector bot = null;
    private Boolean build_ok = false;
    private Boolean can_go_lower = false;
    private String map = "";

    private int lid = -1;

    public CatLevel(CatConfig cnf, Location pt) {
        this(cnf, pt.getWorld(), pt.getBlockX(), pt.getBlockY() - 1, pt.getBlockZ(), Direction.ANY);
    }

    public CatLevel(CatConfig cnf, World world, Vector v, Direction dir) {
        this(cnf, world, v.getBlockX(), v.getBlockY(), v.getBlockZ(), dir);
    }

    public CatLevel(CatConfig cnf, World world, Vector v) {
        this(cnf, world, v.getBlockX(), v.getBlockY(), v.getBlockZ(), Direction.ANY);
    }

    public CatLevel(CatConfig cnf, World world, int x, int y, int z) {
        this(cnf, world, x, y, z, Direction.ANY);
    }

    // ToDo: Need to change map over to the PrePlanned class (and in Level too etc)
    public CatLevel(CatConfig cnf, World world, int x, int y, int z, PrePlanned map, Direction dir) {
        build_ok = true;
        can_go_lower = true;
        this.cnf = cnf;
        roomDepth = cnf.roomDepth();
        roofDepth = cnf.roofDepth();
        floorDepth = cnf.floorDepth() + cnf.extraDepth();
        levelDepth = floorDepth + roomDepth + roofDepth;
        this.world = world;

        top = new Vector(x, y + roomDepth + roofDepth - 1, z);
        bot = new Vector(x, y - floorDepth - 1, z);
        level = new Level(cnf, map, dir);

        int xl = x - level.start().x;
        int yl = y - floorDepth;
        int zl = z + level.start().y - level.grid().sy() + 1;
        int xh = x - level.start().x + level.grid().sx() - 1;
        int yh = y + roomDepth + roofDepth - 1;
        int zh = z + level.start().y;
        cube = new CatCuboid(world, xl, yl, zl, xh, yh, zh, CatCuboid.Type.HUT);
    }

    public CatLevel(Catacombs plugin, ResultSet lvl, World world, Boolean enable) throws Exception {
        build_ok = true;
        can_go_lower = true;
        this.cnf = plugin.getCnf();
        this.world = world;
        lid = lvl.getInt("lid");

        top = new Vector(lvl.getInt("sx"), lvl.getInt("sy"), lvl.getInt("sz"));
        bot = new Vector(lvl.getInt("ex"), lvl.getInt("ey"), lvl.getInt("ez"));

        cube = new CatCuboid(world, lvl.getInt("xl"), lvl.getInt("yl"), lvl.getInt("zl"),
                lvl.getInt("xh"), lvl.getInt("yh"), lvl.getInt("zh"),
                CatUtils.getEnumFromString(CatCuboid.Type.class, lvl.getString("type")));

        level = new Level(cnf);

        //cube.setEnable(enable);
        roofDepth = lvl.getInt("roof");
        roomDepth = lvl.getInt("room");
        floorDepth = lvl.getInt("floor");
        levelDepth = floorDepth + roomDepth + roofDepth;
    }

//  public CatLevel(Catacombs plugin, ResultSet lvl, World world) throws Exception {
//    build_ok = true;
//    can_go_lower = true;
//    this.cnf = plugin.cnf;
//    this.world  = world;
//
//    top = new Vector(lvl.getInt("sx"),lvl.getInt("sy"),lvl.getInt("sz"));
//    bot = new Vector(lvl.getInt("ex"),lvl.getInt("ey"),lvl.getInt("ez"));
//
//    cube = new CatCuboid(world,lvl.getInt("xl"),lvl.getInt("yl"),lvl.getInt("zl"),
//            lvl.getInt("xh"),lvl.getInt("yh"),lvl.getInt("zh"),
//            (lvl.getInt("hut")==1)?CatCuboid.Type.HUT:CatCuboid.Type.LEVEL);
//
//    level = new Level(cnf);    
//
//    if(lvl.getInt("enable")!=0)
//      cube.enable();
//    else
//      cube.suspend();
//    roofDepth = cube.guessRoofSize();
//    roomDepth = cube.guessRoomSize();
//    floorDepth = 0;
//    levelDepth = 0;
//  }

    public CatLevel(CatConfig cnf, World world, int x, int y, int z, Direction dir) {

        build_ok = false;
        can_go_lower = false;
        this.cnf = cnf;
        roomDepth = cnf.roomDepth();
        roofDepth = cnf.roofDepth();
        floorDepth = cnf.floorDepth();
        levelDepth = floorDepth + roomDepth + roofDepth;
        this.world = world;

        if (y + 1 - levelDepth <= 4) {
            System.out.println("[Catacombs] Stopping CatLevel due to bedrock");
            return;
        }
        top = new Vector(x, y, z);
        if (spaceForStairs()) {
            cube = getNaturalCuboid(cnf, world, top.getBlockX(), top.getBlockY() + 1 - levelDepth, top.getBlockZ());

            //System.out.println("Natural="+cube);

            if (cube.dx() < 8 || cube.dz() < 8) {
                System.out.println("[Catacombs] Stopping CatLevel < 8x8");
                return;
            }
            // 3D-2D
            int sx = top.getBlockX() - cube.xl;
            int sy = cube.dz() - 1 - (top.getBlockZ() - cube.zl);
            //System.out.println("[Catacombs] start x="+sx+" y="+sy);

            level = new Level(cnf, cube.dx(), cube.dz(), sx, sy, dir);

            // Check number of rooms to make sure things are ok
            if (level.num_rooms() < 1) {
                System.out.println("[Catacombs] Stopping CatLevel 0 rooms");
                return;
            }

            build_ok = true;
            if (level.end_dir() != null) {
                // 3D-2D
                int xx = top.getBlockX() - level.start().x + level.end().x;
                int yy = top.getBlockY() - levelDepth;
                int zz = top.getBlockZ() + level.start().y - level.end().y;
                bot = new Vector(xx, yy, zz);
                can_go_lower = true;
            }
        } else {
            System.out.println("[Catacombs] Stopping CatLevel no room for stairs");
        }
    }

    public void saveDB(CatSQL sql, int did) {
        if (lid <= 0) {
            sql.command("INSERT INTO levels2 " +
                    "(did,type,room,roof,floor,xl,yl,zl,xh,yh,zh,sx,sy,sz,ex,ey,ez) VALUES" +
                    "(" + did + ",'" + cube.getType() + "'," + roomDepth + "," + roofDepth + "," + floorDepth +
                    "," + cube.xl + "," + cube.yl + "," + cube.zl +
                    "," + cube.xh + "," + cube.yh + "," + cube.zh +
                    "," + top.getBlockX() + "," + top.getBlockY() + "," + top.getBlockZ() +
                    "," + bot.getBlockX() + "," + bot.getBlockY() + "," + bot.getBlockZ() +
                    ");");
            lid = sql.getLastId();
        } else {
            System.err.println("[Catacombs] INTERNAL ERROR: CatLevel .db updates not implemented yet");
        }

    }

    public Block getEndChestDoor() {
        return world.getBlockAt(bot.getBlockX(), bot.getBlockY() + floorDepth + 1, bot.getBlockZ());
    }

    public List<String> dump(Vector top) {
        return cube.dump(top);
    }

    public List<String> map() {
        return cube.map();
    }

    public Boolean getBuild_ok() {
        return build_ok;
    }

    public Boolean getCan_go_lower() {
        return can_go_lower;
    }

    public Vector getBot() {
        return bot;
    }

    public CatCuboid getCube() {
        return cube;
    }

    public Vector getTop() {
        return top;
    }

    public String getMapString() {
        return level.getMapString();
    }

    public List<String> getMap() {
        List<String> list = new ArrayList<>();
        list.add("TOP," + top.getBlockX() + "," + top.getBlockY() + "," + top.getBlockZ() + "\r\n");
        list.addAll(level.getMap());
        return list;
    }

    public String summary() {
        return "Area (" + cube.dx() + " x " + cube.dz() + " dy" + cube.dy() + ") Rooms=" + level.num_rooms();
    }

    public List<String> getinfo() {
        List<String> info = new ArrayList<>();
        info.add("cube:" + cube);
        info.add("top:" + top);
        info.add("bot:" + bot);
        info.add("build_ok:" + build_ok);
        info.add("can_go_lower:" + can_go_lower);
        info.add("dx:" + cube.dx());
        info.add("dz:" + cube.dz());
        info.add("roomDepth:" + roomDepth);
        info.add("roofDepth:" + roofDepth);
        info.add("floorDepth:" + floorDepth);
        //info.add("map:"+map);

        return info;
    }


    public int getRoofDepth() {
        return roofDepth;
    }

    public int getRoomDepth() {
        return roomDepth;
    }

    public int getFloorDepth() {
        return (floorDepth == 0) ? 4 : floorDepth;
    }

    public Direction end_dir() {
        if (level == null)
            return Direction.ANY;
        return level.end_dir();
    }

    public Direction start_dir() {
        if (level == null)
            return Direction.ANY;
        return level.start_dir();
    }

    public Boolean isOk() {
        return level.isOk();
    }

    public void stealDirection(CatLevel from) {
        if (level.end_dir() == null || level.end_dir() == Direction.ANY) {
            level.end_dir(from.start_dir().turn180());
        }
    }

    public void setEndSquare(Square s) {
        if (level.end().x >= 0 && level.end().y >= 0) {
            level.grid().set(level.end().x, level.end().y, s);
        }
        can_go_lower = false;
    }

    public final void show() {
        level.show();
        System.out.println(cube);
        System.out.println("TOP :" + top + " BOT:" + bot + " start_dir:" + level.start_dir() + " end_dir:" + level.end_dir() + " lower:" + can_go_lower);
    }

    private Boolean spaceForStairs() {
        // Check stright down, ignore the top block
        Boolean stairs_ok = areBlocksNatural(top.getBlockX(), top.getBlockY() - 1, top.getBlockZ(), 0, -1, 0, levelDepth - 1);
        stairs_ok &= areBlocksNatural(top.getBlockX() + 1, top.getBlockY() - 1, top.getBlockZ(), 0, -1, 0, levelDepth - 1);
        stairs_ok &= areBlocksNatural(top.getBlockX() - 1, top.getBlockY() - 1, top.getBlockZ(), 0, -1, 0, levelDepth - 1);
        stairs_ok &= areBlocksNatural(top.getBlockX(), top.getBlockY() - 1, top.getBlockZ() + 1, 0, -1, 0, levelDepth - 1);
        stairs_ok &= areBlocksNatural(top.getBlockX(), top.getBlockY() - 1, top.getBlockZ() - 1, 0, -1, 0, levelDepth - 1);
        return stairs_ok;
    }

    private void renderTileSection(BlockChangeHandler handler, int xx, int y1, int y2, int zz,
                                   Material mat,
                                   CatMat major) {
        CatMat use;
        if (major.getMat() == mat) use = (cnf.MinorChance()) ? cnf.minorMat() : major;
        else use = new CatMat(mat);

        for (int yy = y1; yy <= y2 && mat != null; yy++) {
            Block b = world.getBlockAt(xx, yy, zz);

            if (b.getType() == Material.IRON_ORE) iron++;
            else if (b.getType() == Material.COAL_ORE) coal++;
            else if (b.getType() == Material.DIAMOND_ORE) diamond++;
            else if (b.getType() == Material.LAPIS_ORE) lapis++;
            else if (b.getType() == Material.REDSTONE_ORE) redstone++;
            else if (b.getType() == Material.GOLD_ORE) gold++;

            if (mat == Material.AIR) handler.add(b, cnf.AirType(), Position.HIGH);
            else if (use.getHasCode()) handler.add(b, use.getMat(), use.getCode(), Position.HIGH);
            else handler.add(b, use.getMat(), Position.HIGH);
        }
    }

    private void renderTile(BlockChangeHandler handler, int xx, int y, int zz,
                            Material floor0, Material floor1,
                            Material room0, Material room1,
                            Material roof0, Material roof1) {
        int floor_l = y + 1 - levelDepth;
        int floor_h = floor_l + floorDepth - 1;
        int room_l = floor_h + 1;
        int room_h = room_l + roomDepth - 1;
        int roof_l = room_h + 1;
        int roof_h = roof_l + roofDepth - 1;
        renderTileSection(handler, xx, floor_l, floor_h - 1, zz, floor0, cnf.floorMat());
        renderTileSection(handler, xx, floor_h, floor_h, zz, floor1, cnf.floorMat());
        renderTileSection(handler, xx, room_l, room_l + 1, zz, room0, cnf.majorMat());
        renderTileSection(handler, xx, room_l + 2, room_h, zz, room1, cnf.majorMat());
        renderTileSection(handler, xx, roof_l, roof_l, zz, roof0, cnf.roofMat());
        renderTileSection(handler, xx, roof_l + 1, roof_h, zz, roof1, cnf.roofMat());
    }

    @SuppressWarnings("deprecation")
    public void addLeveltoWorld(BlockChangeHandler handler, String[] info) throws Exception {
        if (level == null || world == null)
            return;

        Grid g = level.grid();
        int floor_l = top.getBlockY() + 1 - levelDepth;
        int floor_h = floor_l + floorDepth - 1;
        int room_l = floor_h + 1;
        int room_h = room_l + roomDepth - 1;
        int roof_l = room_h + 1;
        int roof_h = roof_l + roofDepth - 1;

        CatMat major = cnf.majorMat();
        CatMat minor = cnf.minorMat();
        CatMat floorBlk = cnf.floorMat();
        CatMat roofBlk = cnf.roofMat();
        if (major == null) throw new Exception("Invalid major material.");
        if (minor == null) throw new Exception("Invalid minor material.");
        if (floorBlk == null) throw new Exception("Invalid floorBlk material.");
        if (roofBlk == null) throw new Exception("Invalid roofBlk material.");
        // Short hand names to help a bit with code formatting
        Material cob = major.getMat();
        Material flr = floorBlk.getMat();
        Material roo = roofBlk.getMat();
        Material air = Material.AIR;
        Material bar = Material.IRON_FENCE;

        Boolean SquareHuts = false;

        // Extra cobblestone (major) when outside
        Material undr = (cnf.UnderFill()) ? cob : null;
        Material over = (cnf.OverFill() && cube.isLevel()) ? cob : null;
        Material ecob = (SquareHuts && cube.isHut()) ? cob : over;

        // First pass - Place all the Blocks
        for (int x = 0; x < g.sx(); x++)
            for (int y = 0; y < g.sy(); y++) {
                // 3D-2D
                Square s = g.get(x, y);
                int xx = top.getBlockX() + x - level.start().x;
                int zz = top.getBlockZ() - y + level.start().y;

                switch (s) {
                    case UPWALL:
                        renderTile(handler, xx, top.getBlockY(), zz, undr, undr, cob, cob, cob, cob);
                        break;
                    case DOWNWALL:
                        renderTile(handler, xx, top.getBlockY(), zz, cob, cob, cob, cob, ecob, over);
                        break;
                    case BOTHWALL:
                        renderTile(handler, xx, top.getBlockY(), zz, cob, cob, cob, cob, cob, cob);
                        break;
                    case WALL:
                    case HIGH_BARS:
                    case WINDOW:
                    case FIXEDWALL:
                        renderTile(handler, xx, top.getBlockY(), zz, undr, cob, cob, cob, ecob, over);
                        break;
                    case WATER:
                    case LAVA:
                        Material liq = (s == Square.LAVA) ? Material.STATIONARY_LAVA : Material.STATIONARY_WATER;
                        renderTile(handler, xx, top.getBlockY(), zz, flr, liq, air, air, roo, over);
                        break;
                    case BARS:
                        renderTile(handler, xx, top.getBlockY(), zz, undr, flr, air, bar, roo, over);
                        break;
                    case FLOOR:
                    case FIXEDFLOOR:
                        renderTile(handler, xx, top.getBlockY(), zz, undr, flr, air, air, roo, over);
                        break;
                    case FIXEDFLOORUP:
                        renderTile(handler, xx, top.getBlockY(), zz, undr, flr, air, air, roo, cob);
                        break;
                    case FIXEDFLOORDOWN:
                        renderTile(handler, xx, top.getBlockY(), zz, flr, flr, air, air, roo, over);
                        break;
                    case DOOR:
                    case WEB:
                    case ARCH:
                        renderTile(handler, xx, top.getBlockY(), zz, undr, flr, air, cob, ecob, over);
                        break;
                    case HIDDEN:
                        renderTile(handler, xx, top.getBlockY(), zz, cob, air, air, cob, ecob, over);
                        break;
                    case ENCHANT:
                    case BOOKCASE:
                    case BOOKCASE2:
                    case WORKBENCH:
                    case SHROOM:
                    case FURNACE:
                    case CAKE:
                    case TORCH:
                    case ANVIL:
                    case SOULSAND:
                    case BED_F:
                    case BED_H:
                    case SIGNPOST:
                    case BIGCHEST:
                    case MIDCHEST:
                    case EMPTYCHEST:
                    case CHEST:
                        renderTile(handler, xx, top.getBlockY(), zz, undr, flr, air, air, roo, over);
                        break;
                    case ARROW:
                        renderTile(handler, xx, top.getBlockY(), zz, cob, cob, cob, cob, ecob, over);
                        break;
                    case RED1:
                    case RED2:
                    case PRESSURE:
                        renderTile(handler, xx, top.getBlockY(), zz, undr, flr, air, air, roo, over);
                        break;
                    case SPAWNER:
                        renderTile(handler, xx, top.getBlockY(), zz, cob, flr, air, air, roo, over);
                        break;
                    case O_FLOOR:
                    case O_TORCH:
                        renderTile(handler, xx, top.getBlockY(), zz, undr, flr, air, air, over, over);
                        break;
                    case UP:
                        renderTile(handler, xx, top.getBlockY(), zz, undr, flr, air, air, air, air);
                        break;
                    case DOWN:
                        if (can_go_lower)
                            renderTile(handler, xx, top.getBlockY(), zz, air, air, air, air, roo, over);
                        else
                            renderTile(handler, xx, top.getBlockY(), zz, undr, flr, air, air, roo, over);
                        break;
                    default:
                        if (cnf.UnderFill() || cnf.OverFill())
                            renderTile(handler, xx, top.getBlockY(), zz, undr, undr, cob, cob, ecob, over);
                        break;
                }

                if (s == Square.PRESSURE) {
                    handler.add(world, xx, room_l, zz, Material.STONE_PLATE, Position.HIGH);
                    //handler.addHigh(world,xx,floor_h-1,zz,Material.AIR);
                    handler.add(world, xx, floor_h - 1, zz, Material.REDSTONE_WIRE, Position.LOW);
                }
                if (s == Square.RED1) {
                    handler.add(world, xx, floor_h - 1, zz, Material.AIR, Position.HIGH);
                    handler.add(world, xx, floor_h - 2, zz, Material.REDSTONE_TORCH_ON, getRed1Code(x, y), Position.LOW);
                }
                if (s == Square.RED2) {
                    handler.add(world, xx, floor_h - 1, zz, Material.AIR, Position.HIGH);
                    handler.add(world, xx, floor_h - 2, zz, Material.REDSTONE_WIRE, Position.LOW);
                }
                if (s == Square.ARROW) {
                    List<ItemStack> stuff = new ArrayList<>();
                    CatLoot.fillChest(stuff, cnf.TrapList());
                    byte code = getTrapCode(x, y);
                    handler.add(world, xx, room_l, zz, Material.DISPENSER, code, stuff, Position.HIGH);
                    handler.add(world, xx, floor_h - 1, zz, Material.REDSTONE_TORCH_OFF, (byte) 5, Position.LOW);
                }
                if (s == Square.HIDDEN) {
                    int small = (cnf.Chance(50)) ? 1 : 0;
                    handler.add(world, xx, floor_h - 2, zz, Material.REDSTONE_TORCH_ON, Position.HIGH);
                    handler.add(world, xx, floor_h, zz, Material.PISTON_EXTENSION, (byte) 9, Position.HIGH);
                    handler.add(world, xx, floor_h - 1, zz, Material.PISTON_STICKY_BASE, (byte) 9, Position.HIGH);
                    handler.add(world, xx, room_l + (1 - small), zz, major.getMat(), major.getCode(), Position.HIGH);
                    handler.add(world, xx, room_l + small, zz, minor.getMat(), minor.getCode(), Position.HIGH);
                }
                if (s == Square.CHEST || s == Square.MIDCHEST ||
                        s == Square.BIGCHEST || s == Square.EMPTYCHEST) {
                    List<ItemStack> chest = new ArrayList<>();

                    if (s == Square.BIGCHEST) {
                        chest = CatLoot.bigChest(cnf, chest);
                        handler.add(world, xx, floor_h, zz, Material.GRASS, Position.HIGH);
                        if (cnf.ResetButton() || cnf.RecallButton())
                            handler.add(world, xx, room_l + 1, zz, Material.STONE_BUTTON, getButtonCode(x, y), Position.LOW);
                        BlockChange n = new BlockChange(world.getBlockAt(xx, room_l + 2, zz), Material.WALL_SIGN, getLadderCode(x, y));
                        n.setLine(1, "End of dungeon");

                        // These messages don't change as the config changes :(
                        if (cnf.ResetButton()) n.setLine(3, "press to reset");
                         else if (cnf.RecallButton()) n.setLine(3, "press to leave");
                        handler.add(n, Position.LOW);
                    } else if (s == Square.MIDCHEST) {
                        chest = CatLoot.midChest(cnf, chest);

                        // Swept ore goes into chest on first time around (not after a reset)
                        if (cnf.MedSweepOre()) {
                            if (coal > 0) chest.add(new ItemStack(Material.COAL, coal));
                            if (iron > 0) chest.add(new ItemStack(Material.IRON_ORE, iron));
                            if (lapis > 0) chest.add(new ItemStack(Material.INK_SACK, lapis * 6, (short) 0, (byte) 4));
                            if (redstone > 0) chest.add(new ItemStack(Material.REDSTONE, redstone * 4));
                            if (diamond > 0) chest.add(new ItemStack(Material.DIAMOND, diamond));
                            if (gold > 0) chest.add(new ItemStack(Material.GOLD_ORE, gold));
                            coal = iron = lapis = redstone = diamond = gold = 0;
                        }
                    } else if (s == Square.CHEST) chest = CatLoot.smallChest(cnf, chest);
                    handler.add(world, xx, room_l, zz, Material.CHEST, chest, Position.HIGH);
                }

                if (s == Square.DOWN && !can_go_lower) {
                    List<ItemStack> chest = new ArrayList<>();
                    chest = CatLoot.bigChest(cnf, chest);
                    handler.add(world, xx, room_l, zz, Material.CHEST, chest, Position.HIGH);
                }
                if (s == Square.WINDOW) handler.add(world, xx, room_l + 1, zz, Material.THIN_GLASS, Position.HIGH);
                if (s == Square.HIGH_BARS) handler.add(world, xx, room_l + 1, zz, Material.IRON_FENCE, Position.HIGH);
                if (s == Square.BARS) {
                    handler.add(world, xx, room_l, zz, Material.IRON_FENCE, Position.HIGH);
                    handler.add(world, xx, room_l + 1, zz, Material.IRON_FENCE, Position.HIGH);
                }
                if (s == Square.CAKE) {
                    handler.add(world, xx, room_l, zz, Material.FENCE, Position.HIGH);
                    handler.add(world, xx, room_l + 1, zz, Material.CAKE_BLOCK, Position.HIGH);
                }
                if (s == Square.SOULSAND) handler.add(world, xx, floor_h, zz, Material.SOUL_SAND, Position.HIGH);
                if (s == Square.WEB) {
                    handler.add(world, xx, room_l, zz, Material.WEB, Position.HIGH);
                    handler.add(world, xx, room_l + 1, zz, Material.WEB, Position.HIGH);
                }
                if (s == Square.WORKBENCH) {
                    handler.add(world, xx, room_l, zz, Material.WORKBENCH, Position.HIGH);
                    handler.add(world, xx, room_l + 1, zz, Material.BREWING_STAND, Position.HIGH);
                }
                if (s == Square.BOOKCASE || s == Square.BOOKCASE2)
                    handler.add(world, xx, room_l, zz, Material.BOOKSHELF, Position.HIGH);
                if (s == Square.BOOKCASE2) handler.add(world, xx, room_l + 1, zz, Material.BOOKSHELF, Position.HIGH);
                if (s == Square.ENCHANT) handler.add(world, xx, room_l, zz, Material.ENCHANTMENT_TABLE, Position.HIGH);
                if (s == Square.ANVIL) handler.add(world, xx, room_l, zz, Material.ANVIL, Position.HIGH);
            }

        // Second pass - Place all the items (ladders, doors etc)
        for (int x = 0; x < g.sx(); x++) {
            for (int y = 0; y < g.sy(); y++) {
                // 3D-2D
                Square s = g.get(x, y);
                int xx = top.getBlockX() + x - level.start().x;
                int zz = top.getBlockZ() - y + level.start().y;
                if (s == Square.DOOR) {
                    //byte code = g.getDoorCode(x,y);
                    Material dt = cnf.DoorMaterial();
                    byte lower = g.getDoorLowerCode(x, y);
                    byte upper = g.getDoorUpperCode(x, y);
                    handler.add(world, xx, room_l, zz, dt, lower, Position.LOW);
                    handler.add(world, xx, room_l + 1, zz, dt, upper, Position.LOW);
                }
                if (s == Square.UP) {
                    byte code = getLadderCode(x, y);
                    for (int yy = room_l; yy <= room_h; yy++) {
                        handler.add(world, xx, yy, zz, Material.LADDER, code, Position.LOW);
                    }
                    for (int yy = roof_l; yy <= roof_h; yy++) {
                        handler.add(world, xx, yy, zz, Material.LADDER, code, Position.LOW);
                    }
                }
                if (s == Square.FURNACE) {
                    byte code = getFurnaceCode(x, y);
                    handler.add(world, xx, room_l, zz, Material.FURNACE, code, Position.LOW);
                }
                if (s == Square.SHROOM) {
                    handler.add(world, xx, room_l, zz, cnf.ShroomType(), Position.LOW);
                }
                if (s == Square.O_TORCH) {
                    handler.add(world, xx, room_l, zz, Material.TORCH, (byte) 5, Position.LOW);
                }
                if (s == Square.TORCH) {
                    handler.add(world, xx, room_l + 2, zz, Material.TORCH, Position.LOW);
                    if (cube.isHut())
                        handler.add(world, xx, roof_l + 1, zz, Material.TORCH, Position.LOW);
                }
                if (s == Square.BED_H) {
                    byte code = (byte) (getBedCode(x, y) | 8);
                    handler.add(world, xx, room_l, zz, Material.BED_BLOCK, code, Position.LOW);
                }
                if (s == Square.BED_F) {
                    byte code = getBedCode(x, y);
                    handler.add(world, xx, room_l, zz, Material.BED_BLOCK, code, Position.LOW);
                }
                if (s == Square.SIGNPOST) {
                    //byte code = getBedCode(x,y);
                    byte code = getSignCode(x, y);
                    BlockChange n;
                    if (code == 0) n = new BlockChange(world.getBlockAt(xx, room_l, zz), Material.SIGN_POST, (byte) 1);
                    else n = new BlockChange(world.getBlockAt(xx, room_l + 1, zz), Material.WALL_SIGN, code);
                    if (info != null) for (int i = 0; i < 4; i++) n.setLine(i, info[i]);
                    handler.add(n, Position.LOW);
                }
                if (s == Square.SPAWNER) {
                    BlockChange n = new BlockChange(world.getBlockAt(xx, room_l, zz), Material.MOB_SPAWNER);
                    EntityType type = cnf.SpawnerType();
                    n.setSpawner(type);
                    handler.add(n, Position.LOW);
                    if (type == EntityType.WOLF) {
                        handler.add(world, xx, floor_h, zz, Material.GRASS, Position.LOW);
                        handler.add(world, xx + 1, floor_h, zz, Material.GRASS, Position.LOW);
                        handler.add(world, xx - 1, floor_h, zz, Material.GRASS, Position.LOW);
                        handler.add(world, xx, floor_h, zz + 1, Material.GRASS, Position.LOW);
                        handler.add(world, xx, floor_h, zz - 1, Material.GRASS, Position.LOW);
                    }
                }
                if (s == Square.DOWN && can_go_lower) {
                    byte code = getLadderCode(x, y);
                    if (level.end_dir() != null) {
                        code = getLadderCode(level.end_dir());
                    }
                    for (int yy = floor_l; yy <= floor_h; yy++) {
                        handler.add(world, xx, yy, zz, Material.LADDER, code, Position.LOW);
                    }
                    code = getTrapDoorCode(x, y);
                    handler.add(world, xx, room_l, zz, Material.TRAP_DOOR, code, Position.LOW);
                }
            }
        }
        cube.refillChests(cnf);
    }

    public void delete(Catacombs plugin, BlockChangeHandler handler) {
        cube.clearMonsters();
        cube.unrender(handler, cnf.emptyChest(), roofDepth + roomDepth);
    }

    public void reset(Catacombs plugin) {
        if (!cube.isHut()) {
            cube.removeTorch();
            cube.refillChests(cnf);
        }
        cube.restoreCake();
        cube.clearMonsters();
        cube.closeDoors();
    }

    public void clearMonsters(Catacombs plugin) {
        cube.clearMonsters();
    }

    public int fixSecretDoors() {
        return cube.fixSecretDoors();
    }

    public int changeDoorsToIron() {
        return cube.changeDoorsToIron();
    }

    public int fixDoors() {
        return cube.fixDoors();
    }

    public void suspend(Catacombs plugin, CatMat mat) {
        if (plugin != null)
            cube.clearMonsters();
        //cube.suspend();
        if (mat != null)
            cube.addGlow(mat, roofDepth);
    }

    public void buildWindows(Material mat) {
        cube.buildWindows(new CatMat(mat), floorDepth + 1);
    }

    public void enable(CatMat mat) {
        //cube.enable();
        if (mat != null)
            cube.removeGlow(mat, roofDepth);
    }

    private CatCuboid getNaturalCuboid(CatConfig cnf, World world, int ox, int oy, int oz) {
        int lx = ox;
        int lz = oz;
        int hx = ox;
        int hz = oz;
        CatCuboid space;

        if (world == null)
            return new CatCuboid(world, top.getBlockX() - 10, top.getBlockY(), top.getBlockZ() - 10, top.getBlockX() + 10, top.getBlockY() - levelDepth + 1, top.getBlockZ() + 10);

        int iteration = 1;

        int changed;
        do {
            Boolean natural;
            int px, pz;
            changed = 0;

            // Try expand in low X direction
            for (px = lx - 1, pz = lz, natural = true; pz <= hz && natural; pz++)
                natural = natural && areBlocksNatural(px, oy, pz, 0, 1, 0, levelDepth);
            if (natural && ox - lx < cnf.RadiusMax()) {
                changed++;
                lx--;
            }

            // Try expand in High X direction
            for (px = hx + 1, pz = lz, natural = true; pz <= hz && natural; pz++)
                natural = natural && areBlocksNatural(px, oy, pz, 0, 1, 0, levelDepth);
            if (natural && hx - ox < cnf.RadiusMax()) {
                changed++;
                hx++;
            }

            // Try expand in low Z direction
            for (px = lx, pz = lz - 1, natural = true; px <= hx && natural; px++)
                natural = natural && areBlocksNatural(px, oy, pz, 0, 1, 0, levelDepth);
            if (natural && oz - lz < cnf.RadiusMax()) {
                changed++;
                lz--;
            }

            // Try expand in High Z direction
            for (px = lx, pz = hz + 1, natural = true; px <= hx && natural; px++)
                natural = natural && areBlocksNatural(px, oy, pz, 0, 1, 0, levelDepth);
            if (natural && hz - oz < cnf.RadiusMax()) {
                changed++;
                hz++;
            }

            iteration++;
        } while (changed > 0 && iteration < 256);

        space = new CatCuboid(world, lx, oy, lz, hx, oy + levelDepth - 1, hz);
        return space;
    }

    private Boolean areBlocksNatural(int cx, int cy, int cz, int dx, int dy, int dz, int cnt) {
        // For testing
        if (world == null)
            return true;

        for (int i = 1; i <= cnt; i++) {
            Block blk = world.getBlockAt(cx, cy, cz);
            if (!cnf.isNatural(blk)) {
                return false;
            }
            cx += dx;
            cy += dy;
            cz += dz;
        }
        return true;
    }

    public byte getTrapDoorCode(int x, int y) {
        Direction dir = level.grid().getBackWallDir(x, y);
        switch (dir) {
            case NORTH:
                return 2;
            case EAST:
                return 1;
            case SOUTH:
                return 3;
            case WEST:
                return 0;
        }
        return 0;
    }

    public byte getSignCode(int x, int y) {
        return getLadderCode(level.grid().getBackWallDir(x, y));
    }

    public byte getLadderCode(int x, int y) {
        if (cube.isHut())
            return 0;
        return getLadderCode(level.grid().getBackWallDir(x, y));
    }

    public byte getButtonCode(int x, int y) {
        return getButtonCode(level.grid().getBackWallDir(x, y));
    }

    public byte getLadderCode(Direction dir) {
        switch (dir) {
            case SOUTH:
                return 2;
            case NORTH:
                return 3;
            case EAST:
                return 4;
            case WEST:
                return 5;
        }
        return 0;
    }

    public byte getButtonCode(Direction dir) {
        switch (dir) {
            case SOUTH:
                return 4;
            case NORTH:
                return 3;
            case EAST:
                return 2;
            case WEST:
                return 1;
        }
        return 0;
    }

    public byte getFurnaceCode(int x, int y) {
        Direction dir = level.grid().getFloorDir(x, y);
        switch (dir) {
            case NORTH:
                return 4;
            case EAST:
                return 5;
            case SOUTH:
                return 3;
            case WEST:
                return 2;
        }
        return 4;
    }

    public byte getTrapCode(int x, int y) {
        Direction dir = level.grid().getTrapDir(x, y);
        switch (dir) {
            case NORTH:
                return 2;
            case EAST:
                return 5;
            case SOUTH:
                return 3;
            case WEST:
                return 4;
        }
        return 4;
    }

    public byte getRed1Code(int x, int y) {
        Direction dir = level.grid().getPlateDir(x, y);
        switch (dir) {
            case NORTH:
                return 3;
            case EAST:
                return 2;
            case SOUTH:
                return 4;
            case WEST:
                return 1;
        }
        return 4;
    }

    public byte getBedCode(int x, int y) {
        Direction dir = level.grid().getBedDir(x, y);
        switch (dir) {
            case NORTH:
                return 2;
            case EAST:
                return 3;
            case SOUTH:
                return 0;
            case WEST:
                return 1;
        }
        return 0;
    }

    public byte getDoorLowerCode(int x, int y) {
        Grid grid = level.grid();
        if (grid.get(x + 1, y).isWall() && grid.get(x - 1, y).isWall())
            return 3;
        if (grid.get(x, y + 1).isWall() && grid.get(x, y - 1).isWall())
            return 0;
        if (grid.get(x + 1, y).isWall()) return 6;
        if (grid.get(x - 1, y).isWall()) return 3;
        if (grid.get(x, y + 1).isWall()) return 0;
        if (grid.get(x, y - 1).isWall()) return 7;
        return 0;
    }
}
