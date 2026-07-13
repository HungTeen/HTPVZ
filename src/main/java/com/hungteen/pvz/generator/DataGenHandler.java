package com.hungteen.pvz.generator;

import com.hungteen.pvz.generator.loot.LootModifierGen;
import com.hungteen.pvz.generator.tag.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;

public class DataGenHandler {
    public static void dataGen(GatherDataEvent ev) {
        DataGenerator g = ev.getGenerator();
        ExistingFileHelper helper = ev.getExistingFileHelper();
        boolean i = ev.includeServer();

        g.addProvider(i, new BlockModelGen(g, helper));
        g.addProvider(i, new ItemModelGen(g, helper));
        
        BlockTagsProvider blockTag = new BlockTagGen(g, helper);
        g.addProvider(i, blockTag);
        g.addProvider(i, new ItemTagGen(g, blockTag, helper));
        g.addProvider(i, new EntityTagGen(g, helper));
        g.addProvider(i, new BannerPatternTagGen(g, helper));
        g.addProvider(i, new BiomeTagGen(g, helper));
        g.addProvider(i, new StructureTagGen(g, helper));

        g.addProvider(i, new InvasionTypeGen(g));

        g.addProvider(i, new RecipeGen(g));
        g.addProvider(i, new LootGen(g));
        g.addProvider(i, new LootModifierGen(g));

        g.addProvider(i, new SoundGen(g, helper));
    }

}
