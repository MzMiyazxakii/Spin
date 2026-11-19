package com.mzmiyazxakii.spin.client;

import com.mzmiyazxakii.spin.SpinMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SkinSelectPacket(String skin) {
    public static void encode(SkinSelectPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.skin, 32);
    }

    public static SkinSelectPacket decode(FriendlyByteBuf buffer) {
        return new SkinSelectPacket(buffer.readUtf(32));
    }

    public static void handle(SkinSelectPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            var player = context.getSender();
            if (player == null) return;
            if (!packet.skin.equals("standard") && !packet.skin.equals("wrecking_ball")) return;
            for (ItemStack stack : player.getInventory().items) {
                if (stack.is(SpinMod.STEEL_BALL.get())) {
                    stack.getOrCreateTag().putString("SpinSkin", packet.skin);
                    break;
                }
            }
            for (ItemStack stack : player.getInventory().offhand) {
                if (stack.is(SpinMod.STEEL_BALL.get())) {
                    stack.getOrCreateTag().putString("SpinSkin", packet.skin);
                    break;
                }
            }
            player.getInventory().setChanged();
        });
        context.setPacketHandled(true);
    }
}
