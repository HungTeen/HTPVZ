package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.interfaces.IArmorEntity;
import com.hungteen.pvz.api.interfaces.ICanBePlantedOn;
import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.entity.ai.goal.AxisLookAroundGoal;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import com.hungteen.pvz.common.tags.PVZEntityTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidType;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class LilyPad extends SimplePlant implements ICanBePlantedOn {

    double xCurrentSpeed = 0;//can not understand how vanilla methods work...
    double zCurrentSpeed = 0;
    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.lily_pad.lily_boat", PVZItems.AQUA_ESSENCE, 6, 4, 0, 0),
            new Skill("skill.pvz.lily_pad.friendship_of_lily_pad", PVZItems.LUX_ESSENCE, 6, 12, -25, 0).avoidSkills(0)
    );

    public LilyPad(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean canHold(LivingEntity plant, boolean isPlanting) {
        return ! (plant.getType().is(PVZEntityTags.MUST_PLANT_IN_DIRT)) && (!isPlanting || getPassengers().isEmpty()) && PVZOwnedCapability.isTeammate(this, plant);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.set(ROOT, false);
    }

    //entity settings
    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 8D)
                .add(Attributes.MOVEMENT_SPEED, 0.5D)
                .add(ForgeMod.SWIM_SPEED.get(), 50D)
                .add(Attributes.FOLLOW_RANGE, 2D);
    }
    @Override
    public List<Skill> getStaticSkillList(){
        return staticSkillList;
    }

    //overrides
    @Override
    public boolean canBeCollidedWith() {
        return true;
    }
    @Override
    protected float getWaterSlowDown() {
        return 0F;
    }
    @Override
    public boolean canBeRiddenUnderFluidType(FluidType type, Entity rider)
    {
        return true;
    }
    @Override
    public Entity getControllingPassenger() {
        return this.getFirstPassenger();
    }
    @Override
    public double getPassengersRidingOffset() {
        return (this.getFirstPassenger() != null && this.getFirstPassenger() instanceof Player) ? 0 : 0.2;
    }
    @Override
    public void tick() {
        if (! this.noPhysics) {
            if (! level.getFluidState(new BlockPos(position().add(0, this.getBbHeight(), 0))).isEmpty()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, 0.04, 0).multiply(0.5, 0.5, 0.5));
            } else if (! level.getFluidState(new BlockPos(position().add(0, this.getEyeHeight(), 0))).isEmpty()) {
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 0.5, 0.5));
                if (Math.abs(this.getDeltaMovement().y) < 0.001 && this.getDeltaMovement().y != 0) {
                    this.setDeltaMovement(this.getDeltaMovement().multiply(1, 0, 1));
                }
            }
        }
        this.shouldAlign = false;
        super.tick();
    }

    @Override
    public void travel(Vec3 vec3) {
        if (this.isAlive()) {
            if (this.isVehicle() && this.getFirstPassenger() instanceof Player player) {
                this.setRot(this.getYRot() + ((player.getYRot() % 360F - this.getYRot() + 180F) % 360F - 180F) * 0.2F, 0);
                this.yBodyRot = this.getYRot();
                this.yHeadRot = this.yBodyRot;
                boolean inWater = ! level.getFluidState(new BlockPos(position().add(0, this.getEyeHeight(), 0))).isEmpty();

                xCurrentSpeed *= inWater ? 0.95 : 0.5;
                zCurrentSpeed *= inWater ? 0.95 : 0.5;
                double lr = Math.max(xCurrentSpeed, inWater ? player.xxa : 0);
                double fb = Math.max(zCurrentSpeed, inWater ? player.zza : 0);
                if (fb <= 0.0F) {
                    fb *= 0.25F;
                }
                xCurrentSpeed = lr;
                zCurrentSpeed = fb;

                this.flyingSpeed = this.getSpeed() * 0.1F;
                this.setSpeed((float) this.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
                super.travel(new Vec3(lr, vec3.y, fb));
            } else {
                this.flyingSpeed = 0.02F;
                super.travel(vec3);
            }
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new AttractEnemyGoal(this));
        this.goalSelector.addGoal(3, new AxisLookAroundGoal(this));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hasSkill(this, "skill.pvz.lily_pad.lily_boat")) {
            if (PVZOwnedCapability.isTeammate(this, player) && getPassengers().isEmpty() && player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                player.moveTo(getX(), getY(), getZ(), getYRot(), 0.0F);
                player.startRiding(this);
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public Predicate<Entity> canPush(){
        return entity -> true;
    }
    public Set<TagKey<Block>> getAcceptableTags() {
        return Set.of(PVZBlockTags.PLANTABLE_WATER);
    }
    public boolean canBreatheUnderwater() {
        return true;
    }
    public int getMaxAirSupply() {
        return 500;
    }
    protected void handleAirSupply(int p_30344_) {
        if (this.isAlive() && !this.isInWaterOrBubble()) {
            this.setAirSupply(p_30344_ - 1);
            if (this.getAirSupply() == -20) {
                this.setAirSupply(0);
                this.hurt(DamageSource.DROWN, 2.0F);
            }
        } else {
            this.setAirSupply(300);
        }
    }
    public void baseTick() {
        super.baseTick();
        this.handleAirSupply(this.getAirSupply());
    }

    public boolean isPushedByFluid() {
        return true;
    }
    @Override
    public MutableComponent isPositionSafe(Level level, BlockPos onPos, boolean isPlanting) {
        AABB aabb = AABB.ofSize(new Vec3(onPos.getX() + 0.5, onPos.getY() + 1 + getBbHeight() / 2, onPos.getZ() + 0.5), getBbWidth(), getBbHeight() - 0.0001, getBbWidth());
        if (BlockPos.betweenClosedStream(aabb).anyMatch((p_201942_) -> {
            BlockState blockstate = this.level.getBlockState(p_201942_);
            return !blockstate.isAir() && blockstate.isSuffocating(this.level, p_201942_) &&
                    Shapes.joinIsNotEmpty(blockstate.getCollisionShape(this.level, p_201942_).move(p_201942_.getX(), p_201942_.getY(), p_201942_.getZ()), Shapes.create(aabb), BooleanOp.AND);
        })) {
            return Component.translatable("hint.pvz.plant.no_enough_place");
        }

        if (shouldHaveCoincideDmg(level, onPos)) {
            return Component.translatable("hint.pvz.plant.no_enough_place");
        }
        boolean plantableOn = false;
        for (TagKey<Block> tag: getAcceptableTags()) {
            if (level.getBlockState(onPos).is(tag)) {
                plantableOn = true;
                break;
            }
        }
        if (! this.getEntityData().get(root()) || (plantableOn && ! level.getBlockState(onPos).isAir())) {
            if (level.getBlockState(onPos).getFluidState().is(FluidTags.WATER)) {
                if (isPlanting) {
                    this.moveTo(
                            onPos.getX() + 0.5,
                            onPos.getY() + (level.getBlockState(onPos).getCollisionShape(level, onPos).isEmpty() ?
                                    (level.getFluidState(onPos).isEmpty() ? 0: level.getFluidState(onPos).getHeight(level, onPos)) :
                                    level.getBlockState(onPos).getCollisionShape(level, onPos).bounds().maxY),
                            onPos.getZ() + 0.5);
                    ((ServerLevel)this.level).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.level.getBlockState(this.getOnPos())).setPos(this.getOnPos()), this.getX(), this.getY(), this.getZ(), 5, 0.0D, 0.0D, 0.0D, 0.15F);
                }
                return null;
            }
        }
        if (isPlanting) {
            return Component.translatable("hint.pvz.plant.can_only_plant_in_water", this.getName());
        }
        return null;
    }
    @Override
    public MutableComponent isVehicleSafe(Entity target, boolean isPlanting) {
        if (target == null) {
            return Component.translatable("hint.pvz.plant.entity_not_present");
        }
        return Component.translatable("hint.pvz.plant.cant_plant_on", this.getName(), target.getName());
    }
}
