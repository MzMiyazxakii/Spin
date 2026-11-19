package com.mzmiyazxakii.spin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mzmiyazxakii.spin.SteelBallProjectile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

/**
 * Renders the Steel Ball aligned with its flight direction and rolls it around
 * its local forward axis while airborne.
 */
public final class SteelBallRenderer extends EntityRenderer<SteelBallProjectile> {
    private final ItemRenderer itemRenderer;

    public SteelBallRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.12F;
    }

    @Override
    public void render(SteelBallProjectile entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (!entity.getItem().isEmpty()) {
            Vec3 velocity = entity.getDeltaMovement();
            double horizontalSpeed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
            float yaw = (float) (Math.atan2(velocity.x, velocity.z) * (180.0D / Math.PI));
            float pitch = (float) (Math.atan2(-velocity.y, horizontalSpeed) * (180.0D / Math.PI));
            float roll = (entity.tickCount + partialTick) * 28.0F;

            poseStack.pushPose();
            poseStack.scale(0.5F, 0.5F, 0.5F);
            // Roll around local Z first, then align that axis with the current velocity.
            poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));

            itemRenderer.renderStatic(
                    entity.getItem(),
                    ItemDisplayContext.GROUND,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer,
                    entity.level(),
                    entity.getId()
            );
            poseStack.popPose();
        }

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SteelBallProjectile entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
