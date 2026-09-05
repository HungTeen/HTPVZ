package com.hungteen.pvz.api.interfaces;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**With this interface item can drop on broken with using PvZ's DropDamagedArmorPacket. However, when to drop it should be defined manually. */
public interface IDropWhenBroken /*extends Item*/ {
    @OnlyIn(Dist.CLIENT)
    void clientBroken(Vec3 pos, Level level);
}
