package com.hungteen.pvz.generator;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.register.PVZBlocks;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static net.minecraft.client.renderer.RenderType.solid;

public class BlockModelGen extends BlockStateProvider {

    public BlockModelGen(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, PVZMod.MODID, existingFileHelper);
    }

    List<Block> storedBlocks = new ArrayList<>();

    @Override
    protected void registerStatesAndModels() {
        PVZBlocks.modelList.forEach((pair) -> {
            Block block = pair.getFirst().get();
            String renderType = PVZBlocks.renderTypeMap.get(pair.getFirst()) == null ? "solid" : PVZBlocks.renderTypeMap.get(pair.getFirst());
            PVZMod.LOGGER.info("Gen Block Model: "+block);

            switch (pair.getSecond().getFirst()) {
                case Simple -> simple(block, pair.getSecond().getSecond(), renderType);
                case Column -> column((RotatedPillarBlock) block, pair.getSecond().getSecond(), renderType);
                case Cross -> cross(block, pair.getSecond().getSecond(), renderType);
                case Slab -> slab((SlabBlock) block, pair.getSecond().getSecond(), renderType);
                case Stairs -> stair((StairBlock) block, pair.getSecond().getSecond(), renderType);
                case Door -> door((DoorBlock) block, pair.getSecond().getSecond(), renderType);
                case Trapdoor -> trapdoor((TrapDoorBlock) block, pair.getSecond().getSecond(), renderType);
                case Fence -> fence((FenceBlock) block, pair.getSecond().getSecond(), renderType);
                case Gate -> gate((FenceGateBlock) block, pair.getSecond().getSecond(), renderType);
                case Button -> button((ButtonBlock) block, pair.getSecond().getSecond(), renderType);
                case Plate -> plate((PressurePlateBlock) block, pair.getSecond().getSecond(), renderType);
                case Sign -> sign((StandingSignBlock) block);
                case WallSign -> wallsign((WallSignBlock) block, pair.getSecond().getSecond(), renderType);
                //Modeled ones excluded.
            }
        });
    }

    private void simple(Block block, List<ResourceLocation> list, String renderType) {
        if (list.size() == 0){
            simpleBlock(block, models().cubeAll(path(block), blockTexture(block)).renderType(renderType));
        } else {
            simpleBlock(block, models().cubeAll(path(block), list.get(0)).renderType(renderType));
        }
    }
    private void column(RotatedPillarBlock block, List<ResourceLocation> list, String renderType){
        if (list.size() == 0){
            logBlockWithRenderType(block, renderType);
        } else {
            axisBlockWithRenderType(block, /*sides*/list.get(0), /*top*/list.get(1), renderType);
        }
    }
    public void cross(Block block, List<ResourceLocation> list, String renderType) {
        if (list.size() == 0){
            simpleBlock(block, models().cross(path(block), blockTexture(block)).renderType(renderType));
        } else {
            simpleBlock(block, models().cross(path(block), list.get(0)).renderType(renderType));

        }
    }
    private void slab(SlabBlock block, List<ResourceLocation> list, String renderType){
        if (list.size() == 0){
            slabBlock(block, blockTexture(block), blockTexture(block));
        } else if (list.size() == 1){
            slabBlock(block, list.get(0), list.get(0));
        } else if (list.size() == 4){
            slabBlock(block, /*double*/list.get(0), /*sides*/list.get(1), /*bottom*/list.get(2), /*top*/list.get(3));
        }
    }
    private void stair(StairBlock block, List<ResourceLocation> list, String renderType){
        if (list.size() == 0){
            stairsBlockWithRenderType(block, blockTexture(block), renderType);
        } else if (list.size() == 1){
            stairsBlockWithRenderType(block, list.get(0), renderType);
        } else if (list.size() == 3){
            stairsBlockWithRenderType(block, /*double*/list.get(0), /*bottom*/list.get(1), /*top*/list.get(2), renderType);
        }
    }
    private void door(DoorBlock block, List<ResourceLocation> list, String renderType){
        if (list.size() == 0){
            doorBlockWithRenderType(block, res(path(block) + "_bottom"), res(path(block) + "_top"), renderType);
        } else if (list.size() == 2){
            doorBlockWithRenderType(block, list.get(0), list.get(1), renderType);
        }
    }
    private void trapdoor(TrapDoorBlock block, List<ResourceLocation> list, String renderType){
        if (list.size() == 0){
            trapdoorBlockWithRenderType(block, blockTexture(block), true, renderType);
        } else if (list.size() == 1){
            trapdoorBlockWithRenderType(block, list.get(0), true, renderType);
        }
    }
    private void fence(FenceBlock block, List<ResourceLocation> list, String renderType){
        if (list.size() == 0){
            fenceBlockWithRenderType(block, blockTexture(block), renderType);
            models().fenceInventory(path(block) + "_inventory", blockTexture(block));
        } else if (list.size() == 1){
            fenceBlockWithRenderType(block, list.get(0), renderType);
            models().fenceInventory(path(block) + "_inventory", list.get(0));
        }
    }
    private void gate(FenceGateBlock block, List<ResourceLocation> list, String renderType){
        if (list.size() == 0){
            fenceGateBlockWithRenderType(block, blockTexture(block), renderType);
        } else if (list.size() == 1){
            fenceGateBlockWithRenderType(block, list.get(0), renderType);
        }
    }
    private void button(ButtonBlock block, List<ResourceLocation> list, String renderType){
        if (list.size() == 0){
            buttonBlock(block, blockTexture(block));
            models().buttonInventory(path(block) + "_inventory", blockTexture(block));
        } else if (list.size() == 1){
            buttonBlock(block, list.get(0));
            models().buttonInventory(path(block) + "_inventory", list.get(0));
        }
    }
    private void plate(PressurePlateBlock block, List<ResourceLocation> list, String renderType){
        if (list.size() == 0){
            pressurePlateBlock(block, blockTexture(block));
        } else if (list.size() == 1){
            pressurePlateBlock(block, list.get(0));
        }
    }
    private void sign(StandingSignBlock block){
        storedBlocks.add(block);
    }
    private void wallsign(WallSignBlock wall, List<ResourceLocation> list, String renderType){
        StandingSignBlock standing = (StandingSignBlock) getFromStored((block) -> block instanceof StandingSignBlock);
        storedBlocks.remove(standing);
        if (list.size() == 0){
            signBlock(standing, wall, blockTexture(standing));
        } else if (list.size() == 1) {
            signBlock(standing, wall, list.get(0));
        }
    }

    
    //definitions
    private String name(Block block) {
        return block.getName().getString();
    }
    private String path(Block block){
        return name(block).substring(name(block).indexOf("pvz.") + 4);
    }
    private static ResourceLocation res(String path){
        return new ResourceLocation(PVZMod.MODID, ModelProvider.BLOCK_FOLDER + "/" + path);
    }
    private Block getFromStored(Predicate<Block> condition){
        for (Block i : storedBlocks){
            if (condition.test(i)){
                storedBlocks.remove(i);
                return i;
            }
        }
        return null;
    }
}
