package com.mzmiyazxakii.spin;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

public class SteelBallItem extends Item {
    public static final int FULL_CHARGE_TICKS = 40;

    public static float getChargeProgress(int ticksUsingItem) {
        return Math.min(1.0F, Math.max(0.0F, ticksUsingItem / (float) FULL_CHARGE_TICKS));
    }

    public SteelBallItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.spin.steel_ball"));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        if (!level.isClientSide) {
            level.playSound(null, player.blockPosition(), SpinMod.CHARGE.get(),
                    SoundSource.PLAYERS, 0.45F, 1.0F);
        }
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int timeLeft) {
        if (!(living instanceof Player player) || level.isClientSide) return;

        int used = getUseDuration(stack) - timeLeft;
        float power = getChargeProgress(used);

        SteelBallProjectile ball = new SteelBallProjectile(level, player);
        ball.setItem(new ItemStack(this));
        // A full two-second charge gives the ball more damage and a noticeably longer flight.
        ball.setDamage(3.0F + power * 5.0F);
        float launchSpeed = 1.25F + power * 1.25F;
        ball.shootFromRotation(player, player.getXRot(), player.getYRot(),
                0.0F, launchSpeed, 0.0F);
        level.addFreshEntity(ball);

        level.playSound(null, player.blockPosition(), SpinMod.THROW.get(),
                SoundSource.PLAYERS, 0.8F, 1.0F);

        // Consume the thrown ball in every game mode, including Creative.
        stack.shrink(1);
        player.getInventory().setChanged();
    }
}
