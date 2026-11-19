package com.mzmiyazxakii.spin.client;

import com.mzmiyazxakii.spin.SpinMod;
import com.mzmiyazxakii.spin.network.SpinNetwork;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = SpinMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public class SpinClientEvents {
    private static float charge = 0;
    private static boolean wasCharging = false;
    private static final KeyMapping CHARGE = new KeyMapping("key.spin.charge", GLFW.GLFW_KEY_V, "key.categories.spin");
    private static final KeyMapping PUNCH = new KeyMapping("key.spin.punch", GLFW.GLFW_KEY_C, "key.categories.spin");
    private static final KeyMapping FAST = new KeyMapping("key.spin.fast_ball", GLFW.GLFW_KEY_R, "key.categories.spin");
    private static final KeyMapping CURVE = new KeyMapping("key.spin.curveball", GLFW.GLFW_KEY_F, "key.categories.spin");
    private static final KeyMapping BOUNCY = new KeyMapping("key.spin.bouncy_ball", GLFW.GLFW_KEY_B, "key.categories.spin");
    private static final KeyMapping DRILL = new KeyMapping("key.spin.drill_throw", GLFW.GLFW_KEY_G, "key.categories.spin");
    public static void setCharge(float value) { charge = value; }

    @SubscribeEvent public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(CHARGE); event.register(PUNCH); event.register(FAST); event.register(CURVE); event.register(BOUNCY); event.register(DRILL);
    }
    @SubscribeEvent public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SpinMod.STEEL_BALL_PROJECTILE.get(), ThrownItemRenderer::new);
    }
    @SubscribeEvent public static void registerSpinHud(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "spin_charge", (gui, graphics, partialTick, screenWidth, screenHeight) -> {
            Minecraft mc = Minecraft.getInstance(); if (mc.player == null || mc.options.hideGui) return;
            int x = 10, y = screenHeight - 34;
            graphics.fill(x, y, x + 104, y + 18, 0xAA101018);
            graphics.fill(x + 2, y + 11, x + 102, y + 16, 0xFF292934);
            int width = Math.max(0, Math.min(100, Math.round(charge)));
            if (width > 0) graphics.fill(x + 2, y + 11, x + 2 + width, y + 16, 0xFFFFC94A);
            graphics.drawString(mc.font, Component.literal("SPIN  " + Math.round(charge) + "%"), x + 5, y + 2, 0xFFFFE6A0, false);
        });
    }
    @Mod.EventBusSubscriber(modid = SpinMod.MOD_ID, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
    public static class ClientForgeEvents {
        @SubscribeEvent public static void clientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END || Minecraft.getInstance().player == null) return;
            boolean charging = CHARGE.isDown();
            if (charging && !wasCharging) SpinNetwork.sendAction(0);
            if (!charging && wasCharging) SpinNetwork.sendAction(1);
            wasCharging = charging;
            while (PUNCH.consumeClick()) SpinNetwork.sendAction(2);
            while (FAST.consumeClick()) SpinNetwork.sendAction(3);
            while (CURVE.consumeClick()) SpinNetwork.sendAction(4);
            while (BOUNCY.consumeClick()) SpinNetwork.sendAction(5);
            while (DRILL.consumeClick()) SpinNetwork.sendAction(6);
        }
    }
}
