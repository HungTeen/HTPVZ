package com.hungteen.pvz.generator;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.register.PVZBlocks;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.List;

public class BlockModelGen extends BlockStateProvider {

    public BlockModelGen(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, PVZMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        PVZBlocks.modelMap.forEach((blockRegisterObj, model) -> {
            Block block = blockRegisterObj.get();
            PVZMod.LOGGER.info("Gen Block Model: "+block);
            switch (model.getFirst()) {
                case Simple -> simple(block, model.getSecond());
                case Column -> column((RotatedPillarBlock) block, model.getSecond());
                //Modeled ones excluded.
            }
        });
    }

    private void simple(Block block, List<ResourceLocation> list) {
        if (list.equals(List.of())){
            simpleBlock(block);
        } else {
            simpleBlock(block, models().cubeAll(name(block), list.get(0)));
        }
    }
    private void column(RotatedPillarBlock block, List<ResourceLocation> list){
        if (list.equals((List.of()))){
            logBlock(block);
        } else {
            axisBlock(block, list.get(0), list.get(1));
        }
    }
    private void slab(SlabBlock block, List list){
//        slabBlock(block, new ResourceLocation(), texture);
    }




    //definitions
    private String name(Block block) {
        return Registry.BLOCK.getKey(block).getPath();
    }
}
