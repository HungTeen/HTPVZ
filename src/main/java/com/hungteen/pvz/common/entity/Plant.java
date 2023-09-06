package com.hungteen.pvz.common.entity;

import com.hungteen.pvz.Util;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

import static java.lang.Math.ceil;
import static net.minecraftforge.event.ForgeEventFactory.canMountEntity;

public class Plant extends Mob implements IHaveSkills, INeedSafeSituation {


    /**
     * whether this plant need proper plant-able blocks.*/
    protected static final EntityDataAccessor<Boolean> ROOT = SynchedEntityData.defineId(Plant.class, EntityDataSerializers.BOOLEAN);
    /**
     * whether this plant occupy an area so other plants can't plant on.*/
    protected static final EntityDataAccessor<Boolean> HAS_COINCIDE_DMG = SynchedEntityData.defineId(Plant.class, EntityDataSerializers.BOOLEAN);
    private int wiltCountDown = 0;
    private boolean shouldAlign = true;

    protected Plant(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.KNOCKBACK_RESISTANCE, 1)
                .add(Attributes.MOVEMENT_SPEED, 0);
    }

    @Override
    public void baseTick(){
        super.baseTick();
        // check plant wilt.
        if (isPositionSafe(this.level, this.getOnPos()) != null && isVehicleSafe(getVehicle()) != null &&
                this.getAttribute(Attributes.MAX_HEALTH) != null && ++ wiltCountDown > 10) {
            this.hurt(DamageSource.GENERIC, (float) (0.2 * this.getAttribute(Attributes.MAX_HEALTH).getValue()));
            wiltCountDown = 0;
        }
        //about aligning blocks.
        if (this.getDeltaMovement().distanceToSqr(new Vec3(0, 0, 0)) > 0.05 || ! this.isOnGround()) {
            shouldAlign = true;
        } else if (shouldAlign) {
            alignBlocks();
            setDeltaMovement(0, 0, 0);
            shouldAlign = false;
        }
    }


/**see {@link INeedSafeSituation} .
 * */
    @Override
    public MutableComponent isPositionSafe(Level level, BlockPos onPos){
        VoxelShape tmpShape = level.getBlockState(onPos).getCollisionShape(level, onPos);
        //TODO 看看有没有bug。
        double calcHeight = getBbHeight() + (tmpShape.isEmpty() ? 0 : tmpShape.bounds().maxY) - 1;
        for (int i = 1; i <= ceil(calcHeight); i ++) {
            if (! level.getBlockState(onPos.offset(new Vec3i(0, i, 0))).isAir()) {
                if (calcHeight - i >= 1) {
                    return Component.translatable("hint.pvz.plant.no_enough_place");
                } else {
                    tmpShape = level.getBlockState(onPos.offset(new Vec3i(0, i, 0))).getCollisionShape(level, onPos.offset(new Vec3i(0, i, 0)));
                    if ((tmpShape.isEmpty() ? 1 : tmpShape.bounds().minY) < calcHeight - i + 1) {
                        return Component.translatable("hint.pvz.plant.no_enough_place");
                    }
                }
            }
            if (! level.getBlockState(onPos.offset(new Vec3i(0, i, 0))).getFluidState().isEmpty()) {
                return Component.translatable("hint.pvz.plant.cant_plant_in_water", getName());
            }
        }
        if (shouldHaveCoincideDmg(level, onPos)) {
            return Component.translatable("hint.pvz.plant.no_enough_place");
        }
        if (!getEntityData().get(ROOT) || level.getBlockState(onPos).is(PVZBlockTags.PLANTABLE_BLOCKS)) {
            return null;
        } else {
            return Component.translatable("hint.pvz.plant.cant_plant_on", getName(), level.getBlockState(onPos).getBlock().getName());
        }
    }
    @Override
    public MutableComponent isVehicleSafe(Entity vehicle) {
        if (vehicle == null) {
            return Component.translatable("hint.pvz.plant.entity_not_present");
        }
        if (vehicle instanceof ICanBePlantedOn && ((ICanBePlantedOn) vehicle).canHold(this)) {
            if (!canMountEntity(this, vehicle, this.getVehicle() == vehicle)) {
                return Component.translatable("hint.pvz.plant.no_enough_place", getName());
            }
            return null;
        } else {
            return Component.translatable("hint.pvz.plant.cant_plant_on", getName(), vehicle.getName());
        }
    }


    /**
     * control if this plant can push another entity.*/
    public Predicate<Entity> canPush(){
        return (entity) -> this.isPushable();
    }
    /**
     * control if this plant can be pushed by another entity.*/
    @Override
    public boolean isPushable(){
        return false;
    }
    /** control if this plant has coincide dmg.
     */
    public boolean shouldHaveCoincideDmg(Level level, BlockPos onPos){
        if (!this.getEntityData().get(HAS_COINCIDE_DMG)) {
            return false;
        } else {
            BlockPos subPos = this.getOnPos();
            List<Entity> list = level.getEntities(this, this.getBoundingBox().move(onPos.offset(-subPos.getX(), -subPos.getY(), -subPos.getZ())),
//            List<Entity> list = this.level.getEntities(this, this.getBoundingBox(),
                    (entity) -> entity instanceof Plant && entity.getEntityData().get(HAS_COINCIDE_DMG));
            return !list.isEmpty();
        }
    }
    public void alignBlocks() {
        BlockPos pos = this.getOnPos();
        moveTo(pos.getX() + 0.5, this.getY(), pos.getZ() + 0.5);
    }


    //data
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ROOT, true);
        this.entityData.define(HAS_COINCIDE_DMG, true);
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        saveSkills(tag);
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        loadSkills(tag);
    }


    //others
    @Override
    protected void pushEntities(){
        List<Entity> list = this.level.getEntities(this, this.getBoundingBox(), EntitySelector.pushableBy(this).and(this.canPush()));
        if (!list.isEmpty()) {
            int i = this.level.getGameRules().getInt(GameRules.RULE_MAX_ENTITY_CRAMMING);
            if (i > 0 && list.size() > i - 1 && this.random.nextInt(4) == 0) {
                int j = 0;
                for (Entity entity : list) {
                    if (!entity.isPassenger()) {
                        ++j;
                    }
                }
                if (j > i - 1) {
                    this.hurt(DamageSource.CRAMMING, 6.0F);
                }
            }
            for (Entity entity : list) {
                this.doPush(entity);
            }
        }
    }

    @Nullable
    public ItemStack getPickResult() {
        RegistryObject<Item> summonCardItem = PVZItems.plantCardMap.get(Util.name(this.getType()));
        return summonCardItem == null ? null : new ItemStack(summonCardItem.get());
    }
}
