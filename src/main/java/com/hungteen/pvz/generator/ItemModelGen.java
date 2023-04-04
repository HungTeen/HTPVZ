package com.hungteen.pvz.generator;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemModelGen extends ItemModelProvider {
    public ItemModelGen(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, PVZMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        PVZItems.modelMap.forEach((itemRegisterObj, model) -> {
            Item item = itemRegisterObj.get();
            switch (model) {
                case Simple -> simple(item);
                //Modeled and Block(gened in BlockModelGen) ones excluded.
            }
        });
    }

    public void simple(Item item){
        basicItem(item);
    }

}
