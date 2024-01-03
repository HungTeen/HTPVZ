package com.hungteen.pvz.client.renderer.bullet;

import com.hungteen.pvz.client.model.bullet.CommonBulletModel;
import com.hungteen.pvz.client.model.plants.TallNutModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.bullet.PeaBullet;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class PeaBulletRenderer extends CommonBulletRenderer<PeaBullet>{
    private static final ResourceLocation COMMON = Util.prefix("textures/entity/bullet/pea.png");
    private static final ResourceLocation FIRE = Util.prefix("textures/entity/bullet/ice_pea.png");
    private static final ResourceLocation ICE = Util.prefix("textures/entity/bullet/pea.png");
    private static final ResourceLocation POISON = Util.prefix("textures/entity/bullet/pea.png");


    public PeaBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected int getBlockLightLevel(PeaBullet bullet, BlockPos pos) {
        return bullet.getPeaType() == PeaBullet.PeaType.Fire ? 15 : super.getBlockLightLevel(bullet, pos);
    }
    @Override
    public ResourceLocation getTextureLocation(PeaBullet bullet) {
        ResourceLocation res;
        switch (bullet.getPeaType()) {
            case Ice -> res = ICE;
            case Fire -> res = FIRE;
            case Poison -> res = POISON;
            default -> res = COMMON;
        }
        return res;
    }
}
