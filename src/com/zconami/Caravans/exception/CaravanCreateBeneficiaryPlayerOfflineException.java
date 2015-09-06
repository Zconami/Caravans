package com.zconami.Caravans.exception;

public class CaravanCreateBeneficiaryPlayerOfflineException extends RuntimeException {

    private static final long serialVersionUID = 3352853588441626964L;

    public CaravanCreateBeneficiaryPlayerOfflineException(String key) {
        super("Couldn't create caravan (" + key + ") because beneficiary is offline");
    }

}
