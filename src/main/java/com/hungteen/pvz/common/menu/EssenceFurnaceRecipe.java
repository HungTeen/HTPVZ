package com.hungteen.pvz.common.menu;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hungteen.pvz.common.register.OtherRegisters;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

/**registered at {@link OtherRegisters#essenceFurnaceRecipeType} .*/
public class EssenceFurnaceRecipe implements Recipe<Container> {
    private final Ingredient ingredient;
    private final Ingredient fuel;
    private final ItemStack result;
    private final ResourceLocation id;
    private final String group;
    public final boolean costFuel;
    public final boolean costIngredient;
    public final boolean isEssence;
    public final short needTime;

    public EssenceFurnaceRecipe(ResourceLocation location, Ingredient ingredient, Ingredient fuel, ItemStack result, short needTime, String group, boolean costIngredient, boolean costFuel, boolean isEssence) {
        this.id = location;
        this.ingredient = ingredient;
        this.fuel = fuel;
        this.result = result;
        this.group = group;
        this.costIngredient = costIngredient;
        this.costFuel = costFuel;
        this.isEssence = isEssence;
        this.needTime = needTime;
    }
    @Override
    public boolean matches(Container container, Level level) {
        return this.ingredient.test(container.getItem(0)) && this.fuel.test(container.getItem(1));
    }

    @Override
    public ItemStack assemble(Container p_44001_) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
        return true;
    }
    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> nonnulllist = NonNullList.create();
        nonnulllist.add(this.ingredient);
        nonnulllist.add(this.fuel);
        return nonnulllist;
    }
    @Override
    public ItemStack getResultItem() {
        return result;
    }
    @Override
    public String getGroup() {
        return group;
    }
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return OtherRegisters.essenceFurnaceRecipeSerializer.get();
    }

    @Override
    public RecipeType<?> getType() {
        return OtherRegisters.essenceFurnaceRecipeType.get();
    }

    public static class Serializer implements RecipeSerializer<EssenceFurnaceRecipe> {

        @Override
        public EssenceFurnaceRecipe fromJson(ResourceLocation location, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            boolean costIngredient = GsonHelper.getAsBoolean(json, "cost_ingredient", false);
            boolean costFuel = GsonHelper.getAsBoolean(json, "cost_fuel", true);
            boolean isEssence = GsonHelper.getAsBoolean(json, "is_essence", false);
            short needTime = GsonHelper.getAsShort(json, "need_time", (short) 200);

            JsonElement jsonelement = GsonHelper.isArrayNode(json, "ingredient") ? GsonHelper.getAsJsonArray(json, "ingredient") : GsonHelper.getAsJsonObject(json, "ingredient");
            Ingredient ingredient = Ingredient.fromJson(jsonelement);

            JsonElement jsonelement1 = GsonHelper.isArrayNode(json, "fuel") ? GsonHelper.getAsJsonArray(json, "fuel") : GsonHelper.getAsJsonObject(json, "fuel");
            Ingredient fuel = Ingredient.fromJson(jsonelement1);
            //TODO multi items?

            if (!json.has("result")) {
                throw new JsonSyntaxException("Missing result, expected to find a string or object");
            }
            ItemStack result;
            if (json.get("result").isJsonObject()) {
                result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            }
            else {
                String s1 = GsonHelper.getAsString(json, "result");
                ResourceLocation resourcelocation = new ResourceLocation(s1);
                if (ForgeRegistries.ITEMS.containsKey(resourcelocation)) {
                    result = ForgeRegistries.ITEMS.getValue(resourcelocation).getDefaultInstance();
                } else {
                    throw new IllegalStateException("Item: " + s1 + " does not exist");
                }
            }

            return new EssenceFurnaceRecipe(location, ingredient, fuel, result, needTime, group, costIngredient, costFuel, isEssence);
        }

        @Override
        public @Nullable EssenceFurnaceRecipe fromNetwork(ResourceLocation location, FriendlyByteBuf buf) {
            Ingredient ingredient = Ingredient.fromNetwork(buf);
            Ingredient fuel = Ingredient.fromNetwork(buf);
            ItemStack result = buf.readItem();
            String group = buf.readUtf();
            boolean costIngredient = buf.readBoolean();
            boolean costFuel = buf.readBoolean();
            boolean isEssence = buf.readBoolean();
            short needTime = buf.readShort();
            return new EssenceFurnaceRecipe(location, ingredient, fuel, result, needTime, group, costIngredient, costFuel, isEssence);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, EssenceFurnaceRecipe recipe) {
            recipe.ingredient.toNetwork(buf);
            recipe.fuel.toNetwork(buf);
            buf.writeItem(recipe.result);
            buf.writeUtf(recipe.group);
            buf.writeBoolean(recipe.costIngredient);
            buf.writeBoolean(recipe.costFuel);
            buf.writeBoolean(recipe.isEssence);
            buf.writeShort(recipe.needTime);
        }
    }
}
