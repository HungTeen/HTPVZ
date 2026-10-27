package com.hungteen.pvz.api.interfaces;

/**
 * Implemented by sun containers that can receive stored sun from a Sun Collector.
 * @author PangTeen
 * @program HTPVZ
 * @create 2026/10/26 23:00
 **/
public interface ICanChargeSun extends ISunContainer {

    default boolean canChargeSun() {
        return this.getAmount() < this.getCapacity();
    }

    /**
     * @return the amount of sun actually accepted
     */
    default int chargeSun(int amount) {
        if (amount <= 0 || !this.canChargeSun()) {
            return 0;
        }
        int accepted = Math.min(amount, this.getCapacity() - this.getAmount());
        this.setAmount(this.getAmount() + accepted);
        return accepted;
    }
}
