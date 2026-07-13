package com.hungteen.pvz.common.item;

import com.hungteen.pvz.common.register.PVZBannerPatterns;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ModifiedSpawnEggItem extends ForgeSpawnEggItem {

    public static final Supplier<CompoundTag> CONEHEAD_ZOMBIE = () -> new CompoundTag(
            Map.of("EntityTag", new CompoundTag(
                    Map.of("ArmorItems", new ListTag(List.of(new CompoundTag(), new CompoundTag(), new CompoundTag(), PVZItems.CONE_HELMET.get().getDefaultInstance().save(new CompoundTag())), Tag.TAG_COMPOUND)))));
    public static final Supplier<CompoundTag> BUCKETHEAD_ZOMBIE = () -> new CompoundTag(
            Map.of("EntityTag", new CompoundTag(
                    Map.of("ArmorItems", new ListTag(List.of(new CompoundTag(), new CompoundTag(), new CompoundTag(), PVZItems.BUCKET_HELMET.get().getDefaultInstance().save(new CompoundTag())), Tag.TAG_COMPOUND)))));
    public static final Supplier<CompoundTag> DUCK_LIFEBUOY_ZOMBIE = () -> new CompoundTag(
            Map.of("EntityTag", new CompoundTag(
                    Map.of("ArmorItems", new ListTag(List.of(new CompoundTag(), PVZItems.DUCK_LIFEBUOY.get().getDefaultInstance().save(new CompoundTag()), new CompoundTag(), new CompoundTag()), Tag.TAG_COMPOUND)))));
    public static final Supplier<CompoundTag> SCREEN_DOOR_ZOMBIE = () -> new CompoundTag(
            Map.of("EntityTag", new CompoundTag(
                    Map.of("HandItems", new ListTag(List.of(PVZItems.SCREEN_DOOR_SHIELD.get().getDefaultInstance().save(new CompoundTag()), new CompoundTag()), Tag.TAG_COMPOUND)))));
    public static final Supplier<CompoundTag> OVERWORLD_FLAG_ZOMBIE = () -> new CompoundTag(
            Map.of("EntityTag", new CompoundTag(
                    Map.of("ArmorItems", new ListTag(List.of(new CompoundTag(), new CompoundTag(), new CompoundTag(), getOverworldBanner().save(new CompoundTag())), Tag.TAG_COMPOUND),
                            "style_path", new StringTag("minecraft_overworld")))));
    public static final Supplier<CompoundTag> NETHER_FLAG_ZOMBIE = () -> new CompoundTag(
            Map.of("EntityTag", new CompoundTag(
                    Map.of("ArmorItems", new ListTag(List.of(new CompoundTag(), new CompoundTag(), new CompoundTag(), getNetherBanner().save(new CompoundTag())), Tag.TAG_COMPOUND),
                            "style_path", new StringTag("minecraft_the_nether")))));
    public static final Supplier<CompoundTag> END_FLAG_ZOMBIE = () -> new CompoundTag(
            Map.of("EntityTag", new CompoundTag(
                    Map.of("ArmorItems", new ListTag(List.of(new CompoundTag(), new CompoundTag(), new CompoundTag(), getEndBanner().save(new CompoundTag())), Tag.TAG_COMPOUND),
                            "style_path", new StringTag("minecraft_the_end")))));

    public Supplier<CompoundTag> entityModifier;
    public ModifiedSpawnEggItem(Supplier<? extends EntityType<? extends Mob>> type, Supplier<CompoundTag> entityModifier, int backgroundColor, int highlightColor, Properties props) {
        super(type, backgroundColor, highlightColor, props);
        this.entityModifier = entityModifier;
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack itemStack, CompoundTag tag) {
        itemStack.setTag(entityModifier.get());
        return super.initCapabilities(itemStack, tag);
    }


    //utils
    public static ItemStack getOverworldBanner() {
        final ItemStack itemstack = new ItemStack(Items.RED_BANNER);
        final CompoundTag tag = new CompoundTag();
        ListTag listTag = (new BannerPattern.Builder())
                .addPattern(BannerPatterns.BORDER, DyeColor.BLUE)
                .addPattern(BannerPatterns.TRIANGLES_TOP, DyeColor.WHITE)
                .addPattern(BannerPatterns.TRIANGLES_BOTTOM, DyeColor.WHITE)
                .addPattern(PVZBannerPatterns.BRAIN.getKey(), DyeColor.WHITE)
                .toListTag();
        tag.put("Patterns", listTag);
        BlockItem.setBlockEntityData(itemstack, BlockEntityType.BANNER, tag);
        itemstack.hideTooltipPart(ItemStack.TooltipPart.ADDITIONAL);
        itemstack.setHoverName((Component.translatable("block.pvz.brain_banner")));
        return itemstack;
    }
    public static ItemStack getNetherBanner() {
        final ItemStack itemstack = new ItemStack(Items.RED_BANNER);
        final CompoundTag tag = new CompoundTag();
        ListTag listTag = (new BannerPattern.Builder())
                .addPattern(BannerPatterns.GRADIENT, DyeColor.ORANGE)
                .addPattern(BannerPatterns.BRICKS, DyeColor.BROWN)
                .addPattern(BannerPatterns.CIRCLE_MIDDLE, DyeColor.ORANGE)
                .addPattern(BannerPatterns.BORDER, DyeColor.ORANGE)
                .addPattern(PVZBannerPatterns.BRAIN.getKey(), DyeColor.WHITE)
                .toListTag();
        tag.put("Patterns", listTag);
        BlockItem.setBlockEntityData(itemstack, BlockEntityType.BANNER, tag);
        itemstack.hideTooltipPart(ItemStack.TooltipPart.ADDITIONAL);
        itemstack.setHoverName((Component.translatable("block.pvz.brain_banner")));
        return itemstack;
    }
    public static ItemStack getEndBanner() {
        final ItemStack itemstack = new ItemStack(Items.PURPLE_BANNER);
        final CompoundTag tag = new CompoundTag();
        ListTag listTag = (new BannerPattern.Builder())
                .addPattern(BannerPatterns.TRIANGLE_TOP, DyeColor.BLACK)
                .addPattern(BannerPatterns.TRIANGLE_BOTTOM, DyeColor.BLACK)
                .addPattern(PVZBannerPatterns.BRAIN.getKey(), DyeColor.WHITE)
                .toListTag();
        tag.put("Patterns", listTag);
        BlockItem.setBlockEntityData(itemstack, BlockEntityType.BANNER, tag);
        itemstack.hideTooltipPart(ItemStack.TooltipPart.ADDITIONAL);
        itemstack.setHoverName((Component.translatable("block.pvz.brain_banner")));
        return itemstack;
    }
}
