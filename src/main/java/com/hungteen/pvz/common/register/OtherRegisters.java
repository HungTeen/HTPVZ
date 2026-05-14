package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.entity.bullet.MelonBullet;
import com.hungteen.pvz.common.entity.bullet.PeaBullet;
import com.hungteen.pvz.common.event.RegisterSproutsEvent;
import com.hungteen.pvz.common.menu.EssenceFurnaceRecipe;
import com.hungteen.pvz.common.world.invasion.LootWithinZombieEventCondition;
import com.hungteen.pvz.common.world.zen_garden.GlowBerryDecorator;
import com.hungteen.pvz.common.world.zen_garden.MegaNutTrunkPlacer;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.core.Registry;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.client.event.RegisterRecipeBookCategoriesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class OtherRegisters {
    //tree decorators.
    public static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATORS = DeferredRegister.create(ForgeRegistries.TREE_DECORATOR_TYPES, PVZMod.MODID);
    public static final RegistryObject<TreeDecoratorType<GlowBerryDecorator>> GLOW_BERRY_DECORATOR = TREE_DECORATORS.register("glow_berry", () -> new TreeDecoratorType<>(GlowBerryDecorator.CODEC));

    //trunk placers
    public static final DeferredRegister<TrunkPlacerType<?>> TRUNK_PLACER = DeferredRegister.create(Registry.TRUNK_PLACER_TYPE_REGISTRY, PVZMod.MODID);
    public static final RegistryObject<TrunkPlacerType<?>> NUT_TREE_TRUNK_PLACER = TRUNK_PLACER.register("mega_nut", () -> new TrunkPlacerType<>(MegaNutTrunkPlacer.CODEC));

    //EssenceFurnace RecipeBook.
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPE = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, PVZMod.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, PVZMod.MODID);
    public static RecipeBookType essenceFurnaceRecipeBookType = RecipeBookType.create("ESSENCE_FURNACE");
    public static final RegistryObject<RecipeType<EssenceFurnaceRecipe>> essenceFurnaceRecipeType =
            RECIPE_TYPE.register("essence_furnace", () -> RecipeType.simple(Util.prefix("essence_furnace")));
    public static final RegistryObject<RecipeSerializer<EssenceFurnaceRecipe>> essenceFurnaceRecipeSerializer =
            RECIPE_SERIALIZER.register("essence_furnace", EssenceFurnaceRecipe.Serializer::new);

    @SubscribeEvent
    public static void essenceFurnaceRecipeBookRegister(RegisterRecipeBookCategoriesEvent ev) {
        RecipeBookCategories aggregateCategory = RecipeBookCategories.create("ESSENCE_FURNACE_SEARCH",
                new ItemStack(Items.COMPASS));//TODO change to garden compass.
        RecipeBookCategories essenceFurnaceEssencesCategory = RecipeBookCategories.create("ESSENCE_FURNACE_ESSENCES",
                new ItemStack(PVZItems.VENTUS_ESSENCE.get()));
        RecipeBookCategories essenceFurnaceOthersCategory = RecipeBookCategories.create("ESSENCE_FURNACE_OTHERS",
                new ItemStack(PVZItems.ORIGIN_ESSENCE.get()));
        ev.registerBookCategories(essenceFurnaceRecipeBookType, List.of(aggregateCategory, essenceFurnaceEssencesCategory, essenceFurnaceOthersCategory));
        ev.registerRecipeCategoryFinder(essenceFurnaceRecipeType.get(), (recipe) -> ((EssenceFurnaceRecipe)recipe).isEssence ? essenceFurnaceEssencesCategory : essenceFurnaceOthersCategory);
        ev.registerAggregateCategory(aggregateCategory, List.of(essenceFurnaceEssencesCategory, essenceFurnaceOthersCategory));
    }

    //Bullet types.
    public static EntityDataSerializer<PeaBullet.PeaType> peaTypeDataSerializer = EntityDataSerializer.simpleEnum(PeaBullet.PeaType.class);
    public static EntityDataSerializer<MelonBullet.MelonType> melonTypeDataSerializer = EntityDataSerializer.simpleEnum(MelonBullet.MelonType.class);
    public static EntityDataSerializer<MelonBullet.MelonSkill> melonSkillDataSerializer = EntityDataSerializer.simpleEnum(MelonBullet.MelonSkill.class);

    static {
        EntityDataSerializers.registerSerializer(peaTypeDataSerializer);
        EntityDataSerializers.registerSerializer(melonTypeDataSerializer);
        EntityDataSerializers.registerSerializer(melonSkillDataSerializer);
    }

    //Mob Category.
    public static MobCategory PVZPlantMobCategory = MobCategory.create("pvz_plant", "pvz:pvz_plant", 64, true, false, 64);

    //Loot Table Condition.
    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITIONS = DeferredRegister.create(Registry.LOOT_ITEM_REGISTRY, PVZMod.MODID);
    public static final RegistryObject<LootItemConditionType> WITHIN_INVASION = LOOT_CONDITIONS.register("within_zombie_event", () -> new LootItemConditionType(new LootWithinZombieEventCondition.Serializer()));

    //Loot Table Function
    public static final DeferredRegister<LootItemFunctionType> LOOT_FUNCTIONS = DeferredRegister.create(Registry.LOOT_FUNCTION_REGISTRY, PVZMod.MODID);
    public static final RegistryObject<LootItemFunctionType> SET_SPROUT = LOOT_FUNCTIONS.register("set_sprout", () -> new LootItemFunctionType(new RegisterSproutsEvent.SetSproutTypeFunction.Serializer()));
    public static void modBusRegister(IEventBus bus) {
        TREE_DECORATORS.register(bus);
        TRUNK_PLACER.register(bus);
        RECIPE_SERIALIZER.register(bus);
        RECIPE_TYPE.register(bus);
        LOOT_CONDITIONS.register(bus);
        LOOT_FUNCTIONS.register(bus);
    }
}
