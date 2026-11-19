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

public final class StandArrowItem extends Item {
    public StandArrowItem(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            boolean alreadyOwned = serverPlayer.getPersistentData().getBoolean("SpinHasVerity");
            if (!alreadyOwned) {
                serverPlayer.getPersistentData().putBoolean("SpinHasVerity", true);
                StandNetwork.syncUnlocked(serverPlayer);
                level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                        SoundSource.PLAYERS, 0.65F, 1.35F);
                player.displayClientMessage(Component.translatable("message.spin.verity_awakened"), true);
                if (!player.getAbilities().instabuild) stack.shrink(1);
            } else {
                player.displayClientMessage(Component.translatable("message.spin.verity_already_owned"), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
