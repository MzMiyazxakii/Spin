package com.mzmiyazxakii.spin.client;

import com.mzmiyazxakii.spin.SpinMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = SpinMod.MOD_ID, value = Dist.CLIENT)
public final class SkinKeyEvents {
    public static final KeyMapping OPEN_SKINS = new KeyMapping(
            "key.spin.open_skins", GLFW.GLFW_KEY_P, "key.categories.gameplay");

    private SkinKeyEvents() {}

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (OPEN_SKINS.consumeClick() && minecraft.player != null && minecraft.screen == null) {
            var player = minecraft.player;
            var main = player.getMainHandItem();
            var off = player.getOffhandItem();
            if (main.is(SpinMod.STEEL_BALL.get())) {
                minecraft.setScreen(new SkinSelectScreen(main));
            } else if (off.is(SpinMod.STEEL_BALL.get())) {
                minecraft.setScreen(new SkinSelectScreen(off));
            }
        }
    }
}
