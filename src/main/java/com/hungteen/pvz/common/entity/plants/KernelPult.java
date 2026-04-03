package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.entity.plants.base.SimplePlant;
import com.hungteen.pvz.common.entity.bullet.BaseBullet;
import com.hungteen.pvz.common.entity.bullet.ButterBullet;
import com.hungteen.pvz.common.entity.bullet.CornBullet;
import com.hungteen.pvz.common.entity.plants.base.ShooterPlant;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.register.PVZMobEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;

import java.util.List;

public class KernelPult extends ShooterPlant {
    public static final EntityDataAccessor<Integer> CURRENT_BULLET = SynchedEntityData.defineId(KernelPult.class, EntityDataSerializers.INT);
    private static final int BUTTER_CHANCE = 3;
    protected static final double SHOOT_OFFSET = 0.3D;//pea position offset

    public static final String BUTTER_SKILL_NAME = "skill.pvz.kernel_pult.butter_pult";

    public static List<Skill> staticSkillList = List.of(
            new Skill(BUTTER_SKILL_NAME, PVZItems.ORIGIN_ESSENCE, 8, 8, 100, 300)
    );

    public KernelPult(EntityType<? extends Mob> type, Level worldIn) {
        super(type, worldIn);
    }

    public InteractionResult mobInteract(Player player, InteractionHand handIn) {
        ItemStack itemstack = player.getItemInHand(handIn);
        if (itemstack.is(Items.GLASS_BOTTLE) && this.getCurrentBullet() == CornTypes.BUTTER) {
//            player.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F); TODO add sound.
            if (! this.level.isClientSide) {
                ItemStack itemstack1 = PotionUtils.setPotion(Items.POTION.getDefaultInstance(), PVZMobEffects.potionMap.get("butter").get());
                ItemStack itemstack2 = ItemUtils.createFilledResult(itemstack, player, itemstack1, false);
                player.setItemInHand(handIn, itemstack2);
                this.setCurrentBullet(CornTypes.KERNEL);
            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        } else {
            return super.mobInteract(player, handIn);
        }
    }
    @Override
    public List<Skill> getBasicStaticSkillList(){
        return staticSkillList;
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CURRENT_BULLET, CornTypes.KERNEL.ordinal());
        changeBullet();
    }
    @Override
    public void shootBullet() {
        this.performShoot(SHOOT_OFFSET, 0, 0, true, 0);
        this.changeBullet();
    }
    @Override
    public double getMaxShootAngleTangent() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    protected BaseBullet createBullet() {
        if(this.getCurrentBullet() == CornTypes.BUTTER) {
            ButterBullet bullet = new ButterBullet(level, this);
            return bullet;
        }
        return new CornBullet(level, this);
    }

    @Override
    public float getAttackDamage() {
        return (float) getAttribute(Attributes.ATTACK_DAMAGE).getValue() * (getCurrentBullet() == CornTypes.BUTTER ? (this.hasSkill(BUTTER_SKILL_NAME) ? 0.5F : 2) : 1);
    }
    @Override
    public int getShootCD() {
        return this.hasSkill(BUTTER_SKILL_NAME) ? 80 : 40;
    }
    @Override
    public int shootAnimLength() {
        return 15;
    }
    @Override
    public float getBulletSpeed() {
        Entity target = this.getTarget();
        if (target != null) {
            double distance = target.distanceTo(this);
            return (float) (Math.max(0.5 * distance / 12, 0.1));
        }
        return 0.5F;
    }

    protected void changeBullet() {
        if (hasSkill(BUTTER_SKILL_NAME)) {
            setCurrentBullet(CornTypes.BUTTER);
            return;
        }
        this.setCurrentBullet(this.getRandom().nextInt(BUTTER_CHANCE) == 0 ? CornTypes.BUTTER : CornTypes.KERNEL);
    }
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if(compound.contains("current_bullet_type")) {
            this.setCurrentBullet(CornTypes.values()[compound.getInt("current_bullet_type")]);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("current_bullet_type", this.getCurrentBullet().ordinal());
    }

    public void setCurrentBullet(CornTypes type) {
        this.entityData.set(CURRENT_BULLET, type.ordinal());
    }
    public CornTypes getCurrentBullet() {
        return CornTypes.values()[this.entityData.get(CURRENT_BULLET)];
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.FOLLOW_RANGE, 24D)
                .add(Attributes.ATTACK_DAMAGE, 2D)
                .add(Attributes.ATTACK_KNOCKBACK, 0D);
    }
    public enum CornTypes{
        KERNEL,
        BUTTER
    }
}
