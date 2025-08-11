package com.hungteen.pvz.client.model.zombie;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.client.ClientUtil;
import com.hungteen.pvz.common.entity.zombies.PVZZombie;
import com.hungteen.pvz.common.entity.zombies.zombotany.IZombotany;
import com.hungteen.pvz.common.network.ClientProxy;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ZombotanyModel<T extends PVZZombie & IZombotany> extends PVZZombieModel<T> {
    protected ModelPart attachedHead;
    ResourceLocation textureLocation;
    MultiBufferSource multiBufferSource;
    private static final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();
    public ZombotanyModel(ModelPart modelPart) {
        super(modelPart);
        head.children.clear();
    }

    @Override
    public void setupAnim(T zombie, float p_102002_, float p_102003_, float p_102004_, float p_102005_, float p_102006_) {
        super.setupAnim(zombie, p_102002_, p_102003_, p_102004_, p_102005_, p_102006_);
        LivingEntityRenderer<?,?> headRenderer = null;
        if (ClientProxy.MC.getEntityRenderDispatcher().renderers.get(zombie.getPlantType()) instanceof LivingEntityRenderer<?,?> renderer) {
            headRenderer = renderer;
        }
        if (headRenderer != null) {
            try {
                this.textureLocation = zombie.getPlantTextureLocation() == null ?
                        headRenderer.getTextureLocation(null) : zombie.getPlantTextureLocation();
            } catch (Exception ignored) {
                PVZMod.LOGGER.error("Missing Head Texture for zombotany " + zombie);
                this.textureLocation = new ResourceLocation("missingno");
            }
        }
        if (this.attachedHead == null) {
            if (headRenderer != null) {
                this.attachedHead = ClientUtil.copyModelPart(ClientUtil.getFirstHead(headRenderer.getModel()));
                this.attachedHead.getAllParts().forEach(ModelPart::resetPose);
            }
            if (attachedHead != null) {
                if (ClientProxy.MC.levelRenderer.shouldShowEntityOutlines() && ClientProxy.MC.shouldEntityAppearGlowing(zombie)) {
                    OutlineBufferSource outlinebuffersource = ClientProxy.MC.levelRenderer.renderBuffers.outlineBufferSource();
                    this.multiBufferSource = outlinebuffersource;
                    int i = zombie.getTeamColor();
                    int k = i >> 16 & 255;
                    int l = i >> 8 & 255;
                    int i1 = i & 255;
                    outlinebuffersource.setColor(k, l, i1, 255);
                } else {
                    this.multiBufferSource = ClientProxy.MC.levelRenderer.renderBuffers.bufferSource();;
                }
                this.head = this.attachedHead;
            }
        }
        if (attachedHead != null) {
            Vec3 offset = zombie.getPlantHeadOffset();
            this.head.x += (float) offset.x;
            this.head.y += (float) offset.y;
            this.head.z += (float) offset.z;
            this.head.xScale = zombie.getPlantHeadScale();
            this.head.yScale = zombie.getPlantHeadScale();
            this.head.zScale = zombie.getPlantHeadScale();
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int p_102036_, int p_102037_, float p_102038_, float p_102039_, float p_102040_, float p_102041_) {
        if (this.attachedHead == null || ! this.head.visible) {
            super.renderToBuffer(poseStack, vertexConsumer, p_102036_, p_102037_, p_102038_, p_102039_, p_102040_, p_102041_);
            return;
        }
        this.head.visible = false;
        super.renderToBuffer(poseStack, vertexConsumer, p_102036_, p_102037_, p_102038_, p_102039_, p_102040_, p_102041_);
        VertexConsumer headConsumer = this.multiBufferSource.getBuffer(RenderType.entityTranslucent(this.textureLocation));
        this.head.visible = true;
        this.head.render(poseStack, headConsumer, p_102036_, p_102037_, p_102038_, p_102039_, p_102040_, p_102041_);
    }
    protected void animate(AnimationState p_233382_, AnimationDefinition p_233383_, float p_233384_) {
        this.animate(p_233382_, p_233383_, p_233384_, 1.0F);
    }

    protected void animate(AnimationState p_233386_, AnimationDefinition p_233387_, float p_233388_, float p_233389_) {
        p_233386_.updateTime(p_233388_, p_233389_);
        p_233386_.ifStarted((p_233392_) -> {
            animate(this, p_233387_, p_233392_.getAccumulatedTime(), 1.0F, ANIMATION_VECTOR_CACHE);
        });
    }
    public static void animate(ZombotanyModel<?> p_232320_, AnimationDefinition p_232321_, long p_232322_, float p_232323_, Vector3f p_232324_) {
        float f = getElapsedSeconds(p_232321_, p_232322_);

        for(Map.Entry<String, List<AnimationChannel>> entry : p_232321_.boneAnimations().entrySet()) {
            Optional<ModelPart> optional = p_232320_.getAnyDescendantWithName(entry.getKey());
            List<AnimationChannel> list = entry.getValue();
            optional.ifPresent((p_232330_) -> {
                list.forEach((p_232311_) -> {
                    Keyframe[] akeyframe = p_232311_.keyframes();
                    int i = Math.max(0, Mth.binarySearch(0, akeyframe.length, (p_232315_) -> f <= akeyframe[p_232315_].timestamp()) - 1);
                    int j = Math.min(akeyframe.length - 1, i + 1);
                    Keyframe keyframe = akeyframe[i];
                    Keyframe keyframe1 = akeyframe[j];
                    float f1 = f - keyframe.timestamp();
                    float f2 = Mth.clamp(f1 / (keyframe1.timestamp() - keyframe.timestamp()), 0.0F, 1.0F);
                    keyframe1.interpolation().apply(p_232324_, f2, akeyframe, i, j, p_232323_);
                    p_232311_.target().apply(p_232330_, p_232324_);
                });
            });
        }
    }
    private static float getElapsedSeconds(AnimationDefinition p_232317_, long p_232318_) {
        float f = (float)p_232318_ / 1000.0F;
        return p_232317_.looping() ? f % p_232317_.lengthInSeconds() : f;
    }
    public Optional<ModelPart> getAnyDescendantWithName(String p_233394_) {
        return this.attachedHead.getAllParts().filter((p_233400_) -> {
            return p_233400_.hasChild(p_233394_);
        }).findFirst().map((p_233397_) -> {
            return p_233397_.getChild(p_233394_);
        });
    }
}
