package com.hungteen.pvz.compat;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.menu.EssenceFurnaceMenu;
import com.hungteen.pvz.common.menu.EssenceFurnaceRecipe;
import com.hungteen.pvz.common.register.OtherRegisters;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.register.PVZMenus;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;
import java.util.List;

@JeiPlugin
public class JEICompatHandler implements IModPlugin {
    public static ResourceLocation RECIPIES = Util.prefix("recipes");
    public static RecipeType<EssenceFurnaceRecipe> ESSENCE_FURNACE = RecipeType.create(PVZMod.MODID, "essence_furnace", EssenceFurnaceRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return RECIPIES;
    }

    @Override
    public void registerRecipes(IRecipeRegistration reg) {
        List<EssenceFurnaceRecipe> list = Minecraft.getInstance().getConnection().getRecipeManager()
                .byType(OtherRegisters.essenceFurnaceRecipeType.get()).values().stream().toList();
        reg.addRecipes(ESSENCE_FURNACE, list);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new EssenceFurnaceRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        reg.addRecipeCatalyst(PVZBlocks.ESSENCE_FURNACE.get().asItem().getDefaultInstance(), ESSENCE_FURNACE);
    }
    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration reg) {
        reg.addRecipeTransferHandler(EssenceFurnaceMenu.class, PVZMenus.ESSENCE_FURNACE.get(), ESSENCE_FURNACE, 0, 2, 3, 36);
    }

    public static class EssenceFurnaceRecipeCategory implements IRecipeCategory<EssenceFurnaceRecipe> {
        private final IDrawable slot;
        private final IDrawable background;
        private final IDrawable icon;
        protected final IDrawable heatIndicator;
        protected final IDrawableAnimated arrow;

        public EssenceFurnaceRecipeCategory (IGuiHelper helper) {
            ResourceLocation backgroundImage = new ResourceLocation(PVZMod.MODID, "textures/gui/container/essence_furnace.png");
            this.slot = helper.getSlotDrawable();
            this.background = helper.createDrawable(backgroundImage, 55, 16, 82, 54);
            this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(PVZBlocks.ESSENCE_FURNACE.get()));
            heatIndicator = helper.createDrawable(backgroundImage, 176, 0, 14, 14);
            arrow = helper.drawableBuilder(backgroundImage, 176, 14, 24, 17)
                    .buildAnimated(200, IDrawableAnimated.StartDirection.LEFT, false);
        }

        @Override
        public RecipeType<EssenceFurnaceRecipe> getRecipeType() {
            return ESSENCE_FURNACE;
        }

        @Override
        public Component getTitle() {
            return Component.translatable("block.pvz.essence_furnace");
        }

        @Override
        public IDrawable getBackground() {
            return this.background;
        }

        @Override
        public IDrawable getIcon() {
            return this.icon;
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, EssenceFurnaceRecipe recipe, IFocusGroup focusGroup) {
            NonNullList<Ingredient> recipeIngredients = recipe.getIngredients();
            ItemStack resultStack = recipe.getResultItem();

            builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                    .addItemStacks(Arrays.asList(recipeIngredients.get(0).getItems()));
            builder.addSlot(RecipeIngredientRole.INPUT, 1, 37)
                    .addItemStacks(Arrays.asList(recipeIngredients.get(1).getItems()));

            builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 19).addItemStack(resultStack);
        }

        @Override
        public void draw(EssenceFurnaceRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack poseStack, double mouseX, double mouseY) {
            background.draw(poseStack, 0, 0);
            arrow.draw(poseStack, 24, 18);
            heatIndicator.draw(poseStack, 2, 20);
            this.drawCookTime(recipe, poseStack, 45);
        }

        protected void drawCookTime(EssenceFurnaceRecipe recipe, PoseStack poseStack, int y) {
            int cookTime = recipe.needTime;
            if (cookTime > 0) {
                int cookTimeSeconds = cookTime / 20;
                Component timeString = Component.translatable("gui.jei.category.smelting.time.seconds", cookTimeSeconds);
                Minecraft minecraft = Minecraft.getInstance();
                Font fontRenderer = minecraft.font;
                int stringWidth = fontRenderer.width(timeString);
                fontRenderer.draw(poseStack, timeString, (float)(this.getWidth() - stringWidth), (float)y, -8355712);
            }
        }
    }
}
