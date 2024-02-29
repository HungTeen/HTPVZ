package com.hungteen.pvz.generator;

import com.hungteen.pvz.api.events.RegisterSeedPacketsEvent;
import com.hungteen.pvz.common.register.PVZSeedPackets;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.register.PVZBlocks.WoodSet;
import com.hungteen.pvz.common.register.PVZItems;
import com.mojang.datafixers.util.Pair;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.hungteen.pvz.common.register.PVZBlocks.woodList;
import static com.hungteen.pvz.common.register.PVZBlocks.woodTypeList;
import static com.hungteen.pvz.util.Util.name;
import static com.hungteen.pvz.util.Util.prefix;

public class RecipeGen extends RecipeProvider {
    private final Map<Pair<WoodType, Boolean>, Item> boatsToMatch = new HashMap<>();

    public RecipeGen(DataGenerator p_125973_) {
        super(p_125973_);
    }

    @Override
    public void buildCraftingRecipes(Consumer<FinishedRecipe> c){
        //woodSet
        for (int i = 0; i < ItemTagGen.LOGS.size(); i ++){
            //craftingTable
            planksFromLog(c, wood(i, WoodSet.Plank), ItemTagGen.LOGS.get(i));
            woodFromLogs(c, wood(i, WoodSet.Wood), wood(i, WoodSet.Log));
            woodFromLogs(c, wood(i, WoodSet.StWood), wood(i, WoodSet.StLog));
            pressurePlate(c, wood(i, WoodSet.Plate), wood(i, WoodSet.Plank));
            fenceBuilder(wood(i, WoodSet.Fence), Ingredient.of(wood(i, WoodSet.Plank))).unlockedBy(getHasName(wood(i, WoodSet.Plank)), has(wood(i, WoodSet.Plank))).save(c);
            fenceGateBuilder(wood(i, WoodSet.Gate), Ingredient.of(wood(i, WoodSet.Plank))).unlockedBy(getHasName(wood(i, WoodSet.Plank)), has(wood(i, WoodSet.Plank))).save(c);
            buttonBuilder(wood(i, WoodSet.Button), Ingredient.of(wood(i, WoodSet.Plank))).unlockedBy(getHasName(wood(i, WoodSet.Plank)), has(wood(i, WoodSet.Plank))).save(c);
            doorBuilder(wood(i, WoodSet.Door), Ingredient.of(wood(i, WoodSet.Plank))).unlockedBy(getHasName(wood(i, WoodSet.Plank)), has(wood(i, WoodSet.Plank))).save(c);
            trapdoorBuilder(wood(i, WoodSet.Trapdoor), Ingredient.of(wood(i, WoodSet.Plank))).unlockedBy(getHasName(wood(i, WoodSet.Plank)), has(wood(i, WoodSet.Plank))).save(c);
        }
        //boats
        PVZItems.boatItemMap.forEach((pair, itemObj) -> {
            if (!pair.getSecond()){
                woodenBoat(c, itemObj.get(), getWoodSet(pair.getFirst()).get(WoodSet.Plank).get());
                if (boatsToMatch.containsKey(Pair.of(pair.getFirst(), true))){
                    chestBoat(c, boatsToMatch.get(Pair.of(pair.getFirst(), true)), itemObj.get());
                } else {
                    boatsToMatch.put(pair, itemObj.get());
                }
            } else {
                if (boatsToMatch.containsKey(Pair.of(pair.getFirst(), false))) {
                    chestBoat(c, itemObj.get(), boatsToMatch.get(Pair.of(pair.getFirst(), false)));
                } else {
                    boatsToMatch.put(pair, itemObj.get());
                }
            }
        });
        //seed packets
        PVZItems.seedPacketMap.forEach((data, itemObj) -> {
            if (data instanceof PVZSeedPackets.RecipeSeedPacketData<?> && ((PVZSeedPackets.RecipeSeedPacketData<?>)data).recipe != null) {
                final Item packet = ((PVZSeedPackets.RecipeSeedPacketData<?>) data).recipe.get("packet") instanceof RegistryObject<?> ?
                        ((RegistryObject<Item>) ((PVZSeedPackets.RecipeSeedPacketData<?>) data).recipe.get("packet")).get() :
                        PVZItems.seedPacketMap.get((RegisterSeedPacketsEvent.SeedPacketData<?>)((PVZSeedPackets.RecipeSeedPacketData<?>) data).recipe.get("packet")).get();
                if (((PVZSeedPackets.RecipeSeedPacketData<?>) data).recipe.get("seed") != null) {
                    Item seed;
                    try {
                        seed = ((PVZSeedPackets.RecipeSeedPacketData<?>) data).recipe.get("seed") instanceof RegistryObject<?> obj ?
                                ((RegistryObject<Item>) obj).get() :
                                (Item)((PVZSeedPackets.RecipeSeedPacketData<?>) data).recipe.get("seed");
                    } catch (ClassCastException error) {
                        seed = ((RegistryObject<Block>) ((PVZSeedPackets.RecipeSeedPacketData<?>) data).recipe.get("seed")).get().asItem();
                    }
                    Item essence = ((PVZSeedPackets.RecipeSeedPacketData<?>) data).recipe.get("essence") instanceof RegistryObject<?> obj ?
                            ((RegistryObject<Item>) obj).get() :
                            (Item)((PVZSeedPackets.RecipeSeedPacketData<?>) data).recipe.get("essence");
                    ShapedRecipeBuilder.shaped(itemObj.get())
                            .pattern("CCC")
                            .pattern("CBC")
                            .pattern("CAC")
                            .define('A', seed)
                            .define('B', packet)
                            .define('C', essence)
                            .unlockedBy("has_origin", has(packet))
                            .save(c, prefix("seed_packets/" + name(itemObj)));
                    }
                if (PVZItems.seedMap.get(data) != null) {
                    ShapedRecipeBuilder.shaped(itemObj.get())
                            .pattern("BBB")
                            .pattern("BCB")
                            .pattern("BBB")
                            .define('B', PVZItems.seedMap.get(data).get())
                            .define('C', ((PVZSeedPackets.RecipeSeedPacketData<?>) data).getBackCard().get())
                            .unlockedBy("has_origin", has(packet))
                            .save(c, prefix("seed_packets/fusion/" + name(itemObj)));
                }
            }
        });
    }

    public ItemLike wood(int i, WoodSet elem){
        return woodList.get(i).get(elem).get();
    }
    public Map<WoodSet, RegistryObject<Block>> getWoodSet(WoodType woodType){
        for (int i = 0; i < PVZBlocks.woodTypeList.size(); i ++){
            if (woodTypeList.get(i) == woodType){
                return woodList.get(i);
            }
        }
        return null;
    }
    protected void chestBoat(Consumer<FinishedRecipe> consumer, ItemLike chestBoat, ItemLike boat){
        ShapelessRecipeBuilder.shapeless(chestBoat)
                .requires(Blocks.CHEST).requires(boat)
                .group("chest_boat").unlockedBy("has_boat", has(ItemTags.BOATS))
                .save(consumer);
    }
}
