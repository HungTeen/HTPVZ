package com.hungteen.pvz.common.entity;

import com.hungteen.pvz.api.*;
import com.hungteen.pvz.api.interfaces.ICanBePlantedOn;
import com.hungteen.pvz.api.interfaces.IHaveSkills;
import com.hungteen.pvz.api.interfaces.INeedSafeSituation;
import com.hungteen.pvz.api.interfaces.IPlant;
import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapNBT;
import com.hungteen.pvz.common.capability.pvzRules.PVZRulesCapability;
import com.hungteen.pvz.common.enchantment.SunShovelEnchantment;
import com.hungteen.pvz.common.item.SeedPacketItem;
import com.hungteen.pvz.common.network.SpawnParticlePacket;
import com.hungteen.pvz.common.register.PVZEnchantments;
import com.hungteen.pvz.common.register.PVZParticles;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import com.hungteen.pvz.common.world.PVZDamageHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static java.lang.Math.ceil;
import static net.minecraftforge.event.ForgeEventFactory.canMountEntity;

public class PVZPlant extends Mob implements IHaveSkills, IPlant {


    /**
     * whether this plant need proper plant-able blocks.
     */
    public static final EntityDataAccessor<Boolean> ROOT = SynchedEntityData.defineId(PVZPlant.class, EntityDataSerializers.BOOLEAN);
    /**
     * whether this plant occupy an area so other plants can't plant on.
     */
    public static final EntityDataAccessor<Boolean> HAS_COINCIDE_DMG = SynchedEntityData.defineId(PVZPlant.class, EntityDataSerializers.BOOLEAN);
    /**
     * how long can this plant still live. When player is too far, this countdown goes faster.
     */
    public static final EntityDataAccessor<Integer> WILT_COUNTDOWN = SynchedEntityData.defineId(PVZPlant.class, EntityDataSerializers.INT);
    /**skill id. see {@link Skill}.*/
    public static final EntityDataAccessor<Integer> SKILL = SynchedEntityData.defineId(PVZPlant.class, EntityDataSerializers.INT);
    protected static List<Skill> staticSkillSet = new ArrayList<>();


    private int situationHurtCount = 0;
    private boolean shouldAlign = true;

    protected PVZPlant(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.KNOCKBACK_RESISTANCE, 1)
                .add(Attributes.MOVEMENT_SPEED, 0);
    }

    @Override
    public EntityDataAccessor<Boolean> root() {
        return ROOT;
    }
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
        return staticSkillSet;
    }

    @Override
    public void baseTick() {
        super.baseTick();
        //check plant situation damage.
        if (isPositionSafe(this.level, this.getOnPos()) != null && isVehicleSafe(getVehicle()) != null &&
                this.getAttribute(Attributes.MAX_HEALTH) != null && ++ situationHurtCount > 10) {
            this.hurt(PVZDamageHandler.PLANT_WILT, (float) (0.2 * this.getAttribute(Attributes.MAX_HEALTH).getValue()));
            //TODO change this to a new dmg type.
            situationHurtCount = 0;
        }
        //about aligning blocks.
        if (this.getDeltaMovement().distanceToSqr(new Vec3(0, 0, 0)) > 0.05 || ! this.isOnGround()) {
            shouldAlign = true;
        } else if (shouldAlign) {
            alignBlocks();
            setDeltaMovement(0, 0, 0);
            shouldAlign = false;
        }
        //live time count and wilt.

        //TODO relative codes. add particle when plant is dying.
    }


    /**
     * see {@link INeedSafeSituation}  for the two methods below.
     */
    @Override
    public MutableComponent isPositionSafe(Level level, BlockPos onPos) {
        VoxelShape tmpShape = level.getBlockState(onPos).getCollisionShape(level, onPos);
        double calcHeight = getBbHeight() + (tmpShape.isEmpty() ? 0 : tmpShape.bounds().maxY) - 1;
        for (int i = 1; i <= ceil(calcHeight); i ++) {
            if (! level.getBlockState(onPos.offset(new Vec3i(0, i, 0))).isAir()) {
                if (calcHeight - i >= 1) {
                    return Component.translatable("hint.pvz.plant.no_enough_place");
                } else if (calcHeight > 0) {
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
        if (! getEntityData().get(root()) || (level.getBlockState(onPos).is(PVZBlockTags.PLANTABLE_BLOCKS) && ! level.getBlockState(onPos).isAir())) {
            if (level.getBlockState(onPos).getFluidState().isEmpty()) {
                return null;
            }
            return Component.translatable("hint.pvz.plant.cant_plant_in_water", getName());
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
                    (entity) -> entity instanceof PVZPlant && entity.getEntityData().get(HAS_COINCIDE_DMG));
            return !list.isEmpty();
        }
    }
    public void alignBlocks() {
        BlockPos pos = this.getOnPos();
        moveTo(pos.getX() + 0.5, this.getY(), pos.getZ() + 0.5);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand handIn) {
        ItemStack itemstack = player.getItemInHand(handIn);
        if (itemstack.getItem() instanceof ShovelItem) {
            if (onBeingShoveled(player, handIn)) {
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, handIn);
    }

    public boolean onBeingShoveled(Player player, InteractionHand handIn) {
        //check permission.
        final boolean[] permission = {false};
        this.getCapability(PVZOwnedCapability.CAP).ifPresent((cap) -> {
            Entity owner = cap.getOwner();
            if (owner != null) {
            permission[0] = PVZRulesCapability.get("shovelPermission") ?
                    (PVZOwnedCapability.isTeammate(owner, player) || ! PVZRulesCapability.get("teamBattle")) : owner.is(player);
            } else {
                permission[0] = PVZRulesCapability.get("shovelPermission");
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
                for (int i = (int) (cap.cost * SunShovelEnchantment.returnSunPercent(enchantmentLevel)); i > 0; ) {
                    int sunAmount = i;
                    sunAmount = sunAmount > 50 ? 50 : sunAmount > 25 ? 25 : sunAmount > 15 ? 15 : Math.min(sunAmount, 5);
                    i -= sunAmount;
                    Sun.spawnByAmount(level, sunAmount, getOnPos().offset(0, 1, 0), new Vec3((random.nextFloat() - 0.5) * 0.25, random.nextFloat() * 0.1 + 0.15, (random.nextFloat() - 0.5) * 0.25));
                    for (int j = 15; j <= sunAmount; j += 15){
                        SpawnParticlePacket.particle(level, PVZParticles.SUN.get(), Vec3.atCenterOf(this.getOnPos()).add(0, 1, 0));
                    }
                }
            }
            this.remove(RemovalReason.DISCARDED);
            //TODO add particles.
            return true;
        }
        return false;
    }

    //data
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ROOT, true);
        this.entityData.define(HAS_COINCIDE_DMG, true);
        this.entityData.define(WILT_COUNTDOWN, -1);
        this.entityData.define(SKILL, 0);
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Skill", getSkillVal(this));
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        if (tag.contains("Skill")) {
            setSkillVal(this, tag.getInt("Skill"));
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

    @Nullable
    public ItemStack getPickResult() {
        AtomicReference<Item> packetItem = new AtomicReference<>();
        SeedPacketItem.seedPacketItemList.forEach(item -> {
            if (item.getEntity().equals(this.getType())) {
                packetItem.set(item);
        }});
        return packetItem.get() == null ? super.getPickResult() : new ItemStack(packetItem.get());
    }
}
