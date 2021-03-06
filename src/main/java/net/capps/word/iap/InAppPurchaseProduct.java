package net.capps.word.iap;

/**
 * Created by charlescapps on 5/17/15.
 */
public enum InAppPurchaseProduct {
    book_pack_1(100),
    book_pack_2(225),
    book_pack_3(500),
    infinite_books(0);

    private final int numTokens;

    private InAppPurchaseProduct(int numTokens) {
        this.numTokens = numTokens;
    }

    public int getNumTokens() {
        return numTokens;
    }
}
