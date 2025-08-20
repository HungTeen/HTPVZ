package com.hungteen.pvz.common.entity;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.client.ClientUtil;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.PVZEntities;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

/**<b>CLIENT entity! Do not try to summon in server! </b><br>
 * ModelPartEntities renders fallen parts of models in the client game just like particles and won't be stored. They can both render entity ModelParts and item models.*/

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class ModelPartEntity extends Entity {
    public static Set<ModelPartEntity> entities = new HashSet<>();

    /**When rendering a model, a texture should be defined.*/
    @OnlyIn(Dist.CLIENT)
    public ModelPart model;
    public ResourceLocation texture;

    public ItemStack itemStack;
    //ItemStacks contains no rotation information, so added two.
    public Vec3 currentRotation = null;


    public Vec3 rotation;
    public Level level;
    public Vec3 originalScale;
    public int life;


    //if ModelPart
    public ModelPartEntity(EntityType<?> entityType, Level p_19871_) {
        this(p_19871_, 80);
    }
    @OnlyIn(Dist.CLIENT)
    public ModelPartEntity(Level p_19871_, ModelPart model, ResourceLocation texture) {
        this(p_19871_, model, texture, 80);
    }
    @OnlyIn(Dist.CLIENT)
    public ModelPartEntity(Level p_19871_, ModelPart model, ResourceLocation texture, int life) {
        this(p_19871_, life);
        this.model = ClientUtil.copyModelPart(model);
        this.model.x = 0;
        this.model.y = 0;
        this.model.z = 0;
        this.texture = texture;
        this.originalScale = new Vec3(model.xScale, model.yScale, model.zScale);
    }
    //if ItemStack
    @OnlyIn(Dist.CLIENT)
    public ModelPartEntity(Level p_19871_, ItemStack itemStack, int life) {
        this(p_19871_, life);
        this.itemStack = itemStack;
        this.currentRotation = Vec3.ZERO;
        this.originalScale = new Vec3(1, 1, 1);
    }
    @OnlyIn(Dist.CLIENT)
    public ModelPartEntity(Level p_19871_, ItemStack itemStack) {
        this(p_19871_, itemStack, 80);
    }

    //root
    public ModelPartEntity(Level level, int life) {
        super(PVZEntities.MODEL_PART.get(), level);
        this.life = life;
        this.rotation = Vec3.ZERO;
        this.setDeltaMovement(
                new Vec3(this.random.nextFloat() * 0.5 - 0.25, this.random.nextFloat() * 0.25, this.random.nextFloat()* 0.5 - 0.25));
        if (! level.isClientSide) {
            PVZMod.LOGGER.error("trying to summon ModelPartEntity [" + this.getUUID() + "] in server! ");
        }
    }

    public ModelPartEntity pos(Vec3 vec3) {
        this.moveTo(vec3);
        return this;
    }

    public ModelPartEntity speed(Vec3 vec3) {
        this.setDeltaMovement(vec3);
        return this;
    }

    public ModelPartEntity join(Level level) {
        this.level = level;
        entities.add(this);
        return this;
    }

    public ModelPartEntity noGravity(boolean bool) {
        this.setNoGravity(bool);
        return this;
    }

    public ModelPartEntity rotation(Vec3 vec3) {
        this.rotation = vec3;
        return this;
    }

    public ModelPartEntity scale(Vec3 vec3) {
        this.originalScale = vec3;
        return this;
    }
    public ModelPartEntity scale(float scale) {
        this.originalScale = new Vec3(scale, scale, scale);
        return this;
    }


    //settings
    /**Resize the rotation and scale every render tick if necessary because ModelPartEntity resets the size itself before the start of render tick. */
    public void baseTick() {
        this.level.getProfiler().push("entityBaseTick");

        if (this.level.isClientSide) {
            this.clearFire();
            if (model != null) {
                this.model.xRot += (float) rotation.x;
                this.model.yRot += (float) rotation.y;
                this.model.zRot += (float) rotation.z;
            } else {
                this.currentRotation = this.currentRotation.add(rotation);
            }
        } else {
            PVZMod.LOGGER.error("find ModelPartEntity [" + this.getUUID() + "] in server! ");
        }

        this.checkOutOfWorld();

        this.firstTick = false;

        if (this.tickCount > life) {
            this.discard();
        }


        if (level.getBlockState(new BlockPos(this.position().add(this.getDeltaMovement())))
                .isSuffocating(level, new BlockPos(this.position().add(this.getDeltaMovement())))) {
            this.setDeltaMovement(0 ,0, 0);
            this.rotation = Vec3.ZERO;
        }
        this.moveTo(this.getX() + this.getDeltaMovement().x, this.getY() + this.getDeltaMovement().y, this.getZ() + this.getDeltaMovement().z);
        if (! this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, -0.05, 0));
        }
        if (this.level.isClientSide) {
            float size = (float) (this.life - this.tickCount) / 5;
            if (size < 1) {
                if (this.model != null) {
                    this.model.xScale = (float) originalScale.x * size;
                    this.model.yScale = (float) originalScale.y * size;
                    this.model.zScale = (float) originalScale.z * size;
                } else {
                    this.originalScale = new Vec3(size, size, size);
                }
            }
        }

        this.level.getProfiler().pop();

    }

    protected void defineSynchedData() {}

    protected void readAdditionalSaveData(CompoundTag p_20052_) {}

    protected void addAdditionalSaveData(CompoundTag p_20139_) {}

    public Packet<?> getAddEntityPacket() {
        return null;
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void ClientTickModelPart(TickEvent.ClientTickEvent ev) {
        if (ev.phase == TickEvent.Phase.START) {
            Set.copyOf(entities).forEach(entity -> {
                entity.tick();
                entity.tickCount ++;
                if (entity.isRemoved()) {
                    entities.remove(entity);
                }
            });
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void temporaryModelPartEntityRenderingMethod(RenderLevelStageEvent ev) {
        //to circumvent a crushing bug. TODO find reason and add this into vanilla method.
        if (ev.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            Vec3 vec3 = ev.getCamera().getPosition();
            double x = vec3.x();
            double y = vec3.y();
            double z = vec3.z();
            entities.forEach(entity -> {
                if (entity != null) {
                    if (ClientProxy.getPlayer().level == entity.level) {
                        ClientProxy.MC.levelRenderer.renderEntity(entity, x, y, z, ev.getPartialTick(),
                                ev.getPoseStack(), ClientProxy.MC.levelRenderer.renderBuffers.bufferSource());
                    }
                }
            });
        }
    }
}
