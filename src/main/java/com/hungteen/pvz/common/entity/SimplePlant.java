package com.hungteen.pvz.common.entity;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.interfaces.*;
import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapNBT;
import com.hungteen.pvz.common.capability.pvzRules.PVZRulesCapability;
import com.hungteen.pvz.common.enchantment.SunShovelEnchantment;
import com.hungteen.pvz.common.entity.ai.goal.ServerStressReleaseGoals;
import com.hungteen.pvz.common.item.SeedPacketItem;
import com.hungteen.pvz.common.register.PVZEnchantments;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import com.hungteen.pvz.common.world.PVZDamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static net.minecraftforge.event.ForgeEventFactory.canMountEntity;
/**
 * Not including all plants.<br>
 * To identify if a mob is plant or not, use {@link com.hungteen.pvz.api.interfaces.IPlant} which supports more.
 */

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class SimplePlant extends Mob implements IHaveSkills, IPlant, ICanAttack {


    /**
     * whether this plant need proper plant-able blocks.
     */
    public static final EntityDataAccessor<Boolean> ROOT = SynchedEntityData.defineId(SimplePlant.class, EntityDataSerializers.BOOLEAN);
    /**
     * whether this plant occupy an area so other plants can't plant on.
     */
    public static final EntityDataAccessor<Boolean> TAKES_COINCIDE_DMG = SynchedEntityData.defineId(SimplePlant.class, EntityDataSerializers.BOOLEAN);
    /**
     * how long can this plant still live. When player is too far, this countdown goes faster.
     */
    public static final EntityDataAccessor<Integer> WILT_COUNTDOWN = SynchedEntityData.defineId(SimplePlant.class, EntityDataSerializers.INT);
    /**skill id. see {@link Skill}.*/
    public static final EntityDataAccessor<Integer> SKILL = SynchedEntityData.defineId(SimplePlant.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_TIME = SynchedEntityData.defineId(SimplePlant.class, EntityDataSerializers.INT);

    private int situationHurtCount = 0;
    protected boolean shouldAlign = true;

    protected SimplePlant(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.KNOCKBACK_RESISTANCE, 1)
                .add(Attributes.MOVEMENT_SPEED, 0);
    }
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new ServerStressReleaseGoals.ServerStressReleaseGoal(this));
        this.targetSelector.addGoal(0, new ServerStressReleaseGoals.ServerStressReleaseTargetGoal(this, false));
    }

    //IPlant
    @Override
    public EntityDataAccessor<Boolean> root() {
        return ROOT;
    }
    @Override
    public boolean takesCoincideDmg() {
        return this.getEntityData().get(TAKES_COINCIDE_DMG);
    }
    /** control if this plant has coincide dmg.
     */
    public boolean shouldHaveCoincideDmg(Level level, BlockPos onPos) {
        if (! takesCoincideDmg()) {
            return false;
        } else {
            BlockPos subPos = this.getOnPos();
            List<Entity> list = level.getEntities(this, this.getBoundingBox().move(onPos.offset(-subPos.getX(), -subPos.getY(), -subPos.getZ())),
                    (entity) -> entity instanceof IPlant && ((IPlant)entity).takesCoincideDmg() && this.getVehicle() != entity && entity.getVehicle() != this);
            return !list.isEmpty();
        }
    }
    public Set<TagKey<Block>> getAcceptableTags() {
        return Set.of(PVZBlockTags.PLANTABLE_BLOCKS);
    }
    /**
     * see {@link INeedSafeSituation}  for the two methods below.
     */
    @Override
    public MutableComponent isPositionSafe(Level level, BlockPos onPos, boolean isPlanting) {
        AABB aabb = AABB.ofSize(new Vec3(onPos.getX() + 0.5, onPos.getY() + 1 + getBbHeight() / 2, onPos.getZ() + 0.5), getBbWidth() - 0.0001, getBbHeight() - 0.0001, getBbWidth() - 0.0001);
        if (BlockPos.betweenClosedStream(aabb).anyMatch((pos) -> {
            BlockState blockstate = this.level.getBlockState(pos);
            return !blockstate.isAir() && blockstate.isSuffocating(this.level, pos) &&
                    Shapes.joinIsNotEmpty(blockstate.getCollisionShape(this.level, pos).move(pos.getX(), pos.getY(), pos.getZ()), Shapes.create(aabb), BooleanOp.AND);
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
            if (level.getBlockState(onPos).getFluidState().isEmpty()) {
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
            return Component.translatable("hint.pvz.plant.cant_plant_in_water", this.getName());
        } else {
            return Component.translatable("hint.pvz.plant.cant_plant_on", this.getName(), level.getBlockState(onPos).getBlock().getName());
        }
    }
    @Override
    public MutableComponent isVehicleSafe(Entity target, boolean isPlanting) {
        if (target == null) {
            if (! isPlanting) {
                this.boardingCooldown = 0;
                BlockPos onPos = this.getOnPos();
                List<Entity> list = level.getEntities(this, this.getBoundingBox().move(onPos.offset(-onPos.getX(), -onPos.getY() - 0.0001, -onPos.getZ())).inflate(0, 1, 0),
                        (entity) -> entity instanceof IPlant && ((IPlant)entity).takesCoincideDmg() && this.getVehicle() != entity && entity.getVehicle() != this);
                if (this.getVehicle() == null) {
                    list.forEach((entity) -> {
                        if (this.getVehicle() == null && entity instanceof ICanBePlantedOn vehicle && vehicle.canHold(this, false)) {
                            this.startRiding(entity);
                        }
                    });
                    if (this.getVehicle() != null) {
                        return null;
                    }
                }
            }
            return Component.translatable("hint.pvz.plant.entity_not_present");
        }
        if (target instanceof ICanBePlantedOn && ((ICanBePlantedOn) target).canHold(this, isPlanting)) {
            if (PVZOwnedCapability.isTeammate(this, target)) {
                if (! canMountEntity(this, target, this.getVehicle() == target)) {
                    return isPlanting && target.getFirstPassenger() != null ?
                            isVehicleSafe(target.getFirstPassenger(), true) :
                            Component.translatable("hint.pvz.plant.no_enough_place", this.getName());
                }
                if (isPlanting) {
                    this.moveTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), 0.0F);
                    ((ServerLevel)this.level).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.level.getBlockState(this.getOnPos())).setPos(this.getOnPos()), this.getX(), this.getY(), this.getZ(), 5, 0.0D, 0.0D, 0.0D, 0.15F);
                    this.startRiding(target);
                }
                return null;
            }
            return Component.translatable("hint.pvz.plant.need_own_team");
        } else {
            return Component.translatable("hint.pvz.plant.cant_plant_on", this.getName(), target.getName());
        }
    }

    //skill
    @Override
    public int getSkillVal(Object obj) {
        return entityData.get(SKILL);
    }
    @Override
    public void setSkillVal(Object obj, int val) {
        entityData.set(SKILL, val);
    }
    @Override
    public List<Skill> getStaticSkillList(){
        return List.of();
    }

    //overrides
    @Override
    public void baseTick() {
        super.baseTick();
        //check plant situation damage.
        if (! level.isClientSide) {
            if (isPositionSafe(this.level, this.getOnPos(), false) != null && isVehicleSafe(getVehicle(), false) != null &&
                    this.getAttribute(Attributes.MAX_HEALTH) != null) {
                if (++ situationHurtCount > 10) {
                    this.hurt(PVZDamageSource.PLANT_WILT, (float) (0.2 * this.getAttribute(Attributes.MAX_HEALTH).getValue()));
                    situationHurtCount = 0;
                }
            } else {
                situationHurtCount = 0;
            }
        }
        //about aligning blocks.
        if (! this.isOnGround() || this.getDeltaMovement().distanceToSqr(new Vec3(0, 0, 0)) > 0.05) {
            shouldAlign = true;
        } else if (shouldAlign) {
            alignBlocks();
            setDeltaMovement(0, 0, 0);
            shouldAlign = false;
        }
        //TODO relative codes. add particle when plant is dying.
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

    public int getMaxAirSupply() {
        return 50;
    }

    public boolean canBeLeashed(Player p_21418_) {
        return false;
    }
    public void alignBlocks() {
        BlockPos pos = this.getOnPos();
        moveTo(pos.getX() + 0.5, this.getY(), pos.getZ() + 0.5);
    }

    @SubscribeEvent
    public static void handleShovel(PlayerInteractEvent.EntityInteract ev) {
        Player player = ev.getEntity();
        InteractionHand handIn = ev.getHand();
        Entity entity = ev.getTarget();
        ItemStack itemstack = player.getItemInHand(handIn);
        if (itemstack.getItem() instanceof ShovelItem && entity instanceof IPlant plant) {
            if (plant.onBeingShoveled(player, handIn)) {
                ev.setCancellationResult(InteractionResult.SUCCESS);
                ev.setCanceled(true);
            }
        }
    }

    public boolean onBeingShoveled(Player player, InteractionHand handIn) {
        //check permission.
        final boolean[] permission = {false};
        this.getCapability(PVZOwnedCapability.CAP).ifPresent((cap) -> {
            Entity owner = cap.getOwner();
            if (owner != null) {
            permission[0] = PVZRulesCapability.getBoolean("shovelPermission") ?
                    (PVZOwnedCapability.isTeammate(owner, player) || ! PVZRulesCapability.getBoolean("teamBattle")) : owner.is(player);
            } else {
                permission[0] = PVZRulesCapability.getBoolean("shovelPermission");
            }
        });
        //shovel plant.
        if (!player.level.isClientSide() && permission[0]) {
            ItemStack itemstack = player.getItemInHand(handIn);
            itemstack.hurtAndBreak(2, player, (entity) -> {
                entity.broadcastBreakEvent(handIn);
            });
            int enchantmentLevel = EnchantmentHelper.getTagEnchantmentLevel(PVZEnchantments.SUN_SHOVEL.get(), itemstack);
            PVZOwnedCapability cap = this.getCapability(PVZOwnedCapability.CAP).orElse(null);
            if (cap != null && enchantmentLevel > 0 && Objects.equals(cap.resource, PVZPlayerCapNBT.SUN)) {
                Sun.spawnSunsRandomlyByAmount(level, getOnPos(), (int) (cap.cost * SunShovelEnchantment.returnSunPercent(enchantmentLevel)), 0, 0.25F);
            }
            ((ServerLevel)this.level).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.level.getBlockState(this.getOnPos())).setPos(this.getOnPos()), this.getX(), this.getY(), this.getZ(), 5, 0.0D, 0.0D, 0.0D, 0.15F);
            this.remove(RemovalReason.DISCARDED);
            if (this.isVehicle()) {
                this.getPassengers().forEach((entity -> {
                    if (entity instanceof INeedSafeSituation entity1) {
                        entity1.isVehicleSafe(this.getVehicle(), true);
                    }
                }));
            }
            return true;
        }
        return false;
    }

    //data
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ROOT, true);
        this.entityData.define(TAKES_COINCIDE_DMG, true);
        this.entityData.define(WILT_COUNTDOWN, -1);
        this.entityData.define(SKILL, 0);
        this.entityData.define(ATTACK_TIME, 0);
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Root", getEntityData().get(ROOT));
        tag.putBoolean("HasCoincideDmg", getEntityData().get(TAKES_COINCIDE_DMG));
        tag.putInt("WiltCountDown", getEntityData().get(WILT_COUNTDOWN));
        tag.putInt("Skill", getSkillVal(this));
        tag.putInt("PlantAttackTime", getAttackTime(this));

    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        if (tag.contains("Skill")) {
            setSkillVal(this, tag.getInt("Skill"));
        }
        if (tag.contains("PlantAttackTime")) {
            this.setAttackTime(this,tag.getInt("PlantAttackTime"));
        }
        if (tag.contains("WiltCountDown")) {
            this.getEntityData().set(WILT_COUNTDOWN, tag.getInt("WiltCountDown"));
        }
        if (tag.contains("Root")) {
            this.getEntityData().set(ROOT, tag.getBoolean("Root"));
        }
        if (tag.contains("HasCoincideDmg")) {
            this.getEntityData().set(TAKES_COINCIDE_DMG, tag.getBoolean("HasCoincideDmg"));
        }
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
    public int getAttackTime(Object obj) {
        return entityData.get(ATTACK_TIME);
    }

    public void setAttackTime(Object obj,int cd) {
        entityData.set(ATTACK_TIME, cd);
    }
    @Nullable
    public ItemStack getPickResult() {
        AtomicReference<Item> packetItem = new AtomicReference<>();
        SeedPacketItem.seedPacketItemList.forEach(item -> {
            if (item.getEntity().equals(this.getType())) {
                packetItem.set(item);
        }});
        return packetItem.get() == null ? super.getPickResult() : new ItemStack(packetItem.get());
    }
    @Override
    public boolean removeWhenFarAway(double p_27598_) {
        PVZOwnedCapability cap = this.getCapability(PVZOwnedCapability.CAP).orElse(null);
        return cap.getOwner() == null;
        //TODO handle situation when player is not available when loading.
    }
    public static boolean checkSpawnRules(EntityType<? extends LivingEntity> entityType, ServerLevelAccessor level, MobSpawnType mobSpawnType, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(PVZBlockTags.PLANTABLE_BLOCKS);
    }

}
