package com.hungteen.pvz.client.particle;

import com.hungteen.pvz.client.ClientUtil;
import com.hungteen.pvz.common.network.ClientProxy;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ModelPartParticle extends Particle {
    private static final RenderType SHADOW_RENDER_TYPE = RenderType.entityShadow(new ResourceLocation("textures/misc/shadow.png"));

    public ModelPart model;
    public ResourceLocation texture;
    public ItemStack itemStack;

    public Vec3 rotation = Vec3.ZERO;
    public Vec3 offset = Vec3.ZERO;

    public Vec3 originalScale = new Vec3(1, 1, 1);

    public Vec3 aRotation = Vec3.ZERO;

    private static final EntityRenderDispatcher entityRenderDispatcher = ClientProxy.MC.getEntityRenderDispatcher();
    private static final RenderBuffers renderBuffers = ClientProxy.MC.renderBuffers();


    //for modelPart
    public ModelPartParticle(LivingEntity entity, ModelPart model, ResourceLocation texture, Vec3 offset) {
        this((ClientLevel) entity.level, model, texture, entity.position().add(offset));
        Vec3 speed = entity.getDeltaMovement()
                .multiply(0.8, 0.8, 0.8)
                .add(new Vec3(entity.getRandom().nextFloat() * 0.2 - 0.1,
                        entity.getRandom().nextFloat() * 0.3 + 0.1,
                        entity.getRandom().nextFloat() * 0.2 - 0.1));
        this.speed(speed).scale(entity.isBaby() ? 0.5F : 1F).rotation(speed.multiply(80, 80, 80));
        ClientProxy.MC.particleEngine.add(this);
    }
    public ModelPartParticle(LivingEntity entity, List<ModelPart> models, ResourceLocation texture, Vec3 offset) {
        this(entity, models.get(0), texture, offset);
        List<ModelPartParticle> partParticles = new ArrayList<>();
        partParticles.add(this);
        for (int i = 1; i < models.size(); i ++) {
            partParticles.add(new ModelPartParticle(entity, models.get(i), texture, offset));
        }
        Vec3 speed = entity.getDeltaMovement()
                .multiply(0.8, 0.8, 0.8)
                .add(new Vec3(entity.getRandom().nextFloat() * 0.2 - 0.1,
                        entity.getRandom().nextFloat() * 0.3 + 0.1,
                        entity.getRandom().nextFloat() * 0.2 - 0.1));
        for (var i : partParticles) {
            i.speed(speed).scale(entity.isBaby() ? 0.5F : 1F).rotation(speed.multiply(80, 80, 80));
            ClientProxy.MC.particleEngine.add(i);
        }
    }
    public ModelPartParticle(ClientLevel level, ModelPart model, ResourceLocation texture, Vec3 position) {
        this(level, position);
        this.model = ClientUtil.copyModelPart(model);
        this.model.x = 0;
        this.model.y = 0;
        this.model.z = 0;
        this.texture = texture;
        this.originalScale = new Vec3(model.xScale, model.yScale, model.zScale);
    }
    //for itemStack
    public ModelPartParticle(LivingEntity entity, ItemStack itemStack, Vec3 offset) {
        this((ClientLevel) entity.level, itemStack, entity.position().add(offset));
        Vec3 speed = entity.getDeltaMovement()
                .multiply(0.8, 0.8, 0.8)
                .add(new Vec3(entity.getRandom().nextFloat() * 0.2 - 0.1,
                        entity.getRandom().nextFloat() * 0.3 + 0.1,
                        entity.getRandom().nextFloat() * 0.2 - 0.1));
        this.speed(speed).scale(entity.isBaby() ? 0.5F : 1F).rotation(speed.multiply(80, 80, 80));
        ClientProxy.MC.particleEngine.add(this);
    }
    public ModelPartParticle(ClientLevel level, ItemStack itemStack, Vec3 position) {
        this(level, position);
        this.itemStack = itemStack;
        this.rotation = Vec3.ZERO;
        this.originalScale = new Vec3(1, 1, 1);
    }

    public ModelPartParticle(ClientLevel level, Vec3 position) {
        this(level, position, Vec3.ZERO);
    }

    public ModelPartParticle(ClientLevel level, Vec3 position, Vec3 velocity) {
        super(level, position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
        this.gravity = 2F;
        this.friction = 1f;
        this.lifetime = 80;
    }

    public ModelPartParticle pos(Vec3 vec3) {
        this.x = vec3.x;
        this.y = vec3.y;
        this.z = vec3.z;
        return this;
    }
    public ModelPartParticle offset(Vec3 vec3) {
        this.offset = vec3;
        return this;
    }

    public ModelPartParticle speed(Vec3 vec3) {
        this.xd = vec3.x;
        this.yd = vec3.y;
        this.zd = vec3.z;
        return this;
    }

    public ModelPartParticle gravity(float gravity) {
        this.gravity = gravity;
        return this;
    }

    public ModelPartParticle rotation(Vec3 vec3) {
        this.aRotation = vec3;
        return this;
    }

    public ModelPartParticle scale(Vec3 vec3) {
        this.originalScale = vec3;
        return this;
    }
    public ModelPartParticle scale(float scale) {
        this.originalScale = new Vec3(scale, scale, scale);
        return this;
    }
    public ModelPartParticle life(int life) {
        this.lifetime = life;
        return this;
    }

    @Override
    public void tick() {
        super.tick();
        this.rotation = this.rotation.add(aRotation);
    }

    @Override
    public void move(double x, double y, double z) {
        super.move(x, y, z);
        if (this.stoppedByCollision) {
            this.aRotation = Vec3.ZERO;
        }
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float partialTick) {
        float disappearSize = (this.lifetime - partialTick - this.age) / 5;
        if (disappearSize > 0) {
            PoseStack poseStack = new PoseStack();
            MultiBufferSource.BufferSource bufferSource = renderBuffers.bufferSource();
            BlockPos blockpos = new BlockPos(x, y, z);
            Vec3 camPos = camera.getPosition();
            int light = LightTexture.pack(this.level.getBrightness(LightLayer.BLOCK, blockpos), this.level.getBrightness(LightLayer.SKY, blockpos));
            poseStack.pushPose();
            poseStack.translate(
                    x * partialTick + xo * (1 - partialTick) - camPos.x()
                    , y * partialTick + yo * (1 - partialTick) - camPos.y()
                    , z * partialTick + zo * (1 - partialTick) - camPos.z());
            poseStack.scale((float) (-1 * this.originalScale.x), (float) (-1 * this.originalScale.y), (float) (1 * this.originalScale.z));
            if (disappearSize < 1) {
                poseStack.scale(disappearSize, disappearSize, disappearSize);
            }
            Vec3 rotation = this.rotation.add(aRotation.multiply(partialTick, partialTick, partialTick));
            if (this.model != null) { // render ModelPart
                VertexConsumer vertexConsumer1 = bufferSource.getBuffer(
                        RenderType.entityTranslucent(texture));
                poseStack.mulPose(Vector3f.ZP.rotationDegrees((float) rotation.z));
                poseStack.mulPose(Vector3f.YP.rotationDegrees((float) rotation.y));
                poseStack.mulPose(Vector3f.XP.rotationDegrees((float) rotation.x));
                poseStack.translate(offset.x, offset.y, offset.z);
                model.render(poseStack, vertexConsumer1, light, OverlayTexture.NO_OVERLAY);
            } else if (itemStack != null && ! itemStack.isEmpty()) { // render Item
                poseStack.pushPose();
                poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
                poseStack.translate(0, 1, 0);
                poseStack.mulPose(Vector3f.ZP.rotationDegrees((float) rotation.z));
                poseStack.mulPose(Vector3f.YP.rotationDegrees((float) rotation.y));
                poseStack.mulPose(Vector3f.XP.rotationDegrees((float) rotation.x));
                poseStack.translate(offset.x, offset.y, offset.z);
                entityRenderDispatcher.getItemInHandRenderer().renderItem(ClientProxy.getPlayer(), itemStack,
                        ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, true,
                        poseStack, bufferSource, light);
                poseStack.popPose();
            }
            poseStack.popPose();
            bufferSource.endBatch();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }
}
