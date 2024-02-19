package com.hungteen.pvz.client.renderer.bullet;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.common.entity.bullet.PeaBullet;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class PeaBulletRenderer extends CommonBulletRenderer<PeaBullet>{
    private static final ResourceLocation COMMON = Util.prefix("textures/entity/bullet/pea.png");
    private static final ResourceLocation FIRE = Util.prefix("textures/entity/bullet/flame_pea.png");
    private static final ResourceLocation ICE = Util.prefix("textures/entity/bullet/snow_pea.png");
    private static final ResourceLocation POISON = Util.prefix("textures/entity/bullet/poisonous_pea.png");
    private static final ResourceLocation COMMON_ITEM = Util.prefix("textures/item/pea.png");
    private static final ResourceLocation FIRE_ITEM = Util.prefix("textures/entity/bullet/flame_pea_item.png");
    private static final ResourceLocation ICE_ITEM = Util.prefix("textures/entity/bullet/snow_pea_item.png");
    private static final ResourceLocation POISON_ITEM = Util.prefix("textures/entity/bullet/poisonous_pea_item.png");


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
        if (PVZConfig.renderBulletAsModel()) {
            switch (bullet.getPeaType()) {
                case Ice -> res = ICE;
                case Fire -> res = FIRE;
                case Poison -> res = POISON;
                default -> res = COMMON;
            }
        } else {
            switch (bullet.getPeaType()) {
                case Ice -> res = ICE_ITEM;
                case Fire -> res = FIRE_ITEM;
                case Poison -> res = POISON_ITEM;
                default -> res = COMMON_ITEM;
            }
        }
        return res;
    }
}
