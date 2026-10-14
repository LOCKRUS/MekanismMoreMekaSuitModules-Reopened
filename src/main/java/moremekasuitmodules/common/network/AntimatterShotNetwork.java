package moremekasuitmodules.common.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class AntimatterShotNetwork {
    private AntimatterShotNetwork() {
    }

    public static void send(ServerPlayer player, boolean ultra) {
        PacketDistributor.sendToPlayer(player, new AntimatterShotPayload(ultra));
    }
}
