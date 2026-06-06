package com.hazebyte.crate.cratereloaded.util;


public class InventoryConstants {

    public static int ONE_ROW = 9;
    public static int THREE_ROWS = 27;
    public static int FIVE_ROWS = 54;
    public static int SIX_ROWS = 54;
    public static int MAX_INVENTORY_SIZE_WITH_NAV = FIVE_ROWS;

    public static int MAX_TITLE_LENGTH = 32;

    // Common inventory slot positions
    public static int CENTER_SLOT_THREE_ROWS = 13; // Center slot in 27-slot (3-row) inventory
    public static int SLOT_29 = 29; // Accept button position in confirmation menu
    public static int SLOT_33 = 33; // Decline button position in confirmation menu

    public static String getValidatedTitle(String title) {
        return title.substring(0, Math.min(title.length(), MAX_TITLE_LENGTH));
    }
}
