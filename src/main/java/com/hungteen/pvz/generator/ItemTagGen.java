package com.hungteen.pvz.generator;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.tags.PVZItemTags;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ItemTagGen extends ItemTagsProvider {

    public ItemTagGen(DataGenerator p_126530_, BlockTagsProvider p_126531_, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_126530_, p_126531_, PVZMod.MODID, existingFileHelper);
    }

    @Override
    public void addTags(){
        //atItemRegister
        PVZItems.tagMap.forEach((item, tagList)-> tagList.forEach((tag) -> this.tag(tag).add(item.get())));
        //woodSet
        this.copy(BlockTags.PLANKS, ItemTags.PLANKS);
        this.copy(BlockTags.LOGS, ItemTags.LOGS);
        this.copy(BlockTags.LOGS_THAT_BURN, ItemTags.LOGS_THAT_BURN);
        this.copy(BlockTags.WOODEN_BUTTONS, ItemTags.WOODEN_BUTTONS);
        this.copy(BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS);
        this.copy(BlockTags.SAPLINGS, ItemTags.SAPLINGS);
        this.copy(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
        this.copy(BlockTags.LEAVES, ItemTags.LEAVES);
        this.copy(BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS);
        this.copy(BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS);
        this.copy(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES);
        this.copy(BlockTags.STANDING_SIGNS, ItemTags.SIGNS);
        for (TagKey<Block> i : BlockTagGen.LOGS){
            TagKey<Item> tag = tag(i.location().getPath());
            this.copy(i, tag);
            LOGS.add(tag);
        }
        //others
        this.tag(PVZItemTags.TO_TERRA_ESSENCE).add(Blocks.STONE.asItem(), Blocks.DEEPSLATE.asItem(), 
                Blocks.GRANITE.asItem(), Blocks.DIORITE.asItem(), Blocks.ANDESITE.asItem(), Blocks.TUFF.asItem());
        this.tag(PVZItemTags.TO_LUX_ESSENCE).add(Blocks.GLOWSTONE.asItem(), Blocks.SHROOMLIGHT.asItem(),
                Blocks.PEARLESCENT_FROGLIGHT.asItem(), Blocks.OCHRE_FROGLIGHT.asItem(), Blocks.VERDANT_FROGLIGHT.asItem());
    }

    public static final List<TagKey<Item>> LOGS = new ArrayList<>();

    private static TagKey<Item> tag(String path) {
        return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(PVZMod.MODID + ":" + path));
    }
}
