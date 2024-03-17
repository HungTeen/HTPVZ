package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.interfaces.ICanBePlantedOn;
import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.entity.ai.goal.AxisLookAroundGoal;
import com.hungteen.pvz.common.event.PVZResourceEvent;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import com.hungteen.pvz.common.tags.PVZEntityTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidBlock;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class LilyPad extends SimplePlant implements ICanBePlantedOn {

    double xCurrentSpeed = 0;//can not understand how vanilla methods work...
    double zCurrentSpeed = 0;
    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.lily_pad.lily_boat", PVZItems.AQUA_ESSENCE, 6, 4, 0, 0),
            new Skill("skill.pvz.lily_pad.friendship_of_lily_pad", PVZItems.LUX_ESSENCE, 6, 12, -25, 0).avoidSkills(0)
            //new Skill("skill.pvz.lily_pad.lava_swimmer", PVZItems.IGNIS_ESSENCE, 9, 4, 75, 0).avoidSkills(0)
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
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
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
            //TODO find a way to support lava situation.
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
                boolean inWater = ! level.getFluidState(new BlockPos(position().add(0, this.getEyeHeight(), 0))).isEmpty();
                this.setRot(this.getYRot() + ((player.getYRot() % 360F - this.getYRot() + 180F) % 360F - 180F) * (inWater ? 0.2F : 0.02F), 0);
                this.yBodyRot = this.getYRot();
                this.yHeadRot = this.yBodyRot;

                xCurrentSpeed *= inWater ? 0.95 : 0.5;
                zCurrentSpeed *= inWater ? 0.95 : 0.5;
                double lr = xCurrentSpeed * 0.75 + (inWater ? 1 : 0.25) * player.xxa * 0.25;
                double fb = zCurrentSpeed * 0.75 + (inWater ? 0.5 : 0.15) * player.zza * 0.25;
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

    public double getFluidJumpThreshold() {
        return 0D;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new AttractEnemyGoal(this, () -> this.getFirstPassenger() == null, 2));
        this.goalSelector.addGoal(3, new AxisLookAroundGoal(this));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hasSkill(this, "skill.pvz.lily_pad.lily_boat")) {
            if (PVZOwnedCapability.isTeammate(this, player) && getPassengers().isEmpty()
                    && player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()
                    && ! (player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof ShovelItem)) {
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
    public boolean canBreatheUnderwater() {
        return true;
    }
    public int getMaxAirSupply() {
        return 750;
    }
    public boolean fireImmune() {
        return super.fireImmune() || this.hasSkill(this, "skill.pvz.lily_pad.lava_swimmer");
    }
    protected void handleAirSupply(int p_30344_) {
        if (this.isAlive() && !this.isInWaterOrBubble()) {
            this.setAirSupply(p_30344_ - 1);
            if (this.getAirSupply() == -20) {
                this.setAirSupply(0);
                this.hurt(DamageSource.DROWN, 2.0F);
            }
        } else {
            this.setAirSupply(this.getMaxAirSupply());
        }
    }
    public void baseTick() {
        int i = this.getAirSupply();
        super.baseTick();
        this.handleAirSupply(i);
    }

    public boolean isPushedByFluid() {
        return true;
    }
    @Override
    public Direction getGrowDirection() {
        return null;
    }

    @Override
    public MutableComponent isPositionSafe(PVZResourceEvent.CheckPlantConditionEvent event, Level level, BlockPos pos, Direction direction, boolean isPlanting) {
        if (isPlanting && event != null) {
            if (event.cost > PVZPlayerCapability.getValue(event.getEntity(), event.resource)) {
                return Component.translatable("hint.pvz.plant.no_enough_resource", Component.translatable(event.resource));
            }
        }
        Vec3i offset = direction == null ? Vec3i.ZERO : direction.getNormal();
        pos = pos.offset(offset).offset(getGrowDirection() == null ? Vec3i.ZERO : getGrowDirection().getOpposite().getNormal());
        direction = getGrowDirection();
        offset = direction == null ? Vec3i.ZERO : direction.getNormal();
        AABB aabb = AABB.ofSize(new Vec3(pos.getX() + 0.5 + offset.getX(),
                        pos.getY() + offset.getY() + getBbHeight() / 2,
                        pos.getZ() + 0.5 + offset.getZ()),
                getBbWidth() - 0.0001, getBbHeight() - 0.0001, getBbWidth() - 0.0001);
        if (BlockPos.betweenClosedStream(aabb).anyMatch((p_201942_) -> {
            BlockState blockstate = this.level.getBlockState(p_201942_);
            return !blockstate.isAir() && blockstate.isSuffocating(this.level, p_201942_) &&
                    Shapes.joinIsNotEmpty(blockstate.getCollisionShape(this.level, p_201942_).move(p_201942_.getX(), p_201942_.getY(), p_201942_.getZ()), Shapes.create(aabb), BooleanOp.AND);
        })) {
            return Component.translatable("hint.pvz.plant.no_enough_place");
        }
        if (shouldHaveCoincideDmg(level, Vec3.atBottomCenterOf(pos.offset(offset)))) {
            return Component.translatable("hint.pvz.plant.no_enough_place");
        }
        if (! this.getEntityData().get(root()) || (! level.getBlockState(pos).isAir())) {
            if (level.getBlockState(pos).getFluidState().is(FluidTags.WATER)) {
                if (isPlanting) {
                    this.moveTo(
                            pos.getX() + 0.5 + offset.getX(),
                            pos.getY() + (level.getBlockState(pos).getCollisionShape(level, pos).isEmpty() ?
                                    level.getFluidState(pos).isEmpty() ? 0: level.getFluidState(pos).getHeight(level, pos) :
                                    level.getBlockState(pos).getCollisionShape(level, pos).bounds().maxY),
                            pos.getZ() + 0.5 + offset.getZ());
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
    public MutableComponent isVehicleSafe(PVZResourceEvent.CheckPlantConditionEvent event, Entity target, boolean isPlanting) {
        if (target == null) {
            return Component.translatable("hint.pvz.plant.entity_not_present");
        }
        return Component.translatable("hint.pvz.plant.cant_plant_on", this.getName(), target.getName());
    }
}
