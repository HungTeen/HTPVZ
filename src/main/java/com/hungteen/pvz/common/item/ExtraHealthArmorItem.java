package com.hungteen.pvz.common.item;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.client.model.attached.BucketHelmetModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Calendar;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class ExtraHealthArmorItem extends ArmorItem {
    public ExtraHealthArmorItem(ArmorMaterial material, Properties properties, EquipmentSlot armorType) {
        super(material, armorType, properties);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "pvz:textures/models/armor/" + Registry.ITEM.getKey(stack.getItem()).getPath() + "_" + (stack.getDamageValue() * 3 / stack.getMaxDamage() + ".png");
    }

    @SubscribeEvent
    public static void handleHurt(LivingHurtEvent event) {
        if (! event.getSource().isBypassArmor()) {
            for (ItemStack stack : event.getEntity().getArmorSlots()) {
                if (stack.getItem() instanceof ExtraHealthArmorItem) {
                    int blocked = (int) Math.min(stack.getMaxDamage() - stack.getDamageValue(), event.getAmount());
                    stack.hurtAndBreak(blocked, event.getEntity(), (entity) -> {});
                    event.setAmount(event.getAmount() - blocked);
                }
            }
        }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(2) + 1 == 4 && calendar.get(5) <= 3) {
            super.initializeClient(consumer);
        } else {
            consumer.accept(ExtraHealthArmorClients.INSTANCE);
        }
    }

    private static class ExtraHealthArmorClients implements IClientItemExtensions {
        private static final ExtraHealthArmorClients INSTANCE = new ExtraHealthArmorClients();
        @Override
        public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original)
        {
            EntityModelSet models = Minecraft.getInstance().getEntityModels();
            ModelPart root = models.bakeLayer(PVZLayerHandler.LayerLocationMap.get(Registry.ITEM.getKey(itemStack.getItem()).getPath() + ":main"));
            return new BucketHelmetModel<>(root);
        }
    }

}
