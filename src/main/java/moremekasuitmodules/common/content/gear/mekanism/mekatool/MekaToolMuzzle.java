package moremekasuitmodules.common.content.gear.mekanism.mekatool;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Shared muzzle geometry for MekaTool projectiles and client-side muzzle effects. */
public final class MekaToolMuzzle {
    private static final double FORWARD_OFFSET = 0.78D;
    private static final double RIGHT_OFFSET = 0.48D;
    // The muzzle sits below the player's eye line on the visible MekaTool body.
    // Keep this shared by projectiles, flame particles, and the charging orb.
    private static final double DOWN_OFFSET = 0.52D;

    private MekaToolMuzzle() {
    }

    public static Vec3 origin(Player player, Vec3 direction) {
        Vec3 forward = direction.normalize();
        Vec3 right = forward.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (right.lengthSqr() < 1.0E-6D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }
        Vec3 up = right.cross(forward).normalize();
        return player.getEyePosition()
                .add(forward.scale(FORWARD_OFFSET))
                .add(right.scale(RIGHT_OFFSET))
                .subtract(up.scale(DOWN_OFFSET));
    }

    public static Vec3 origin(Player player) {
        return origin(player, player.getViewVector(1.0F));
    }
}
