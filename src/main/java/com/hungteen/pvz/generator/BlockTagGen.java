package com.hungteen.pvz.generator;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.register.PVZBlocks;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.hungteen.pvz.common.register.PVZBlocks.WoodSet.*;
import static com.hungteen.pvz.common.register.PVZBlocks.woodList;
import static com.hungteen.pvz.common.register.PVZBlocks.woodTypeList;

public class BlockTagGen extends BlockTagsProvider {

    public BlockTagGen(DataGenerator p_126511_, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_126511_, PVZMod.MODID, existingFileHelper);
    }
    @Override
    public void addTags(){
        //atBlockRegister
        PVZBlocks.tagMap.forEach((block, tagList)-> {
            tagList.forEach((tag) -> {
                this.tag(tag).add(block.get());
            });
        });
        //woodSet
        for (int i = 0; i < woodList.size(); i ++){
            String name = woodTypeList.get(i).name();
            LOGS.add(tag(name + "_logs"));
            this.tag(LOGS.get(i)).add(woodList.get(i).get(Log).get(),
                    woodList.get(i).get(StLog).get(),
                    woodList.get(i).get(Wood).get(),
                    woodList.get(i).get(StWood).get()
            );
        }
    }

    public static final List<TagKey<Block>> LOGS = new ArrayList<>();

    private static TagKey<Block> tag(String path) {
        return TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(PVZMod.MODID + ":" + path));
    }
}
