package com.hungteen.pvz.common.register;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class PVZItemTabs {

    public static final CreativeModeTab PVZ_BLOCKS = new CreativeModeTab("pvz_blocks") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(PVZBlocks.NUT.get(PVZBlocks.WoodSet.Plank).get().asItem());
        }
    };

    public static final CreativeModeTab PVZ_MISC = new CreativeModeTab("pvz_misc") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(PVZItems.NUT.get());
        }
    };
//    public static final CreativeModeTab PVZ_FUNCTIONAL = new CreativeModeTab("pvz_functional") {
//        @Override
//        public ItemStack makeIcon() {
//            return new ItemStack(PVZItems.NUT_BOAT.get());
//        }
//    };
}
