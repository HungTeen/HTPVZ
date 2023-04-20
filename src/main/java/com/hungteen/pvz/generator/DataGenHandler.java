package com.hungteen.pvz.generator;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;

public class DataGenHandler {
    public static void dataGen(GatherDataEvent ev) {
        DataGenerator g = ev.getGenerator();
        ExistingFileHelper helper = ev.getExistingFileHelper();
        boolean include = ev.includeServer();

        g.addProvider(include, new BlockModelGen(g, helper));
        g.addProvider(include, new ItemModelGen(g, helper));
        
        BlockTagsProvider blockTag = new BlockTagGen(g, helper);
        g.addProvider(include, blockTag);
        g.addProvider(include, new ItemTagGen(g, blockTag, helper));

        g.addProvider(include, new RecipeGen(g));
        g.addProvider(include, new LootGen(g));

    }

}
