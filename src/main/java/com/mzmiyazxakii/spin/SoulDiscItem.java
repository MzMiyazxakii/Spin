package com.mzmiyazxakii.spin;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Removes Verity from the player who uses the disc on themselves.
 * The Stand Arrow can awaken Verity again afterwards.
 */
public final class SoulDiscItem extends Item {
    public SoulDiscItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (!serverPlayer.getPersistentData().getBoolean("SpinHasVerity")) {
                serverPlayer.displayClientMessage(
                        Component.translatable("message.spin.soul_disc_no_stand"), true);
                return InteractionResultHolder.fail(stack);
            }

            serverPlayer.getPersistentData().putBoolean("SpinHasVerity", false);
            StandNetwork.syncUnlocked(serverPlayer, false);

            if (!serverPlayer.getAbilities().instabuild) {
                stack.shrink(1);
            }

            level.playSound(null, serverPlayer.blockPosition(), SoundEvents.RESPAWN_ANCHOR_DEPLETE,
                    SoundSource.PLAYERS, 0.8F, 0.75F);
            serverPlayer.displayClientMessage(
                    Component.translatable("message.spin.soul_disc_used"), true);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
