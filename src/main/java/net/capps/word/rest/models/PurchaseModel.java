package net.capps.word.rest.models;

import net.capps.word.iap.InAppPurchaseProduct;

/**
 * Created by charlescapps on 5/17/15.
 */
public class PurchaseModel {
    private Boolean isGoogle;
    private String identifier;
    private String signature;
    private InAppPurchaseProduct product;

    public PurchaseModel() {
    }

    public Boolean getIsGoogle() {
        return isGoogle;
    }

    public void setIsGoogle(Boolean isGoogle) {
        this.isGoogle = isGoogle;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public InAppPurchaseProduct getProduct() {
        return product;
    }

    public void setProduct(InAppPurchaseProduct product) {
        this.product = product;
    }
}
