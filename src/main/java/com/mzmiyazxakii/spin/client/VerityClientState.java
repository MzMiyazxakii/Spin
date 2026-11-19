package com.mzmiyazxakii.spin.client;

public final class VerityClientState {
    private static boolean unlocked;
    private static boolean visible;

    private VerityClientState() {}

    public static boolean isUnlocked() { return unlocked; }
    public static boolean isVisible() { return visible && unlocked; }
    public static void setUnlocked(boolean value) {
        unlocked = value;
        if (!value) visible = false;
    }
    public static void toggleVisible() {
        if (unlocked) visible = !visible;
    }
}
