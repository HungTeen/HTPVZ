package com.hungteen.pvz.client.renderer.zombie;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.client.model.zombie.PVZZombieModel;
import com.hungteen.pvz.common.entity.ModelPartEntity;
import com.hungteen.pvz.common.entity.zombies.PVZZombie;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.io.FileNotFoundException;

public class PVZZombieRenderer<T extends PVZZombie, M extends PVZZombieModel<T>> extends HumanoidMobRenderer<T, M> {
    private static final ResourceLocation OVERWORLD_LOCATION = Util.prefix("textures/entity/zombie/minecraft_overworld_zombie.png");
    public PVZZombieRenderer(EntityRendererProvider.Context context) {
        super(context, (M) new PVZZombieModel<T>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR))));
        if (PVZConfig.renderZombieStuckArrows()) {
            this.addLayer(new ArrowLayer<>(context, this));
        }
    }

    @Override
    public void render(T zombie, float p_115456_, float p_115457_, PoseStack p_115458_, MultiBufferSource p_115459_, int p_115460_) {
        if (PVZConfig.zombieDropParts() && ! Minecraft.getInstance().isPaused()) {
            if (zombie.renderHand && zombie.shouldDropHand()) {
                zombie.renderHand = false;
                Vec3 speed = new Vec3(zombie.getRandom().nextFloat() * 0.25 - 0.125,
                        zombie.getRandom().nextFloat() * 0.15,
                        zombie.getRandom().nextFloat() * 0.25 - 0.125);
                new ModelPartEntity(zombie.level, model.leftArm, getTextureLocation(zombie)).pos(zombie.position().add(0, zombie.getBbHeight(), 0))
                        .speed(speed).rotation(new Vec3(0.5, 0.5, 0.5)).join(zombie.level);
                new ModelPartEntity(zombie.level, model.leftSleeve, getTextureLocation(zombie)).pos(zombie.position().add(0,  zombie.getBbHeight() * 0.75, 0))
                        .speed(speed).rotation(new Vec3(0.5, 0.5, 0.5)).join(zombie.level);
            }
            if (zombie.renderHead && zombie.shouldDropHead()) {
                zombie.renderHead = false;
                Vec3 speed = new Vec3(zombie.getRandom().nextFloat() * 0.25 - 0.125,
                        zombie.getRandom().nextFloat() * 0.15,
                        zombie.getRandom().nextFloat() * 0.25 - 0.125);
                new ModelPartEntity(zombie.level, model.head, getTextureLocation(zombie)).pos(zombie.position().add(0, 1, 0))
                        .speed(speed).rotation(new Vec3(0.5, 0.5, 0.5)).join(zombie.level);
                new ModelPartEntity(zombie.level, model.hat, getTextureLocation(zombie)).pos(zombie.position().add(0, 1, 0))
                        .speed(speed).rotation(new Vec3(0.5, 0.5, 0.5)).join(zombie.level);
            }
        }
        super.render(zombie, p_115456_, p_115457_, p_115458_, p_115459_, p_115460_);
    }

    @Override
    public ResourceLocation getTextureLocation(T zombie) {
        try {
            ResourceLocation res = zombie.getStyle().equals("") ? OVERWORLD_LOCATION :
                    Util.prefix("textures/entity/zombie/" + zombie.getStyle() + "_zombie.png");
            ClientProxy.MC.getResourceManager().getResourceOrThrow(res);
            return res;
        } catch (FileNotFoundException e) {
            return OVERWORLD_LOCATION;
        }
    }
}
