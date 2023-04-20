package com.hungteen.pvz.generator;

import com.hungteen.pvz.common.register.PVZBlocks.WoodSet;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.Map;
import java.util.function.Consumer;

import static com.hungteen.pvz.common.register.PVZBlocks.woodList;

public class RecipeGen extends RecipeProvider {

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

    }

    public ItemLike wood(int i, WoodSet elem){
        return woodList.get(i).get(elem).get();
    };
}
