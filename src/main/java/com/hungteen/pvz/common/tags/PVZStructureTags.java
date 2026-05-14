package com.hungteen.pvz.common.tags;

import com.hungteen.pvz.util.Util;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;

public class PVZStructureTags {
    public static final TagKey<Structure> CAN_INVADE = pvzTag("can_invade");

    public static TagKey<Structure> pvzTag(String name){
        return TagKey.create(Registry.STRUCTURE_REGISTRY, Util.prefix(name));
    }
}
