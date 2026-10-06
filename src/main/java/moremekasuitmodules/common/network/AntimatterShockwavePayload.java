package moremekasuitmodules.common.network;

import io.netty.buffer.ByteBuf;
import moremekasuitmodules.common.MoreMekaSuitModules;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

/** Client-side presentation of the antimatter detonation's pressure wave. */
public record AntimatterShockwavePayload(int duration, float intensity) implements CustomPacketPayload {
    public static final Type<AntimatterShockwavePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MoreMekaSuitModules.MODID, "antimatter_shockwave"));
    public static final StreamCodec<ByteBuf, AntimatterShockwavePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AntimatterShockwavePayload::duration,
            ByteBufCodecs.FLOAT, AntimatterShockwavePayload::intensity,
            AntimatterShockwavePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AntimatterShockwavePayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            context.enqueueWork(() -> moremekasuitmodules.client.ClientTickHandler.triggerCameraShake(payload.duration(), payload.intensity()));
        }
    }
}
