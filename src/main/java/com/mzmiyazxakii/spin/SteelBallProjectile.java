package com.mzmiyazxakii.spin;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;

public class SteelBallProjectile extends ThrowableItemProjectile {
    private static final DustParticleOptions SPIN_DUST =
            new DustParticleOptions(new Vector3f(0.08F, 0.95F, 0.16F), 0.78F);

    private float damage = 3.0F;
    private boolean returning = false;
    private boolean itemReturnedToOwner = false;
    private int returnTicks = 0;

    public SteelBallProjectile(EntityType<? extends SteelBallProjectile> type, Level level) {
        super(type, level);
    }

    public SteelBallProjectile(Level level, LivingEntity owner) {
        super(SpinMod.STEEL_BALL_PROJECTILE.get(), owner, level);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    protected Item getDefaultItem() {
        return SpinMod.STEEL_BALL.get();
    }

    @Override
    protected float getGravity() {
        return 0.018F;
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            spawnSpinParticles();
            return;
        }

        if (!returning && tickCount > 38) {
            beginReturning();
        }

        if (returning) {
            Entity owner = getOwner();
            if (owner == null || !owner.isAlive()) {
                discard();
                return;
            }

            returnTicks++;
            Vec3 target = owner.position().add(0, owner.getBbHeight() * 0.65, 0);
            Vec3 delta = target.subtract(position());

            if (delta.lengthSqr() < 1.25) {
                returnOneBallToOwner(owner);
                discard();
                return;
            }

            noPhysics = true;
            setDeltaMovement(delta.normalize().scale(Math.min(1.45, 0.45 + returnTicks * 0.035)));
            hasImpulse = true;
        }

        if (tickCount > 180) {
            discard();
        }
    }

    private void beginReturning() {
        if (returning) return;
        returning = true;
        noPhysics = true;
        hasImpulse = true;
    }

    /**
     * The consumed item is restored in one place only: once per projectile.
     */
    private void returnOneBallToOwner(Entity owner) {
        if (itemReturnedToOwner) return;
        itemReturnedToOwner = true;

        if (owner instanceof Player player) {
            ItemStack returned = new ItemStack(SpinMod.STEEL_BALL.get(), 1);
            if (!player.getInventory().add(returned)) {
                player.drop(returned, false);
            }
            player.getInventory().setChanged();
            level().playSound(null, player.blockPosition(), SpinMod.CATCH.get(),
                    SoundSource.PLAYERS, 0.7F, 1.0F);
        }
    }

    private void spawnSpinParticles() {
        Vec3 movement = getDeltaMovement();
        Vec3 forward = movement.lengthSqr() > 1.0E-4 ? movement.normalize() : new Vec3(0, 0, 1);
        Vec3 reference = Math.abs(forward.y) > 0.92 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);

        Vec3 right = forward.cross(reference).normalize();
        Vec3 up = right.cross(forward).normalize();
        double phase = tickCount * 0.42D;

        for (int i = 0; i < 3; i++) {
            double angle = phase + i * (Math.PI * 2.0D / 3.0D);
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            Vec3 radial = right.scale(cos).add(up.scale(sin));
            Vec3 offset = radial.scale(0.25D);
            Vec3 tangent = right.scale(-sin).add(up.scale(cos)).scale(0.025D);
            Vec3 particleVelocity = forward.scale(0.035D).add(tangent);

            level().addParticle(SPIN_DUST,
                    getX() + offset.x, getY() + offset.y, getZ() + offset.z,
                    particleVelocity.x, particleVelocity.y, particleVelocity.z);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (level().isClientSide || returning) return;

        if (result.getType() == HitResult.Type.ENTITY) {
            Entity target = ((EntityHitResult) result).getEntity();
            Entity owner = getOwner();
            if (target != owner && target instanceof LivingEntity living) {
                living.hurt(damageSources().thrown(this, owner), damage);
            }
            beginReturning();
        } else if (result.getType() == HitResult.Type.BLOCK) {
            beginReturning();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("SpinDamage", damage);
        tag.putBoolean("Returning", returning);
        tag.putBoolean("ItemReturnedToOwner", itemReturnedToOwner);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        damage = tag.getFloat("SpinDamage");
        returning = tag.getBoolean("Returning");
        itemReturnedToOwner = tag.getBoolean("ItemReturnedToOwner");
        if (returning) noPhysics = true;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
