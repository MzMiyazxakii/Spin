package com.mzmiyazxakii.spin.client;

import com.mzmiyazxakii.spin.SpinMod;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = SpinMod.MOD_ID, value = Dist.CLIENT)
public final class VerityKeyHandler {
    public static final KeyMapping TOGGLE_VERITY =
            new KeyMapping("key.spin.toggle_verity", GLFW.GLFW_KEY_U, "key.categories.spin");

    private VerityKeyHandler() {}

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        while (TOGGLE_VERITY.consumeClick()) {
            if (minecraft.player != null && minecraft.screen == null) {
                VerityClientState.toggleVisible();
            }
        }
    }
}
