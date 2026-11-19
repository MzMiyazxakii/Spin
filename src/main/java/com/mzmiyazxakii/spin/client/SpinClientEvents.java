package com.mzmiyazxakii.spin.client;

import com.mzmiyazxakii.spin.SpinMod;
import com.mzmiyazxakii.spin.SteelBallItem;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(
        modid = SpinMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class SpinClientEvents {
    private static final int CHARGE_SPIN_STEPS = 12;

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
                SpinMod.STEEL_BALL.get(),
                new ResourceLocation(SpinMod.MOD_ID, "charge_spin"),
                (stack, level, entity, seed) -> {
                    if (entity == null || !entity.isUsingItem()
                            || !entity.getUseItem().is(SpinMod.STEEL_BALL.get())) {
                        return 0.0F;
                    }
                    int step = (entity.getTicksUsingItem() / 2) % CHARGE_SPIN_STEPS;
                    return step / (float) CHARGE_SPIN_STEPS;
                }
        ));
    }

    @SubscribeEvent
    public static void registerSpinHud(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "spin_charge", (gui, graphics, partialTick, screenWidth, screenHeight) -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || minecraft.options.hideGui) return;

            var player = minecraft.player;
            if (!player.isUsingItem() || !player.getUseItem().is(SpinMod.STEEL_BALL.get())) return;

            float charge = SteelBallItem.getChargeProgress(player.getTicksUsingItem());
            int percent = Math.round(charge * 100.0F);
            int x = 10;
            int y = screenHeight - 34;

            graphics.fill(x, y, x + 104, y + 18, 0xAA101018);
            graphics.fill(x + 2, y + 11, x + 102, y + 16, 0xFF292934);
            int fillWidth = Math.round(charge * 100.0F);
            if (fillWidth > 0) {
                graphics.fill(x + 2, y + 11, x + 2 + fillWidth, y + 16, 0xFFFFC94A);
            }
            graphics.drawString(minecraft.font, Component.literal("SPIN  " + percent + "%"),
                    x + 5, y + 2, 0xFFFFE6A0, false);
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SpinMod.STEEL_BALL_PROJECTILE.get(), SteelBallRenderer::new);
    }
}
