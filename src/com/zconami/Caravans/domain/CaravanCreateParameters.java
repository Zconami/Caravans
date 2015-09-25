package com.zconami.Caravans.domain;

import org.bukkit.entity.Horse;

import net.minecraft.server.v1_8_R3.EntityHorse;

public class CaravanCreateParameters extends LinkedEntityCreateParameters<Horse, EntityHorse> {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private final Beneficiary beneficiary;

    private final Caravan.ProfitMultiplyerStrategy profitStrategy;

    private final Region origin;

    private final long investment;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public CaravanCreateParameters(Horse horse, Beneficiary beneficiary, Region origin,
            Caravan.ProfitMultiplyerStrategy profitStrategy, long investment) {
        super(horse);
        this.beneficiary = beneficiary;
        this.origin = origin;
        this.profitStrategy = profitStrategy;
        this.investment = investment;
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public Beneficiary getBeneficiary() {
        return beneficiary;
    }

    public Region getOrigin() {
        return origin;
    }

    public Caravan.ProfitMultiplyerStrategy getProfitStrategy() {
        return profitStrategy;
    }

    public long getInvestment() {
        return investment;
    }

}
