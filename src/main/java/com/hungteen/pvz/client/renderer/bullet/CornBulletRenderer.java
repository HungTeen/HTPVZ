package com.hungteen.pvz.client.renderer.bullet;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.common.entity.bullet.CornBullet;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CornBulletRenderer extends CommonBulletRenderer<CornBullet>{
    private static final ResourceLocation MODEL = Util.prefix("textures/entity/bullet/corn.png");
    private static final ResourceLocation ITEM = Util.prefix("textures/entity/bullet/corn_item.png");

    public CornBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(CornBullet bullet) {
        if (PVZConfig.Client.renderBulletAsModel.get()) {
            return MODEL;
        } else {
            return ITEM;
        }
    }
}
