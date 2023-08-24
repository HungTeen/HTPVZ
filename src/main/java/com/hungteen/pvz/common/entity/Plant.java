package com.hungteen.pvz.common.entity;

import com.hungteen.pvz.common.tags.PVZBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Predicate;

public class Plant extends Mob implements IHaveSkills{


    /**
     * whether this plant need proper plant-able blocks.*/
    protected static final EntityDataAccessor<Boolean> ROOT = SynchedEntityData.defineId(Plant.class, EntityDataSerializers.BOOLEAN);
    /**
     * whether this plant occupy an area so other plants can't plant on.*/
    protected static final EntityDataAccessor<Boolean> HAS_COINCIDE_DMG = SynchedEntityData.defineId(Plant.class, EntityDataSerializers.BOOLEAN);
    private int wiltCountDown = 0;

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
        if (! isPositionSafe(this, this.level, this.getOnPos()) && this.getAttribute(Attributes.MAX_HEALTH) != null && ++ wiltCountDown > 10){
            this.hurt(DamageSource.GENERIC, (float) (0.2 * this.getAttribute(Attributes.MAX_HEALTH).getValue()));
            wiltCountDown = 0;
        }
    }


    /**
     * used in {@link Plant#baseTick()}, checking if this place fits the plant.
     * */
    public static boolean isPositionSafe(Plant plant, Level level, BlockPos pos){
        return plant.isPassenger() || (
                level.getBlockState(pos.above()).getFluidState().isEmpty()
                && !plant.hasCoincideDmg()
                && (!plant.getEntityData().get(ROOT) || level.getBlockState(pos).is(PVZBlockTags.PLANTABLE_BLOCKS))
        );
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
    public boolean hasCoincideDmg(){
        if (!this.getEntityData().get(HAS_COINCIDE_DMG)) {
            return false;
        } else {
            List<Entity> list = this.level.getEntities(this, this.getBoundingBox(),
                    (entity) -> entity instanceof Plant && entity.getEntityData().get(HAS_COINCIDE_DMG));
            return !list.isEmpty();
        }
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
}
