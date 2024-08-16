package com.hungteen.pvz.common.entity.creatures;

import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.gameevent.GameEvent;

import static net.minecraft.world.level.biome.Biomes.*;

public class MooBloom extends Cow implements Shearable, net.minecraftforge.common.IForgeShearable {

    public MooBloom(EntityType<? extends MooBloom> p_28914_, Level p_28915_) {
        super(p_28914_, p_28915_);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D);
    }

    public static boolean checkMooBloomSpawnRules(EntityType<? extends Animal> entityType, LevelAccessor level, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource rand) {
        return level.getBiome(blockPos).is(SUNFLOWER_PLAINS)
                || (level.getBiome(blockPos).is(FLOWER_FOREST) || level.getBiome(blockPos).is(MEADOW) && rand.nextInt(3) == 0)
                || rand.nextInt(10) == 0
                && checkAnimalSpawnRules(entityType, level, mobSpawnType, blockPos, rand);
    }

    public float getWalkTargetValue(BlockPos p_28933_, LevelReader p_28934_) {
        return p_28934_.getBlockState(p_28933_.below()).is(BlockTags.FLOWERS) ? 10.0F : p_28934_.getPathfindingCostFromLightLevels(p_28933_);
    }

    public InteractionResult mobInteract(Player p_28941_, InteractionHand p_28942_) {
        ItemStack itemstack = p_28941_.getItemInHand(p_28942_);
        if (itemstack.is(Items.BOWL) && !this.isBaby()) {
            ItemStack itemstack1;
            itemstack1 = Items.SUSPICIOUS_STEW.getDefaultInstance();
            SuspiciousStewItem.saveMobEffect(itemstack1, PVZMobEffects.BRIGHTNESS.get(), 600);
            ItemStack itemstack2 = ItemUtils.createFilledResult(itemstack, p_28941_, itemstack1, false);
            p_28941_.setItemInHand(p_28942_, itemstack2);
            this.playSound(SoundEvents.MOOSHROOM_MILK, 1.0F, 1.0F);
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        } else {
            return super.mobInteract(p_28941_, p_28942_);
        }
    }

    public MooBloom getBreedOffspring(ServerLevel p_148942_, AgeableMob p_148943_) {
        return PVZEntities.MOOBLOOM.get().create(p_148942_);
    }


    //About shearing
    @Override
    public java.util.List<ItemStack> onSheared(@org.jetbrains.annotations.Nullable Player player, @org.jetbrains.annotations.NotNull ItemStack item, Level world, BlockPos pos, int fortune) {
        this.gameEvent(GameEvent.SHEAR, player);
        return shearInternal(player == null ? SoundSource.BLOCKS : SoundSource.PLAYERS);
    }

    @Override
    public boolean isShearable(@org.jetbrains.annotations.NotNull ItemStack item, Level world, BlockPos pos) {
        return readyForShearing();
    }

    public boolean readyForShearing() {
        return this.isAlive() && !this.isBaby();
    }

    @Override
    public void shear(SoundSource p_28924_) {
        shearInternal(p_28924_).forEach(s -> this.level.addFreshEntity(new ItemEntity(this.level, this.getX(), this.getY(1.0D), this.getZ(), s)));
    }

    private java.util.List<ItemStack> shearInternal(SoundSource p_28924_) {
        this.level.playSound(null, this, SoundEvents.MOOSHROOM_SHEAR, p_28924_, 1.0F, 1.0F);
        if (!this.level.isClientSide()) {
            ((ServerLevel) this.level).sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(0.5D), this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            this.discard();
            Cow cow = EntityType.COW.create(this.level);
            cow.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
            cow.setHealth(this.getHealth());
            cow.yBodyRot = this.yBodyRot;
            if (this.hasCustomName()) {
                cow.setCustomName(this.getCustomName());
                cow.setCustomNameVisible(this.isCustomNameVisible());
            }

            if (this.isPersistenceRequired()) {
                cow.setPersistenceRequired();
            }

            cow.setInvulnerable(this.isInvulnerable());
            this.level.addFreshEntity(cow);

            java.util.List<ItemStack> items = new java.util.ArrayList<>();
            for (int i = 0; i < 3; ++i) {
                items.add(Items.SUNFLOWER.getDefaultInstance());
            }
            return items;
        }
        return java.util.Collections.emptyList();

    }


    //
}
