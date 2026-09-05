package com.hungteen.pvz.mixin;

import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.commands.ItemCommands;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemCommands.class)
public class ItemCommandsMixin {

    @Inject(method = "getEntityItem", at = @At("HEAD"), cancellable = true)
    private static void getEntityItem(Entity p_180246_, int p_180247_, CallbackInfoReturnable<ItemStack> cir) throws CommandSyntaxException {
        if (p_180247_ >= 73562 && p_180247_ < 73571 && p_180246_ instanceof Player player) {
            cir.setReturnValue(PVZPlayerCapability.getEnderSeedBundleSlot(player, p_180247_ - 73562));
        }
    }

    @Redirect(
            method = "setEntityItem",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getSlot(I)Lnet/minecraft/world/entity/SlotAccess;")
    )
    private static SlotAccess redirectGetSlotInSet(Entity entity, int p_146919_) {
        SlotAccess original = entity.getSlot(p_146919_);
        if (p_146919_ >= 73562 && p_146919_ < 73571 && entity instanceof Player player) {
            return new PVZPlayerCapability.EnderSeedBundleSlotAccess(player, p_146919_ - 73562);
        }
        return original;
    }

    @Redirect(
            method = "modifyEntityItem",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getSlot(I)Lnet/minecraft/world/entity/SlotAccess;")
    )
    private static SlotAccess redirectGetSlotInModify(Entity entity, int p_146919_) {
        SlotAccess original = entity.getSlot(p_146919_);
        if (p_146919_ >= 73562 && p_146919_ < 73571 && entity instanceof Player player) {
            return new PVZPlayerCapability.EnderSeedBundleSlotAccess(player, p_146919_ - 73562);
        }
        return original;
    }
}