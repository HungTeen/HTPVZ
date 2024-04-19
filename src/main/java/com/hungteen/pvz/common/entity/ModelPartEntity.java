package com.hungteen.pvz.common.entity;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.PVZEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

/**CLIENT entity! Do not try to summon in server. */

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class ModelPartEntity extends Entity {
    public static Set<ModelPartEntity> entities = new HashSet<>();
    public final ModelPart model;
    public final ResourceLocation texture;
    public Vec3 rotation;
    public Level level;
    public Vec3 originalScale;
    public int life;

    public ModelPartEntity(EntityType<?> p_19870_, Level p_19871_) {
        this(p_19871_, null, null, 80);
    }
    public ModelPartEntity(Level p_19871_, ModelPart model, ResourceLocation texture) {
        this(p_19871_, model, texture, 80);
    }

    public ModelPartEntity(Level level, ModelPart model, ResourceLocation texture, int life) {
        super(PVZEntities.MODEL_PART.get(), level);
        this.model = copyModelPart(model);
        this.originalScale = new Vec3(model.xScale, model.yScale, model.zScale);
        this.texture = texture;
        this.life = life;
        this.rotation = Vec3.ZERO;
        this.setDeltaMovement(
                new Vec3(this.random.nextFloat() * 0.5 - 0.25, this.random.nextFloat() * 0.25, this.random.nextFloat()* 0.5 - 0.25));
        if (! level.isClientSide) {
            PVZMod.LOGGER.error("try to summon ModelPartEntity [" + this.getUUID() + "] in server! ");
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
//        if (level instanceof ClientLevel cLevel) {
//            int id = -7356299;
//            if (cLevel.getEntity(id) == null) {
//                cLevel.putNonPlayerEntity(id, this);
//            }
//        }
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



    //
    public static ModelPart copyModelPart(ModelPart original) {
        Map<String, ModelPart> children = new HashMap<>();
        for (String part : original.children.keySet()) {
            children.put(part, copyModelPart(original.children.get(part)));
        }
        ModelPart newPart = new ModelPart(original.cubes, children);
        newPart.xScale = original.xScale;
        newPart.yScale = original.yScale;
        newPart.zScale = original.zScale;
        return newPart;
    }

    //settings
    public void baseTick() {
        this.level.getProfiler().push("entityBaseTick");

        if (this.level.isClientSide) {
            this.clearFire();
            this.model.xRot += rotation.x;
            this.model.yRot += rotation.y;
            this.model.zRot += rotation.z;
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
            if (this.life - this.tickCount < 5) {
                this.model.xScale = (float) originalScale.x * (this.life - this.tickCount) / 5;
                this.model.yScale = (float) originalScale.y * (this.life - this.tickCount) / 5;
                this.model.zScale = (float) originalScale.z * (this.life - this.tickCount) / 5;
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
