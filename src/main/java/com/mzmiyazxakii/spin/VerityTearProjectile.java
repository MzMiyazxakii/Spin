package com.mzmiyazxakii.spin;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;

public class VerityTearProjectile extends ThrowableItemProjectile {
    private static final float DAMAGE = 2.0F;

    public VerityTearProjectile(EntityType<? extends VerityTearProjectile> type, Level level) {
        super(type, level);
    }

    public VerityTearProjectile(Level level, LivingEntity owner) {
        super(SpinMod.VERITY_TEAR_PROJECTILE.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.GHAST_TEAR;
    }

    @Override
    protected float getGravity() {
        return 0.0F;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount > 60) discard();
    }

    @Override
    protected void onHit(HitResult result) {
        if (level().isClientSide) return;

        if (result.getType() == HitResult.Type.ENTITY) {
            var target = ((EntityHitResult) result).getEntity();
            if (target instanceof LivingEntity living && target != getOwner()) {
                living.hurt(damageSources().thrown(this, getOwner()), DAMAGE);
            }
        }
        discard();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
