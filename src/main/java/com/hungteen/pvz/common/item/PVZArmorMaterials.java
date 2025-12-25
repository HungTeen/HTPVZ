package com.hungteen.pvz.common.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Supplier;

public enum PVZArmorMaterials implements ArmorMaterial {

    CONE("pvz:cone", 250, new int[] {0, 0, 0, 0}, 10, SoundEvents.ARMOR_EQUIP_LEATHER, 0F, 0F, () -> Ingredient.of(Items.LEATHER), true),
    BUCKET("pvz:bucket", 750, new int[] {0, 0, 0, 0}, 10, SoundEvents.ARMOR_EQUIP_IRON, 0F, 0F, () -> Ingredient.of(Items.IRON_INGOT), true),
    DUCK_LIFEBUOY("pvz:duck_lifebuoy", 100, new int[] {0, 0, 0, 0}, 10, SoundEvents.ARMOR_EQUIP_LEATHER, 0F, 0F, () -> Ingredient.of(Items.LEATHER), true),
    PUMPKIN("pvz:pumpkin", 500, new int[] {0, 0, 0, 0}, 10, SoundEvents.ARMOR_EQUIP_LEATHER, 0F, 0F, () -> Ingredient.of(Items.PUMPKIN), true);

    private static final int[] HEALTH_PER_SLOT = new int[] {13, 15, 16, 11};
    private final String name;
    private final int durabilityMultiplier;
    private final int[] slotProtections;
    private final int enchantmentValue;
    private final SoundEvent sound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredient;
    private final boolean sameDurability;

    PVZArmorMaterials(String name, int durability, int[] protections, int enchantPoint, SoundEvent soundEvent, float toughness, float kbValue, Supplier<Ingredient> ingredientSupplier) {
        this(name, durability, protections, enchantPoint, soundEvent, toughness, kbValue, ingredientSupplier, false);
    }

    PVZArmorMaterials(String name, int durability, int[] protections, int enchantPoint, SoundEvent soundEvent, float toughness, float kbValue, Supplier<Ingredient> ingredientSupplier, boolean sameDurability) {
        this.name = name;
        this.durabilityMultiplier = durability;
        this.slotProtections = protections;
        this.enchantmentValue = enchantPoint;
        this.sound = soundEvent;
        this.toughness = toughness;
        this.knockbackResistance = kbValue;
        this.repairIngredient = ingredientSupplier;
        this.sameDurability = sameDurability;
    }

    public int getDurabilityForSlot(EquipmentSlot slot) {
        return this.sameDurability ? durabilityMultiplier : HEALTH_PER_SLOT[slot.getIndex()] * this.durabilityMultiplier;
    }

    public int getDefenseForSlot(EquipmentSlot slot) {
        return this.slotProtections[slot.getIndex()];
    }

    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    public SoundEvent getEquipSound() {
        return this.sound;
    }

    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    public String getName() {
        return this.name;
    }

    public float getToughness() {
        return this.toughness;
    }

    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }
}
