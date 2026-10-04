package moremekasuitmodules.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import moremekasuitmodules.common.MoreMekaSuitModules;
import moremekasuitmodules.common.content.gear.mekanism.mekatool.AntimatterExplosiveOrbEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.PartPose;

public class AntimatterExplosiveOrbModel extends EntityModel<AntimatterExplosiveOrbEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            MoreMekaSuitModules.rl("antimatter_explosive_orb"), "main");
    private final ModelPart bone;

    public AntimatterExplosiveOrbModel(ModelPart root) {
        this.bone = root.getChild("bone");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition bone = root.addOrReplaceChild("bone",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-3.0F, -2.5F, -3.0F, 6.0F, 5.0F, 6.0F),
                PartPose.offset(0.0F, 21.5F, 0.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(AntimatterExplosiveOrbEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        bone.yRot = ageInTicks * 0.65F;
        bone.xRot = ageInTicks * 0.43F;
        bone.zRot = ageInTicks * 0.29F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
                               int packedOverlay, int color) {
        bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
