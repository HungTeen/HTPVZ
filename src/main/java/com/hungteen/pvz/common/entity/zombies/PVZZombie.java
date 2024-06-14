package com.hungteen.pvz.common.entity.zombies;

import com.hungteen.pvz.api.interfaces.ICanGroupUp;
import com.hungteen.pvz.common.entity.ai.goal.BlockWithShieldGoal;
import com.hungteen.pvz.common.entity.ai.goal.FollowGroupLeaderGoal;
import com.hungteen.pvz.common.entity.ai.goal.GroupShareEnemyGoal;
import com.hungteen.pvz.common.register.PVZBannerPatterns;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.UUID;
import java.util.function.Consumer;

public class PVZZombie extends Zombie implements ICanGroupUp {
    public static final EntityDataAccessor<String> SKIN = SynchedEntityData.defineId(PVZZombie.class, EntityDataSerializers.STRING);
    @OnlyIn(Dist.CLIENT)
    public boolean renderHand = true; // controlled by renderer.
    @OnlyIn(Dist.CLIENT)
    public boolean renderHead = true; // controlled by renderer.
    public static Consumer<Entity> CONEHEAD_ZOMBIE_CONSUMER = (entity) -> entity.setItemSlot(EquipmentSlot.HEAD, PVZItems.CONE_HELMET.get().getDefaultInstance());
    public static Consumer<Entity> BUCKET_ZOMBIE_CONSUMER = (entity) -> entity.setItemSlot(EquipmentSlot.HEAD, PVZItems.BUCKET_HELMET.get().getDefaultInstance());
    public static Consumer<Entity> DUCK_LIFEBUOY_ZOMBIE_CONSUMER = (entity) -> entity.setItemSlot(EquipmentSlot.LEGS, PVZItems.DUCK_LIFEBUOY.get().getDefaultInstance());
    public static Consumer<Entity> SCREEN_DOOR_CONSUMER = (entity) -> entity.setItemSlot(EquipmentSlot.MAINHAND, PVZItems.SCREEN_DOOR_SHIELD.get().getDefaultInstance());
    public static Consumer<Entity> OVERWORLD_FLAG_ZOMBIE_CONSUMER = (entity) -> {
        entity.setItemSlot(EquipmentSlot.HEAD, getOverworldBanner());
        entity.getEntityData().set(SKIN, "minecraft_overworld");
    };
    public static Consumer<Entity> NETHER_FLAG_ZOMBIE_CONSUMER = (entity) -> {
        entity.setItemSlot(EquipmentSlot.HEAD, getNetherBanner());
        entity.getEntityData().set(SKIN, "minecraft_the_nether");
    };
    public static Consumer<Entity> END_FLAG_ZOMBIE_CONSUMER = (entity) -> {
        entity.setItemSlot(EquipmentSlot.HEAD, getEndBanner());
        entity.getEntityData().set(SKIN, "minecraft_the_end");
    };
    public static UUID GROUP_UP_MODIFIER = UUID.fromString("772807aa-672f-bfda-7d21-0f66823f6d53");
    public PVZZombie(EntityType<? extends Zombie> p_34271_, Level p_34272_) {
        super(p_34271_, p_34272_);
    }

    //methods
    public boolean shouldDropHand() {
        return this.getHealth() < this.getAttributeBaseValue(Attributes.MAX_HEALTH) / 2;
    }

    public boolean shouldDropHead() {
        return this.getHealth() <= 0;
    }

    //configs
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        ResourceLocation res = this.level.dimension().location();
        entityData.define(SKIN, res.getNamespace() + "_" + res.getPath());
    }

    @Override
    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(1, new BlockWithShieldGoal(this));
        this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(3, new FollowGroupLeaderGoal(this));
        this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0D, true, 4, this::canBreakDoors));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0D));
        this.targetSelector.addGoal(1, new GroupShareEnemyGoal(this));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers(ZombifiedPiglin.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }
    @Override
    protected boolean convertsInWater() {
        return false;
    }
    @Override
    protected ItemStack getSkull() {
        return ItemStack.EMPTY;
    }
    @Override
    public int getExperienceReward() {
        return 0;
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("style_path", this.entityData.get(SKIN));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("style_path")) {
            this.entityData.set(SKIN, tag.getString("style_path"));
        }
    }
    public String getStyle() {
        return this.entityData.get(SKIN);
    }

    @Override
    public void tick(){
        super.tick();
        if (! EntityUtil.isEntityValid(this.getVehicle())) {
            this.stopRiding();
        }
        AttributeInstance instance = this.getAttribute(Attributes.FOLLOW_RANGE);
        if (this.schoolSize > 1) {
            if (instance.getModifier(GROUP_UP_MODIFIER) == null) {
                instance.addTransientModifier(new AttributeModifier(GROUP_UP_MODIFIER, "group_up_modifier", -16, AttributeModifier.Operation.ADDITION));
            }
        } else {
            instance.removeModifier(GROUP_UP_MODIFIER);
        }
    }

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

    //ICanGroupUp
    ICanGroupUp leader = null;
    int schoolSize = 1;

    @Override
    public ICanGroupUp getLeader() {
        return leader;
    }

    @Override
    public void setLeader(ICanGroupUp entity) {
        leader = entity;
    }

    @Override
    public int getMaxSchoolSize() {
        return this.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof BannerItem ? 20 : 5;
    }

    @Override
    public int getSchoolSize() {
        return schoolSize;
    }

    @Override
    public void setSchoolSize(int size) {
        schoolSize = size;
    }

    @Override
    public int getGroupRangeSqr() {
        return 8;
    }


}
