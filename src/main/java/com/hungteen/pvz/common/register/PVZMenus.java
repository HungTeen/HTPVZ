package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.client.gui.screens.AlmanacScreen;
import com.hungteen.pvz.client.gui.screens.EssenceAltarScreen;
import com.hungteen.pvz.client.gui.screens.EssenceFurnaceScreen;
import com.hungteen.pvz.client.gui.screens.PennyScreen;
import com.hungteen.pvz.common.menu.AlmanacMenu;
import com.hungteen.pvz.common.menu.EssenceAltarMenu;
import com.hungteen.pvz.common.menu.EssenceFurnaceMenu;
import com.hungteen.pvz.common.menu.PennyMenu;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PVZMenus {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, PVZMod.MODID);

    public static final RegistryObject<MenuType<EssenceAltarMenu>> ESSENCE_ALTAR = MENU_TYPES.register("essence_altar",
            () -> IForgeMenuType.create((id, inv, data) -> new EssenceAltarMenu(inv, id)));
    public static final RegistryObject<MenuType<EssenceFurnaceMenu>> ESSENCE_FURNACE = MENU_TYPES.register("essence_furnace",
            () -> IForgeMenuType.create((id, inv, data) -> new EssenceFurnaceMenu(inv, id, data.readBlockPos())));
    public static final RegistryObject<MenuType<AlmanacMenu>> ALMANAC = MENU_TYPES.register("almanac",
            () -> IForgeMenuType.create((id, inv, data) -> new AlmanacMenu(inv, id)));
    public static final RegistryObject<MenuType<PennyMenu>> PENNY = MENU_TYPES.register("penny",
            () -> IForgeMenuType.create((id, inv, data) -> new PennyMenu(inv, id)));


    @OnlyIn(Dist.CLIENT)
    public static void registerScreens() {
        MenuScreens.register(ESSENCE_ALTAR.get(), EssenceAltarScreen::new);
        MenuScreens.register(ESSENCE_FURNACE.get(), EssenceFurnaceScreen::new);
        MenuScreens.register(ALMANAC.get(), AlmanacScreen::new);
        MenuScreens.register(PENNY.get(), PennyScreen::new);
    }
}
