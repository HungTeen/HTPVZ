package com.hungteen.pvz.client.renderer.bullet;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.common.entity.bullet.ButterBullet;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ButterBulletRenderer extends CommonBulletRenderer<ButterBullet>{
    private static final ResourceLocation MODEL = Util.prefix("textures/entity/bullet/butter.png");
    private static final ResourceLocation ITEM = Util.prefix("textures/entity/bullet/butter_item.png");

    public ButterBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(ButterBullet bullet) {
        if (PVZConfig.Client.renderBulletAsModel.get()) {
            return MODEL;
        } else {
            return ITEM;
        }
    }
}
