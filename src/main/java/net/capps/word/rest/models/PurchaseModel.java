package net.capps.word.rest.models;

import net.capps.word.iap.InAppPurchaseProduct;

/**
 * Created by charlescapps on 5/17/15.
 */
public class PurchaseModel {
    private String googleVerification;
    private String appleVerification;
    private InAppPurchaseProduct product;

    public PurchaseModel() {
    }

    public String getGoogleVerification() {
        return googleVerification;
    }

    public void setGoogleVerification(String googleVerification) {
        this.googleVerification = googleVerification;
    }

    public String getAppleVerification() {
        return appleVerification;
    }

    public void setAppleVerification(String appleVerification) {
        this.appleVerification = appleVerification;
    }

    public InAppPurchaseProduct getProduct() {
        return product;
    }

    public void setProduct(InAppPurchaseProduct product) {
        this.product = product;
    }
}
