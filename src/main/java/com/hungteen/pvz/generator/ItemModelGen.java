package com.hungteen.pvz.generator;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.util.Util;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.List;

public class ItemModelGen extends ItemModelProvider {
    public ItemModelGen(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, PVZMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        PVZItems.modelList.forEach((pair) -> {
            Item item = pair.getFirst().get();
            PVZMod.LOGGER.info("Gen Item Model: "+item);
            switch (pair.getSecond().getFirst()) {
                case Simple -> simple(item, pair.getSecond().getSecond());
                case Handheld -> handheld(item, pair.getSecond().getSecond());
                case Block -> block(item, pair.getSecond().getSecond());
                case SeedPacket -> seedPacket(item, pair.getSecond().getSecond());
                case SpawnEgg -> spawnEgg(item);
                //Modeled ones excluded.
            }
        });
    }

    public void simple(Item item, List<ResourceLocation> list){
        if (list.size() == 0){
            basicItem(item);
        } else if (list.size() == 1){
            getBuilder(item.toString())
                    .parent(new ModelFile.UncheckedModelFile("item/generated"))
                    .texture("layer0", list.get(0));
        }
    }
    public void handheld(Item item, List<ResourceLocation> list){
        if (list.size() == 0){
            getBuilder(item.toString())
                    .parent(new ModelFile.UncheckedModelFile("item/handheld"))
                    .texture("layer0", new ResourceLocation(PVZMod.MODID, "item/" + Util.name(item)));
        } else if (list.size() == 1){
            getBuilder(item.toString())
                    .parent(new ModelFile.UncheckedModelFile("item/handheld"))
                    .texture("layer0", list.get(0));
        }
    }
    public void seedPacket(Item item, List<ResourceLocation> list){
        if (list.size() == 0){
            basicItem(item);
        } else if (list.size() == 2){
            getBuilder(item.toString())
                    .parent(new ModelFile.UncheckedModelFile("item/generated"))
                    .texture("layer0", list.get(0))
                    .texture("layer1", list.get(1));
        }
    }
    public void block(Item item, List<ResourceLocation> list){
        if (list.size() == 0){
            getBuilder(item.toString())
                    .parent(new ModelFile.UncheckedModelFile(new ResourceLocation(PVZMod.MODID,"block/"+item)));
        } else if (list.size() == 1){
            getBuilder(item.toString())
                    .parent(new ModelFile.UncheckedModelFile(list.get(0)));
        }
    }
    public void spawnEgg(Item item){
        getBuilder(item.toString())
                .parent(new ModelFile.UncheckedModelFile(new ResourceLocation("item/template_spawn_egg")));
    }

}
