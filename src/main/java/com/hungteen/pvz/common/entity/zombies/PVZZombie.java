package com.hungteen.pvz.common.entity.zombies;

import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class PVZZombie extends Zombie {
    ResourceLocation dimensionStyle;
    public static Consumer<Entity> coneHead_zombie_consumer = (entity) -> {entity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(PVZItems.CONE_HELMET.get()));};
    public static Consumer<Entity> bucketHead_zombie_consumer = (entity) -> {entity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(PVZItems.BUCKET_HELMET.get()));};
    public PVZZombie(EntityType<? extends Zombie> p_34271_, Level p_34272_) {
        super(p_34271_, p_34272_);
        dimensionStyle = this.level.dimension().location();
    }
    protected boolean isSunSensitive() {
        return false;
    }
    protected boolean convertsInWater() {
        return false;
    }
    protected ItemStack getSkull() {
        return ItemStack.EMPTY;
    }
    public int getExperienceReward() {
        return 0;
    }
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("style_path", dimensionStyle.getPath());
        tag.putString("style_space", dimensionStyle.getNamespace());
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        dimensionStyle = new ResourceLocation(tag.getString("style_space"), tag.getString("style_path"));
    }
    public ResourceLocation getStyle() {
        return dimensionStyle;
    }
}
