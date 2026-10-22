package com.hungteen.pvz.common.item;

import com.google.common.collect.Multimap;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.client.model.attached.DuckLifebuoyModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Wearable;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class DuckLifebuoyItem extends ArmorItem implements Wearable {
    public static UUID SPEED_MODIFIER_UUID = UUID.fromString("87e0c942-d4ec-dc97-0166-79c93a5c8135");
    public DuckLifebuoyItem(Properties p_40388_) {
        super(PVZArmorMaterials.DUCK_LIFEBUOY, EquipmentSlot.LEGS, p_40388_);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(DuckLifebuoyClients.INSTANCE);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return slot == EquipmentSlot.LEGS ? "pvz:textures/models/armor/" + ForgeRegistries.ITEMS.getKey(stack.getItem()).getPath() + ".png" : null;
    }


    /**onArmorTick only runs when the entity equipping it is player. So Added This.*/
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent ev) {
        LivingEntity livingEntity = ev.getEntity();
        if (livingEntity.level.isClientSide() && ! (ev.getEntity() instanceof Player)) {
            return;
        }
        boolean lifebuoy = livingEntity.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof DuckLifebuoyItem;
        if (lifebuoy) {
            Vec3 lifeBuoyPos = livingEntity.position().add(0, livingEntity.getBbHeight() * 0.5, 0);
            if (! livingEntity.level.getFluidState(new BlockPos(lifeBuoyPos.x, lifeBuoyPos.y, lifeBuoyPos.z)).isEmpty() && livingEntity.tickCount % 2 == 0) {
                livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(0, 0.06, 0));
            }
            EntityUtil.addModifierToAttribute(livingEntity, ForgeMod.SWIM_SPEED.get(), new AttributeModifier(SPEED_MODIFIER_UUID,
                    "duck_lifebuoy", livingEntity instanceof Player ? 0.5 : 2, AttributeModifier.Operation.ADDITION));
        } else {
            EntityUtil.removeModifierFromAttribute(livingEntity, ForgeMod.SWIM_SPEED.get(), SPEED_MODIFIER_UUID);
        }
        if (livingEntity instanceof PathfinderMob mob) {
            livingEntity.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> {
                if (lifebuoy && livingEntity.getPose() == Pose.STANDING) {
                    if (! mob.getNavigation().canFloat() && ! cap.lifebuoyAdjusted) {
                        cap.lifebuoyAdjusted = true;
                        mob.getNavigation().setCanFloat(true);
                    }
                } else {
                    if (cap.lifebuoyAdjusted) {
                        cap.lifebuoyAdjusted = false;
                        mob.getNavigation().setCanFloat(false);
                    }
                }
            });
        }
    }

    private static class DuckLifebuoyClients implements IClientItemExtensions {
        private static final DuckLifebuoyClients INSTANCE = new DuckLifebuoyClients();
        @Override
        public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
            EntityModelSet models = Minecraft.getInstance().getEntityModels();
            ModelPart root = models.bakeLayer(PVZLayerHandler.LayerLocationMap.get(ForgeRegistries.ITEMS.getKey(itemStack.getItem()).getPath() + ":main"));
            return new DuckLifebuoyModel<>(root);
        }
    }

    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot p_40390_) {
        return super.getDefaultAttributeModifiers(p_40390_);
    }

    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity)
    {
        return EquipmentSlot.LEGS == armorType;
    }
}
