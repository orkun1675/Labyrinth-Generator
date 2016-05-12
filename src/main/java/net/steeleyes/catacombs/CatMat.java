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

import org.bukkit.Material;
import org.bukkit.block.Block;

@SuppressWarnings("deprecation")
public class CatMat {
    private Material mat;
    private byte code = 0;
    private Boolean hasCode = false;

    public CatMat(Material mat) {
        this.mat = mat;
    }

    public CatMat(Block blk) {
        this.mat = blk.getType();
        if (blk.getData() > 0) {
            this.code = blk.getData();
            hasCode = true;
        }
    }

    public CatMat(Material mat, byte code) {
        this.mat = mat;
        this.code = code;
        hasCode = true;
    }

    @SuppressWarnings("unused")
    static public CatMat parseMaterial(String orig) {
        CatMat m = null;
        String name = orig;
        byte code = -1;
        if (name.contains(":")) {
            String tmp[] = name.split(":");
            name = tmp[0];
            try {
                code = Byte.parseByte(tmp[1]);
            } catch (Exception e) {
                System.err.println("[Catacombs] Unknown material '" + orig + "' invalid data byte, expecting a number");
                return null;
            }
        }
        Material mat = Material.matchMaterial(name);
        if (mat == null || !mat.isBlock()) {
            System.err.println("[Catacombs] Invalid block material '" + orig + "'");
            return null;
        }
        if (code >= 0) return new CatMat(mat, code);
        return new CatMat(mat);
    }
    @Override
    public String toString() {
        if (hasCode) return mat.toString() + ":" + code;
        return mat.toString();
    }

    public Boolean equals(CatMat that) {
        if (that == null) return false;
        if (this.hasCode || that.hasCode) {
            return this.mat == that.mat && this.code == that.code;
        }
        return this.mat == that.mat;
    }

    public Boolean equals(Block blk) {
        if (this.hasCode || blk.getData() > 0) return this.mat == blk.getType() && this.code == blk.getData();
        return this.mat == blk.getType();
    }

    public Boolean is(Material that) {
        return !this.hasCode && this.mat == that;
    }

    public void setBlock(Block blk) {
        if (this.hasCode) blk.setTypeIdAndData(mat.getId(), this.code, false);
        else blk.setType(mat);
    }

    public void getBlock(Block blk) {
        Material m = blk.getType();
        byte c = blk.getData();
        if (c > 0) {
            code = c;
            hasCode = true;
        } else {
            code = -1;
            hasCode = false;
        }
        mat = m;
    }

    public void get(CatMat that) {
        hasCode = that.hasCode;
        mat = that.mat;
        code = that.code;
    }

    public byte getCode() {
        return code;
    }

    public Boolean getHasCode() {
        return hasCode;
    }

    public Material getMat() {
        return mat;
    }
}
