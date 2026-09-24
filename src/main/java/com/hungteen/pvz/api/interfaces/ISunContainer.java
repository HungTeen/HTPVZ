package com.hungteen.pvz.api.interfaces;

import com.hungteen.pvz.common.register.PVZAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/**ISunContainer includes all object that can store sun. Remember player is also a kind of sun container.*/
public interface ISunContainer {
    int getAmount();
    /**If this is a LivingEntity, use attribute {@link com.hungteen.pvz.common.register.PVZAttributes#MAX_SUN SUN} as capacity of this entity.*/
    int getCapacity();
    default Vec3 position() {
        return this instanceof Entity ? ((Entity) this).position().add(0, ((Entity) this).getBbHeight() / 2, 0) :
                Vec3.atCenterOf(((BlockEntity) this).getBlockPos());
    }

    record PlayerSunContainerWrapper(Player player) implements ISunContainer {
        @Override
            public int getAmount() {
                return 0;
            }

            @Override
            public int getCapacity() {
                return (int) player.getAttribute(PVZAttributes.MAX_SUN.get()).getValue();
            }
        }
}
