package net.capps.word.iap;

/**
 * Created by charlescapps on 5/17/15.
 */
public enum InAppPurchaseProduct {
    BOOK_PACK_1(10),
    BOOK_PACK_2(25),
    BOOK_PACK_3(60);

    private final int numTokens;

    private InAppPurchaseProduct(int numTokens) {
        this.numTokens = numTokens;
    }

    public int getNumTokens() {
        return numTokens;
    }
}
