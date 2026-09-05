package com.hungteen.pvz.compat;

import com.hungteen.pvz.util.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.patchouli.api.PatchouliAPI;

public class PatchouliCompatHandler {
    public static final String PATCHOULI = "patchouli";
    public static final ResourceLocation GUIDE_BOOK = new ResourceLocation(PATCHOULI, "guide_book");
    public static final ResourceLocation PVZ_GUIDE = Util.prefix("pvz_guide");

    public static ItemStack getPatchouliGuide() {
        if(isPatchouliLoaded() && ForgeRegistries.ITEMS.containsKey(GUIDE_BOOK)) {
            return getPatchouliAPI().getBookStack(PVZ_GUIDE);
        }
        return ItemStack.EMPTY;
    }

    public static PatchouliAPI.IPatchouliAPI getPatchouliAPI() {
        return PatchouliAPI.get();
    }

    public static boolean isPatchouliLoaded() {
        return ModList.get().isLoaded(PATCHOULI);
    }
}
