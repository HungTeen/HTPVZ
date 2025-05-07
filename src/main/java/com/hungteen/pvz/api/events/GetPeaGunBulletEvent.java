package com.hungteen.pvz.api.events;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/**Get bullet for Pea Guns. The event is fired in Server and Client(To judge if to apply recoil) before and overrides vanilla PVZ logic.
 * <br>To judge if a itemStack is allowed, use {@link net.minecraftforge.common.ForgeHooks#getProjectile getProjectile()}.*/
@Cancelable
public class GetPeaGunBulletEvent extends PlayerEvent {
    public final Level level;
    public final EquipmentSlot slot;
    public final ItemStack itemStack;
    public Projectile projectile;
    /**@param itemStack the bullet itemStack the gun use.
     * @param slot the equipment slot Pea Gun in. Can be {@link EquipmentSlot#MAINHAND} or {@link  EquipmentSlot#OFFHAND}.*/
    public GetPeaGunBulletEvent(Player player, Level level, EquipmentSlot slot, ItemStack itemStack, Projectile projectile) {
        super(player);
        this.itemStack = itemStack;
        this.level = level;
        this.slot = slot;
        this.projectile = projectile;
    }
}
