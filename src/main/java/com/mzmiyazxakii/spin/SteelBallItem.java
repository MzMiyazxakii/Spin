package com.mzmiyazxakii.spin;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.UseAnim;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SteelBallItem extends Item {
    public SteelBallItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.spin.steel_ball"));
        tooltip.add(Component.translatable("tooltip.spin.steel_ball.spin"));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        if (!level.isClientSide) {
            level.playSound(null, player.blockPosition(), SpinMod.CHARGE.get(), SoundSource.PLAYERS, 0.45F, 1.0F);
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
        float power = Math.min(1.0F, used / 40.0F);
        boolean drill = player.isCrouching();

        SteelBallProjectile ball = new SteelBallProjectile(level, player);
        ball.setItem(new ItemStack(this));
        ball.setMode(drill ? SteelBallProjectile.Mode.DRILL : SteelBallProjectile.Mode.NORMAL);
        // Aim slightly above the crosshair to give the ball a visible, arcing flight.
        ball.shootFromRotation(player, player.getXRot() - 7.0F, player.getYRot(), 0.0F, 1.25F + power, 0.8F);
        ball.setDamage(3.0F + power * 4.0F);
        level.addFreshEntity(ball);
        level.playSound(null, player.blockPosition(), drill ? SpinMod.DRILL.get() : SpinMod.THROW.get(), SoundSource.PLAYERS, 0.8F, 1.0F);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }
}
