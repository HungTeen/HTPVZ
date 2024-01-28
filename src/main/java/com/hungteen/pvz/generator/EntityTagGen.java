package com.hungteen.pvz.generator;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.register.PVZEntities;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class EntityTagGen extends EntityTypeTagsProvider {
    public EntityTagGen(DataGenerator p_126517_, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_126517_, modId, existingFileHelper);
    }


    @Override
    public void addTags(){
        //atEntityRegister
        PVZEntities.tagMap.forEach((entity, tagList)-> tagList.forEach((tag) -> this.tag(tag).add((EntityType<?>) entity.get())));
    }

    private static TagKey<EntityType<?>> tag(String path) {
        return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(PVZMod.MODID + ":" + path));
    }

}
