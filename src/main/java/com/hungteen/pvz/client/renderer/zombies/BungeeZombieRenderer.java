package com.hungteen.pvz.client.renderer.zombies;

import com.hungteen.pvz.client.model.zombie.PVZZombieModel;
import com.hungteen.pvz.common.entity.zombies.BungeeZombie;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BungeeZombieRenderer<T extends BungeeZombie, M extends PVZZombieModel<T>> extends PVZZombieRenderer<T, M>{
    private static final ResourceLocation TEXTURE = Util.prefix("textures/entity/zombie/bungee_zombie/bungee_zombie.png");
    public BungeeZombieRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(T zombie, float p_115456_, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int p_115460_) {
        super.render(zombie, p_115456_, partialTicks, poseStack, bufferSource, p_115460_);
    }

    @Override
    public ResourceLocation getTextureLocation(T zombie) {
        return TEXTURE;
    }

}
