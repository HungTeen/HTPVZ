package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.entity.bullet.PeaBullet;
import com.hungteen.pvz.common.menu.EssenceFurnaceRecipe;
import com.hungteen.pvz.common.world.zen_garden.GlowBerryDecorator;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
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

    //Pea types.
    public static EntityDataSerializer<PeaBullet.PeaType> peaTypeDataSerializer = EntityDataSerializer.simpleEnum(PeaBullet.PeaType.class);

    static {
        EntityDataSerializers.registerSerializer(peaTypeDataSerializer);
    }

    public static void modBusRegister(IEventBus bus){
        TREE_DECORATORS.register(bus);
        RECIPE_SERIALIZER.register(bus);
        RECIPE_TYPE.register(bus);
    }
}
