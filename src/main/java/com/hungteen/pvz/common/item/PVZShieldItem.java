package com.hungteen.pvz.common.item;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.api.interfaces.IDropWhenBroken;
import com.hungteen.pvz.client.particle.ModelPartParticle;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.register.PVZSoundEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PVZShieldItem extends ShieldItem implements IDropWhenBroken {
    public PVZShieldItem(Properties p_43089_) {
        super(p_43089_);
    }

    public boolean isValidRepairItem(ItemStack p_43091_, ItemStack p_43092_) {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public void clientBroken(Vec3 pos, Level level) {
        ItemStack item = this.getDefaultInstance();
        item.setDamageValue(this.getMaxDamage(item) - 1);
        if (level.isClientSide && PVZConfig.Client.zombiesDropParts.get()) {
            ClientProxy.MC.particleEngine.add(
                    new ModelPartParticle((ClientLevel) level, item, pos)
                            .rotation(new Vec3(9, 0, 0)));
        }
    }

    public SoundEvent getBlockSound() {
        return PVZSoundEvents.DAMAGE_METAL.get();
    }

    public static void registerProperties(){
        ItemProperties.register(PVZItems.SCREEN_DOOR_SHIELD.get(), new ResourceLocation("durability"),
                (itemStack, level, entity, seed) -> (150 - itemStack.getDamageValue()) / 51);
        ItemProperties.register(PVZItems.SCREEN_DOOR_SHIELD.get(), new ResourceLocation("blocking"),
                (itemStack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == itemStack ? 1.0F : 0.0F);
    }

}
