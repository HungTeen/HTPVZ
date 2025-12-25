package com.hungteen.pvz.common.item;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.interfaces.IDropWhenBroken;
import com.hungteen.pvz.client.model.attached.BucketHelmetModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.ModelPartEntity;
import com.hungteen.pvz.common.network.DropDamagedArmorPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class ExtraHealthArmorItem extends ArmorItem implements IDropWhenBroken {
    public ExtraHealthArmorItem(ArmorMaterial material, Properties properties, EquipmentSlot armorType) {
        super(material, armorType, properties);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "pvz:textures/models/armor/" + ForgeRegistries.ITEMS.getKey(stack.getItem()).getPath() + "_" + (stack.getDamageValue() * 3 / stack.getMaxDamage() + ".png");
    }

    public void handleHurt(LivingHurtEvent event) {
        ItemStack stack = event.getEntity().getItemBySlot(slot);
        int blocked = (int) Math.min(stack.getMaxDamage() - stack.getDamageValue(), event.getAmount());
        stack.hurtAndBreak(blocked * 5 /* 5 durability equals to 1 health. */, event.getEntity(), (entity) -> {
            DropDamagedArmorPacket.drop((IDropWhenBroken) stack.getItem(), entity.level,
                    entity.position().add(0, slot == EquipmentSlot.HEAD ? entity.getBbHeight() : 0, 0));
            entity.broadcastBreakEvent(slot);
        });
        event.setAmount(event.getAmount() - blocked);
    }

    public void clientBroken(Vec3 pos, Level level) {
        if (level.isClientSide && PVZConfig.Client.zombiesDropParts.get()) {
            EntityModelSet models = Minecraft.getInstance().getEntityModels();
            new ModelPartEntity(level,
                    models.bakeLayer(PVZLayerHandler.LayerLocationMap.get(ForgeRegistries.ITEMS.getKey(this).getPath() + ":main")),
                    new ResourceLocation("pvz:textures/models/armor/" + ForgeRegistries.ITEMS.getKey(this).getPath() + "_2.png"))
                    .pos(pos)
                    .rotation(new Vec3(0.5, 0, 0)).join(level);
        }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(ExtraHealthArmorClients.INSTANCE);
    }

    private static class ExtraHealthArmorClients implements IClientItemExtensions {
        private static final ExtraHealthArmorClients INSTANCE = new ExtraHealthArmorClients();
        @Override
        public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
            EntityModelSet models = Minecraft.getInstance().getEntityModels();
            ModelPart root = models.bakeLayer(PVZLayerHandler.LayerLocationMap.get(ForgeRegistries.ITEMS.getKey(itemStack.getItem()).getPath() + ":main"));
            return new BucketHelmetModel<>(root);
        }
    }

}
