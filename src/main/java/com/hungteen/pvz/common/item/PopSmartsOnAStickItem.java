package com.hungteen.pvz.common.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.hungteen.pvz.common.register.PVZAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.FoodOnAStickItem;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class PopSmartsOnAStickItem<T extends Entity & ItemSteerable> extends FoodOnAStickItem<T> {
    private static final UUID modifierUuid = UUID.fromString("70580191-35bc-68f7-c1f0-b133ca9ff778");

    public PopSmartsOnAStickItem(Properties p_41307_, EntityType<T> p_41308_, int p_41309_) {
        super(p_41307_, p_41308_, p_41309_);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            super.getAttributeModifiers(slot, stack).forEach(builder::put);
            builder.put(PVZAttributes.ENEMY_ATTRACTION.get(), new AttributeModifier(modifierUuid, "pop_smarts_on_a_stick", 5, AttributeModifier.Operation.ADDITION));
            return builder.build();
        }
        return super.getAttributeModifiers(slot, stack);
    }
}
