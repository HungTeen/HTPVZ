package com.hungteen.pvz.client.gui.components;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class SunImageToolTipComponent implements TooltipComponent {
    public int cost;
    public int cd;
    public boolean isCostSun;
    public boolean isAddition;
    public boolean hasCd;
    public boolean hasExtraCost;
    public SunImageToolTipComponent(int cost, int cd, boolean isCostSun, boolean isAddition, boolean hasCd, boolean hasExtraCost) {
        this.cost = cost;
        this.cd = cd;
        this.isCostSun = isCostSun;
        this.isAddition = isAddition;
        this.hasCd = hasCd;
        this.hasExtraCost = hasExtraCost;
    }
}
