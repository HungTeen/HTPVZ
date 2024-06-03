package com.hungteen.pvz.common.item;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.common.entity.ModelPartEntity;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PVZShieldItem extends ShieldItem implements IDropWhenBroken{
    public PVZShieldItem(Properties p_43089_) {
        super(p_43089_);
    }

    public void clientBroken(Vec3 pos, Level level) {
        ItemStack item = this.getDefaultInstance();
        item.setDamageValue(this.getMaxDamage(item) - 1);
        if (level.isClientSide && PVZConfig.Client.zombiesDropParts.get()) {
            new ModelPartEntity(level, item).pos(pos).rotation(new Vec3(9, 0, 0)).join(level);
        }
    }

    public static void registerProperties(){
        ItemProperties.register(PVZItems.SCREEN_DOOR_SHIELD.get(), new ResourceLocation("durability"),
                (itemStack, level, entity, seed) -> (150 - itemStack.getDamageValue()) / 51);
        ItemProperties.register(PVZItems.SCREEN_DOOR_SHIELD.get(), new ResourceLocation("blocking"),
                (itemStack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == itemStack ? 1.0F : 0.0F);
    }
}
