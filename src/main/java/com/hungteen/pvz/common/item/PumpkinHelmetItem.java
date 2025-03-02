package com.hungteen.pvz.common.item;

import com.hungteen.pvz.client.model.attached.PumpkinHelmetModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class PumpkinHelmetItem extends ExtraHealthArmorItem {
    public PumpkinHelmetItem(ArmorMaterial material, Properties properties, EquipmentSlot armorType) {
        super(material, properties, armorType);
    }

    @Override
    public boolean isEnderMask(ItemStack itemStack, Player player, EnderMan enderMan) {
        return true;
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(itemStack, level, entity, slotId, isSelected);
        if (level.isClientSide) {
            return;
        }
        if (entity instanceof LivingEntity living && living.getItemBySlot(EquipmentSlot.HEAD) != itemStack) {
            if (! entity.level.isClientSide) {
                changeToPumpkin(itemStack, entity.level, entity.position().add(0, entity.getBbHeight() / 2, 0), entity.getXRot(), entity.getYRot(), entity.getDeltaMovement());
                itemStack.shrink(1);
            }
        }
        if (entity instanceof LivingEntity living && entity.tickCount % 5 == 0) {
            itemStack.hurtAndBreak(1, living, (p_41007_) -> p_41007_.broadcastBreakEvent(EquipmentSlot.HEAD));
        }
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack itemStack, ItemEntity entity) {
        if (! entity.level.isClientSide) {
            changeToPumpkin(itemStack, entity.level, entity.position().add(0, entity.getBbHeight() / 2, 0), entity.getXRot(), entity.getYRot(), entity.getDeltaMovement());
            entity.discard();
        }
        return super.onEntityItemUpdate(itemStack, entity);
    }

    public Entity changeToPumpkin(ItemStack itemStack, Level level, Vec3 pos, float xRot, float yRot, Vec3 speed) {
        CompoundTag tag = itemStack.getTag();
        if (tag == null) return null;
        tag = tag.getCompound("entity_data");
        if (tag != null) {
            Entity entity = EntityType.loadEntityRecursive(tag, level, Function.identity());
            if (entity != null) {
                entity.setPos(pos);
                entity.setXRot(xRot);
                entity.setYRot(yRot);
                if (entity instanceof LivingEntity living) {
                    living.setHealth(living.getMaxHealth() * (1 - (float) itemStack.getDamageValue() / itemStack.getMaxDamage()));
                }
                if (itemStack.hasCustomHoverName()) {
                    entity.setCustomName(itemStack.getHoverName());
                }
                level.addFreshEntity(entity);
                return entity;
            }
        }
        return null;
    }

    public void handleHurt(LivingHurtEvent event) {
        ItemStack stack = event.getEntity().getItemBySlot(slot);
        int blocked = (int) Math.min(stack.getMaxDamage() - stack.getDamageValue(), event.getAmount());
        stack.hurtAndBreak(blocked * 5 /* 5 durability equals to 1 health. */, event.getEntity(), (entity) -> {
            entity.broadcastBreakEvent(slot);
        });
        event.setAmount(event.getAmount() - blocked);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(Component.translatable("tooltip.pvz.pumpkin").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        super.appendHoverText(stack, level, tooltip, flagIn);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(PumpkinArmorClients.INSTANCE);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "pvz:textures/entity/plants/pumpkin/pumpkin_" + (stack.getDamageValue() * 3 / stack.getMaxDamage() + ".png");
    }

    public static class PumpkinArmorClients implements IClientItemExtensions {
        public static final PumpkinArmorClients INSTANCE = new PumpkinArmorClients();
        @Override
        public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
            EntityModelSet models = Minecraft.getInstance().getEntityModels();
            ModelPart root = models.bakeLayer(PVZLayerHandler.LayerLocationMap.get("pumpkin_helmet:main"));
            return new PumpkinHelmetModel<>(root);
        }
    }

}
