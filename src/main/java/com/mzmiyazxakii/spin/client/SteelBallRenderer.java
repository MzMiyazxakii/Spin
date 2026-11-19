package com.mzmiyazxakii.spin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mzmiyazxakii.spin.SteelBallProjectile;
import com.mzmiyazxakii.spin.SpinSkinManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

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
            // Apply the local roll first, then pitch and yaw to align the ball with its flight vector.
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            poseStack.mulPose(Axis.ZP.rotationDegrees(roll));

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

        renderOrbitingMotes(entity, partialTick, poseStack, buffer);
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    /**
     * Draw the green motes as part of the projectile render instead of long-lived
     * world particles. This keeps them attached to the ball and prevents a trail.
     */
    private static void renderOrbitingMotes(SteelBallProjectile entity, float partialTick,
                                            PoseStack poseStack, MultiBufferSource buffer) {
        Vec3 velocity = entity.getDeltaMovement();
        if (velocity.lengthSqr() < 1.0E-5D) return;

        Vec3 forward = velocity.normalize();
        Vec3 reference = Math.abs(forward.y) > 0.92D
                ? new Vec3(1.0D, 0.0D, 0.0D)
                : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = forward.cross(reference).normalize();
        Vec3 up = right.cross(forward).normalize();

        float phase = (entity.tickCount + partialTick) * 0.42F;
        int skin = SpinSkinManager.get(entity.getItem());
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f pose = poseStack.last().pose();

        for (int i = 0; i < 4; i++) {
            float angle = phase + i * ((float) Math.PI * 0.5F);
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            Vec3 offset = right.scale(cos * 0.29D).add(up.scale(sin * 0.29D));
            drawGreenMote(consumer, pose, (float) offset.x, (float) offset.y,
                    (float) offset.z, 0.022F, skin);
        }
    }

    private static void drawGreenMote(VertexConsumer consumer, Matrix4f pose,
                                      float x, float y, float z, float halfSize, int skin) {
        float x0 = x - halfSize, x1 = x + halfSize;
        float y0 = y - halfSize, y1 = y + halfSize;
        float z0 = z - halfSize, z1 = z + halfSize;

        // Six tiny faces make each mote visible from every viewing angle.
        moteQuad(consumer, pose, x0,y0,z1, x1,y0,z1, x1,y1,z1, x0,y1,z1, skin);
        moteQuad(consumer, pose, x1,y0,z0, x0,y0,z0, x0,y1,z0, x1,y1,z0, skin);
        moteQuad(consumer, pose, x0,y0,z0, x0,y0,z1, x0,y1,z1, x0,y1,z0, skin);
        moteQuad(consumer, pose, x1,y0,z1, x1,y0,z0, x1,y1,z0, x1,y1,z1, skin);
        moteQuad(consumer, pose, x0,y1,z1, x1,y1,z1, x1,y1,z0, x0,y1,z0, skin);
        moteQuad(consumer, pose, x0,y0,z0, x1,y0,z0, x1,y0,z1, x0,y0,z1, skin);
    }

    private static void moteQuad(VertexConsumer consumer, Matrix4f pose,
                                 float x0, float y0, float z0,
                                 float x1, float y1, float z1,
                                 float x2, float y2, float z2,
                                 float x3, float y3, float z3, int skin) {
        moteVertex(consumer, pose, x0, y0, z0, skin);
        moteVertex(consumer, pose, x1, y1, z1, skin);
        moteVertex(consumer, pose, x2, y2, z2, skin);
        moteVertex(consumer, pose, x3, y3, z3, skin);
    }

    private static void moteVertex(VertexConsumer consumer, Matrix4f pose,
                                   float x, float y, float z, int skin) {
        if (skin == SpinSkinManager.WRECKING_BALL) consumer.vertex(pose, x, y, z).color(255, 128, 24, 235).endVertex();
        else consumer.vertex(pose, x, y, z).color(45, 255, 75, 230).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(SteelBallProjectile entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
