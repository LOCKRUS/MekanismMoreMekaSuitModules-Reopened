package moremekasuitmodules.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import moremekasuitmodules.client.ClientTickHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/** Renders the real antimatter orb model at the MekaTool muzzle while charging. */
public final class AntimatterChargeRenderer {
    private static AntimatterExplosiveOrbModel model;

    private AntimatterChargeRenderer() {
    }

    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        float progress = ClientTickHandler.antimatterChargeProgress(minecraft.player.getMainHandItem());
        if (progress <= 0.0F) {
            return;
        }
        if (model == null) {
            ModelPart root = minecraft.getEntityModels().bakeLayer(AntimatterExplosiveOrbModel.LAYER_LOCATION);
            model = new AntimatterExplosiveOrbModel(root);
        }

        Vec3 origin = ClientTickHandler.mekaToolChargeOrigin(minecraft.player);
        Vec3 camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(origin.x - camera.x, origin.y - camera.y, origin.z - camera.z);

        // Keep the charge model readable without letting it fill the first-person view.
        float orbScale = Mth.lerp(progress, 0.18F, 0.48F);
        poseStack.scale(orbScale, orbScale, orbScale);
        // The baked entity model keeps its bone at y=21.5 model units for the
        // normal entity renderer. Cancel that pivot here so the cube's center,
        // rather than the bone origin, sits exactly at the MekaTool muzzle.
        poseStack.translate(0.0D, -21.5D / 16.0D, 0.0D);
        model.setupChargeAnim(minecraft.level.getGameTime()
                + event.getPartialTick().getGameTimeDeltaPartialTick(false));
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucent(AntimatterExplosiveOrbRenderer.TEXTURE));
        model.renderToBuffer(poseStack, consumer, LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        buffers.endBatch(RenderType.entityTranslucent(AntimatterExplosiveOrbRenderer.TEXTURE));
        poseStack.popPose();
    }
}
