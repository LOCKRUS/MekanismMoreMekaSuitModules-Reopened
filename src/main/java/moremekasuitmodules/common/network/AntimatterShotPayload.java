package moremekasuitmodules.common.network;

import io.netty.buffer.ByteBuf;
import moremekasuitmodules.common.MoreMekaModulesReopen;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client-side presentation of an antimatter orb being fired. */
public record AntimatterShotPayload(boolean ultra) implements CustomPacketPayload {
    public static final Type<AntimatterShotPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MoreMekaModulesReopen.MODID, "antimatter_shot"));
    public static final StreamCodec<ByteBuf, AntimatterShotPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, AntimatterShotPayload::ultra,
                    AntimatterShotPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AntimatterShotPayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            context.enqueueWork(() -> moremekasuitmodules.client.ClientTickHandler.triggerOrbShot(payload.ultra()));
        }
    }
}
