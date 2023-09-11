package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.enchantment.QuickCoolDownEnchantment;
import com.hungteen.pvz.common.enchantment.SunShovelEnchantment;
import com.hungteen.pvz.common.enchantment.SoillessCultureEnchantment;
import com.hungteen.pvz.common.enchantment.SunMendingEnchantment;
import com.hungteen.pvz.common.item.SeedPacketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.stream.Stream;

public class PVZEnchantments {

    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, PVZMod.MODID);

    public static final RegistryObject<Enchantment> SUN_SHOVEL = ENCHANTMENTS.register("sun_shovel", SunShovelEnchantment::new);
    public static final RegistryObject<Enchantment> SUN_MENDING = ENCHANTMENTS.register("sun_mending", SunMendingEnchantment::new);
    public static final RegistryObject<Enchantment> SOILLESS_CULTURE = ENCHANTMENTS.register("soilless_culture", SoillessCultureEnchantment::new);
    public static final RegistryObject<Enchantment> QUICK_COOL_DOWN = ENCHANTMENTS.register("quick_cool_down", QuickCoolDownEnchantment::new);


    //enchantmentTypes.
    /** add EnchantmentCategory in vanilla CreativeModeTabs at {@link PVZEnchantments#handleEnchantmentTypes()}.
     * */
    public static final EnchantmentCategory SUMMON_CARD = EnchantmentCategory.create("summon_card", (item) -> item instanceof SeedPacketItem<?>);

    public static final EnchantmentCategory SHOVEL = EnchantmentCategory.create("shovel", (item) -> item instanceof ShovelItem);

    public static void handleEnchantmentTypes() {
        List<EnchantmentCategory> list = new java.util.ArrayList<>(Stream.of(CreativeModeTab.TAB_TOOLS.getEnchantmentCategories()).toList());
        list.add(PVZEnchantments.SHOVEL);
        CreativeModeTab.TAB_TOOLS.setEnchantmentCategories(list.toArray(new EnchantmentCategory[]{}));

        PVZItemTabs.PVZ_PLANT_CARDS.setEnchantmentCategories(PVZEnchantments.SUMMON_CARD);
    }
}
