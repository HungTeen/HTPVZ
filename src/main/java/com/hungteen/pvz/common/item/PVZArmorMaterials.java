package com.hungteen.pvz.common.item;

import com.hungteen.pvz.common.register.PVZSoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public enum PVZArmorMaterials implements ArmorMaterial {

    CONE("pvz:cone", 250, new int[] {0, 0, 0, 0}, 10, PVZSoundEvents.EQUIP_CONE.get(), PVZSoundEvents.DAMAGE_CONE.get(), 0F, 0F, Ingredient::of, true),
    BUCKET("pvz:bucket", 750, new int[] {0, 0, 0, 0}, 10, SoundEvents.ARMOR_EQUIP_IRON, PVZSoundEvents.DAMAGE_METAL.get(), 0F, 0F, Ingredient::of, true),
    DUCK_LIFEBUOY("pvz:duck_lifebuoy", 100, new int[] {0, 0, 0, 0}, 10, PVZSoundEvents.EQUIP_LIFEBUOY.get(), null, 0F, 0F, Ingredient::of, true),
    PUMPKIN("pvz:pumpkin", 500, new int[] {0, 0, 0, 0}, 10, PVZSoundEvents.EQUIP_PUMPKIN.get(), PVZSoundEvents.DAMAGE_PUMPKIN.get(), 0F, 0F, Ingredient::of, true);

    private static final int[] HEALTH_PER_SLOT = new int[] {13, 15, 16, 11};
    private final String name;
    private final int durabilityMultiplier;
    private final int[] slotProtections;
    private final int enchantmentValue;
    private final SoundEvent equipSound;
    private @Nullable final SoundEvent breakSound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredient;
    private final boolean sameDurability;

    PVZArmorMaterials(String name, int durability, int[] protections, int enchantPoint, SoundEvent soundEvent, float toughness, float kbValue, Supplier<Ingredient> ingredientSupplier, SoundEvent breakSound) {
        this(name, durability, protections, enchantPoint, soundEvent, breakSound, toughness, kbValue, ingredientSupplier, false);
    }

    PVZArmorMaterials(String name, int durability, int[] protections, int enchantPoint, SoundEvent soundEvent, @Nullable SoundEvent breakSound, float toughness, float kbValue, Supplier<Ingredient> ingredientSupplier, boolean sameDurability) {
        this.name = name;
        this.durabilityMultiplier = durability;
        this.slotProtections = protections;
        this.enchantmentValue = enchantPoint;
        this.equipSound = soundEvent;
        this.breakSound = breakSound;
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
        return this.equipSound;
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

    @Nullable
    public SoundEvent getBreakSound() {
        return breakSound;
    }
}
