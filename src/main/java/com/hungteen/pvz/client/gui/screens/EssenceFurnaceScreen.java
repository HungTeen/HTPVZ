package com.hungteen.pvz.client.gui.screens;

import com.hungteen.pvz.Util;
import com.hungteen.pvz.common.menu.EssenceFurnaceMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Iterator;
import java.util.List;


@OnlyIn(Dist.CLIENT)
public class EssenceFurnaceScreen extends AbstractContainerScreen<EssenceFurnaceMenu> implements RecipeUpdateListener {
    private static final ResourceLocation TEXTURE = Util.prefix("textures/gui/container/essence_furnace.png");
    private static final ResourceLocation RECIPE_BUTTON_LOCATION = new ResourceLocation("textures/gui/recipe_button.png");
    public final RecipeBookComponent recipeBookComponent;
    private boolean widthTooNarrow;


    public EssenceFurnaceScreen(EssenceFurnaceMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_);
        recipeBookComponent = new RecipeBookComponent() {
            @Override
            public void setupGhostRecipe(Recipe<?> recipe, List<Slot> slots) {
                ItemStack itemstack = recipe.getResultItem();
                this.ghostRecipe.setRecipe(recipe);
                this.ghostRecipe.addIngredient(Ingredient.of(itemstack), (slots.get(2)).x, (slots.get(2)).y);
                NonNullList<Ingredient> nonnulllist = recipe.getIngredients();
                Iterator<Ingredient> iterator = nonnulllist.iterator();
                for (int i = 0; i < 2; ++i) {
                    if (!iterator.hasNext()) {
                        return;
                    }
                    Ingredient ingredient = iterator.next();
                    if (!ingredient.isEmpty()) {
                        Slot slot1 = slots.get(i);
                        this.ghostRecipe.addIngredient(ingredient, slot1.x, slot1.y);
                    }
                }
            }

        };
    }
    @Override
    public void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        this.widthTooNarrow = this.width < 379;
        this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
        this.addRenderableWidget(new ImageButton(this.leftPos + 20, this.height / 2 - 49, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, (p_97863_) -> {
            this.recipeBookComponent.toggleVisibility();
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            ((ImageButton)p_97863_).setPosition(this.leftPos + 20, this.height / 2 - 49);
        }));
        this.addWidget(this.recipeBookComponent);
        this.setInitialFocus(this.recipeBookComponent);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.recipeBookComponent.tick();
    }

    @Override
    protected void renderBg(PoseStack stack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.TEXTURE);
        int i = this.leftPos;
        int j = this.topPos;
        this.blit(stack, i, j, 0, 0, this.imageWidth, this.imageHeight);
        if (this.menu.isLit()) {
            this.blit(stack, i + 57, j + 36, 176, 0, 14, 14);
        }
        if (! this.menu.slots.get(1).hasItem()) {
            this.blit(stack, i + 57, j + 53, 178, 33, 16, 16);
        }

        int l = Mth.floor(this.menu.getBurnProgress() * 23);
        this.blit(stack, i + 79, j + 34, 176, 14, l, 16);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
            this.renderBg(stack, partialTicks, mouseX, mouseY);
            this.recipeBookComponent.render(stack, mouseX, mouseY, partialTicks);
        } else {
            this.recipeBookComponent.render(stack, mouseX, mouseY, partialTicks);
            super.render(stack, mouseX, mouseY, partialTicks);
            this.recipeBookComponent.renderGhostRecipe(stack, this.leftPos, this.topPos, true, partialTicks);
        }

        this.renderTooltip(stack, mouseX, mouseY);
        this.recipeBookComponent.renderTooltip(stack, this.leftPos, this.topPos, mouseX, mouseY);
    }

    @Override
    public void recipesUpdated() {
        this.recipeBookComponent.recipesUpdated();
    }

    @Override
    public RecipeBookComponent getRecipeBookComponent() {
        return this.recipeBookComponent;
    }

    protected boolean isHovering(int p_98462_, int p_98463_, int p_98464_, int p_98465_, double p_98466_, double p_98467_) {
        return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering(p_98462_, p_98463_, p_98464_, p_98465_, p_98466_, p_98467_);
    }
    public boolean mouseClicked(double p_98452_, double p_98453_, int p_98454_) {
        if (this.recipeBookComponent.mouseClicked(p_98452_, p_98453_, p_98454_)) {
            this.setFocused(this.recipeBookComponent);
            return true;
        } else {
            return this.widthTooNarrow && this.recipeBookComponent.isVisible() ? true : super.mouseClicked(p_98452_, p_98453_, p_98454_);
        }
    }

    protected boolean hasClickedOutside(double p_98456_, double p_98457_, int p_98458_, int p_98459_, int p_98460_) {
        boolean flag = p_98456_ < (double)p_98458_ || p_98457_ < (double)p_98459_ || p_98456_ >= (double)(p_98458_ + this.imageWidth) || p_98457_ >= (double)(p_98459_ + this.imageHeight);
        return this.recipeBookComponent.hasClickedOutside(p_98456_, p_98457_, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, p_98460_) && flag;
    }

    protected void slotClicked(Slot p_98469_, int p_98470_, int p_98471_, ClickType p_98472_) {
        super.slotClicked(p_98469_, p_98470_, p_98471_, p_98472_);
        this.recipeBookComponent.slotClicked(p_98469_);
    }
}
