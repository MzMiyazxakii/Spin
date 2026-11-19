package com.mzmiyazxakii.spin.client;

public final class VerityClientState {
    private static final int SUMMON_ANIMATION_TICKS = 18;
    private static boolean unlocked;
    private static boolean visible;
    private static int summonTicks = SUMMON_ANIMATION_TICKS;

    private VerityClientState() {}

    public static boolean isUnlocked() { return unlocked; }
    public static boolean isVisible() { return visible && unlocked; }

    public static void setUnlocked(boolean value) {
        unlocked = value;
        if (!value) {
            visible = false;
            summonTicks = SUMMON_ANIMATION_TICKS;
        }
    }

    public static void toggleVisible() {
        if (!unlocked) return;
        visible = !visible;
        if (visible) summonTicks = 0;
    }

    public static void clientTick() {
        if (isVisible() && summonTicks < SUMMON_ANIMATION_TICKS) {
            summonTicks++;
        }
    }

    public static float getSummonProgress(float partialTick) {
        if (!isVisible()) return 0.0F;
        float progress = Math.min(1.0F, (summonTicks + partialTick) / (float) SUMMON_ANIMATION_TICKS);
        // Smoothstep: the stand starts and stops gently instead of snapping into place.
        return progress * progress * (3.0F - 2.0F * progress);
    }
}
