package com.hungteen.pvz.client.gui.components;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class SunImageToolTipComponent implements TooltipComponent {
    public int cost;
    public int cd;
    public boolean isCostSun;
    public boolean isAddition;
    public boolean hasCd;
    public SunImageToolTipComponent(int cost, int cd, boolean isCostSun, boolean isAddition, boolean hasCd) {
        this.cost = cost;
        this.cd = cd;
        this.isCostSun = isCostSun;
        this.isAddition = isAddition;
        this.hasCd = hasCd;
    }
}
