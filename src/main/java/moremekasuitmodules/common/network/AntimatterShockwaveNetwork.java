package moremekasuitmodules.common.network;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public final class AntimatterShockwaveNetwork {
    private AntimatterShockwaveNetwork() {
    }

    public static void sendNear(ServerLevel level, Vec3 center, double radius) {
        sendNear(level, center, radius, 200);
    }

    public static void sendNear(ServerLevel level, Vec3 center, double radius, int duration) {
        PacketDistributor.sendToPlayersNear(level, null, center.x, center.y, center.z, radius,
                new AntimatterShockwavePayload(duration, 0.8F));
    }
}
