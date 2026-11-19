package com.mzmiyazxakii.spin.client;

public final class SkinSelection {
    private static String current = "standard";
    private SkinSelection() {}
    public static void setLocal(String skin) { current = skin; }
    public static String getLocal() { return current; }
}
