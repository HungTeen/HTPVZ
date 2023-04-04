package com.hungteen.pvz.generator;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;

public class DataGenHandler {
    public static void dataGen(GatherDataEvent ev) {
        DataGenerator g = ev.getGenerator();
        g.addProvider(true, new BlockModelGen(g, ev.getExistingFileHelper()));
        g.addProvider(true, new ItemModelGen(g, ev.getExistingFileHelper()));
    }

}
