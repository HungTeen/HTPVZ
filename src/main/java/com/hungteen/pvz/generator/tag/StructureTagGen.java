package com.hungteen.pvz.generator.tag;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.register.PVZStructures;
import com.hungteen.pvz.common.tags.PVZStructureTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.StructureTagsProvider;
import net.minecraft.data.worldgen.Structures;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class StructureTagGen extends StructureTagsProvider {
    public StructureTagGen(DataGenerator p_236437_, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_236437_, PVZMod.MODID, existingFileHelper);
    }

    @Override
    public void addTags() {
        this.tag(PVZStructureTags.CAN_INVADE).add(PVZStructures.INVASION_RUIN.get(), PVZStructures.NETHER_INVASION_RUIN.get()
                , Structures.MINESHAFT.get(), Structures.NETHER_FOSSIL.get(), Structures.MINESHAFT_MESA.get());
    }
}
