package com.hungteen.pvz.client.renderer.bullet;

import com.hungteen.pvz.common.entity.bullet.PeaBullet;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class PeaBulletRenderer extends CommonBulletRenderer<PeaBullet>{
    private static final ResourceLocation COMMON = Util.prefix("entity/bullet/pea/pea_bullet.png");


    public PeaBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(PeaBullet p_114482_) {
        return COMMON;
    }
}
