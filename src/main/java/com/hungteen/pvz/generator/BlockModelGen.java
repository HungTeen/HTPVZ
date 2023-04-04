package com.hungteen.pvz.generator;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.register.PVZBlocks;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockModelGen extends BlockStateProvider {

    public BlockModelGen(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, PVZMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        PVZBlocks.modelMap.forEach((blockRegisterObj, model) -> {
            Block block = blockRegisterObj.get();
            switch (model) {
                case Simple ->  simple(block);
                //Modeled ones excluded.
            }
        });
    }

    private void simple(Block block) {
        ModelFile tmp = cubeAll(block);
        simpleBlock(block, tmp);
        simpleBlockItem(block, tmp);
    }


    //definitions
    private String name(Block block) {
        return Registry.BLOCK.getKey(block).getPath();
    }
}
