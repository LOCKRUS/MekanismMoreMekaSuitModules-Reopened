package moremekasuitmodules.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import moremekasuitmodules.common.MoreMekaSuitModules;
import moremekasuitmodules.common.content.gear.mekanism.mekatool.AntimatterExplosiveOrbEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;

public class AntimatterExplosiveOrbRenderer extends EntityRenderer<AntimatterExplosiveOrbEntity> {
    private static final ResourceLocation TEXTURE = MoreMekaSuitModules.rl("textures/entity/antimatter_explosive_orb.png");
    private final AntimatterExplosiveOrbModel model;

    public AntimatterExplosiveOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new AntimatterExplosiveOrbModel(context.bakeLayer(AntimatterExplosiveOrbModel.LAYER_LOCATION));
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(AntimatterExplosiveOrbEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float base = entity.isUltraMode() ? 2.5F : 1.0F;
        float pulse = base * (1.35F + Mth.sin((entity.tickCount + partialTick) * 0.55F) * 0.12F);
        poseStack.scale(pulse, pulse, pulse);
        this.model.setupAnim(entity, 0, 0, entity.tickCount + partialTick, 0, 0);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        this.model.renderToBuffer(poseStack, consumer, packedLight,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(AntimatterExplosiveOrbEntity entity) {
        return TEXTURE;
    }
}
