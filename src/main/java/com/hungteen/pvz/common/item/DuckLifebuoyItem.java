package com.hungteen.pvz.common.item;

import com.google.common.collect.Multimap;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.client.model.attached.DuckLifebuoyModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Wearable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
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
        return slot == EquipmentSlot.LEGS ? "pvz:textures/models/armor/" + Registry.ITEM.getKey(stack.getItem()).getPath() + ".png" : null;
    }


    /**onArmorTick only runs when the entity equipping it is player. So Added This.*/
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent ev) {
        LivingEntity livingEntity = ev.getEntity();
        if (livingEntity.level.isClientSide() && ! (ev.getEntity() instanceof Player)) {
            return;
        }
        boolean isInWater = false;
        Vec3 lifeBuoyPos = livingEntity.position().add(0, livingEntity.getBbHeight() * 0.55, 0);
        if (! livingEntity.level.getFluidState(new BlockPos(lifeBuoyPos.x, lifeBuoyPos.y, lifeBuoyPos.z)).isEmpty()) {
            if (livingEntity.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof DuckLifebuoyItem item && item.getSlot() == EquipmentSlot.FEET) {
                livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(0, 0.04, 0));
                isInWater = true;
                if (livingEntity instanceof PathfinderMob mob && ! mob.getNavigation().isDone()) {
                    if (! mob.getNavigation().canFloat() && mob.isInWater() && mob.tickCount % 3 == 0) {
                        //TODO find a way to decrease calculation.
                        mob.getNavigation().setCanFloat(true);
                        mob.getNavigation().path = null;
                        mob.getNavigation().path = mob.getNavigation().createPath(mob.getNavigation().getTargetPos(), 0);
                        mob.getNavigation().setCanFloat(false);
                    }
                }
            }
        }
        AttributeInstance speed = livingEntity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && ! (livingEntity instanceof Player)) {
            if (isInWater && livingEntity.getPose() == Pose.STANDING /*getWaterSlowDown() is not callable...*/ && speed.getModifier(SPEED_MODIFIER_UUID) == null) {
                speed.addTransientModifier(new AttributeModifier(SPEED_MODIFIER_UUID,
                        "duck_lifebuoy", 5, AttributeModifier.Operation.MULTIPLY_TOTAL));
            } else if (speed.getModifier(SPEED_MODIFIER_UUID) != null){
                speed.removeModifier(SPEED_MODIFIER_UUID);
            }
        }
    }

    private static class DuckLifebuoyClients implements IClientItemExtensions {
        private static final DuckLifebuoyClients INSTANCE = new DuckLifebuoyClients();
        @Override
        public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
            EntityModelSet models = Minecraft.getInstance().getEntityModels();
            ModelPart root = models.bakeLayer(PVZLayerHandler.LayerLocationMap.get(Registry.ITEM.getKey(itemStack.getItem()).getPath() + ":main"));
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
