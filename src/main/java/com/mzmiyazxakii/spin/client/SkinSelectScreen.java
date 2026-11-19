package com.mzmiyazxakii.spin.client;

import com.mzmiyazxakii.spin.SpinMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class SkinSelectScreen extends Screen {
    private final ItemStack selectedStack;

    public SkinSelectScreen(ItemStack stack) {
        super(Component.translatable("screen.spin.skins.title"));
        this.selectedStack = stack;
    }

    @Override
    protected void init() {
        int center = width / 2;
        int top = height / 2 - 34;
        addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                Component.translatable("screen.spin.skins.standard"), button -> choose("standard"))
                .bounds(center - 105, top, 210, 20).build());
        addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                Component.translatable("screen.spin.skins.wrecking_ball"), button -> choose("wrecking_ball"))
                .bounds(center - 105, top + 25, 210, 20).build());
    }

    private void choose(String skin) {
        if (minecraft == null || minecraft.player == null) return;
        SkinSelection.setLocal(skin);
        SpinMod.CHANNEL.sendToServer(new SkinSelectPacket(skin));
        selectedStack.getOrCreateTag().putString("SpinSkin", skin);
        minecraft.player.getInventory().setChanged();
        onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 58, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
