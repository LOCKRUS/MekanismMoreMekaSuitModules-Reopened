package moremekasuitmodules.common.content.gear.mekanism.mekatool;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.event.EventHooks;

public class MekaToolBlasterFireball extends LargeFireball {
    private final int explosionPower;

    public MekaToolBlasterFireball(Level level, LivingEntity owner, net.minecraft.world.phys.Vec3 direction, int explosionPower) {
        super(level, owner, direction, explosionPower);
        this.explosionPower = explosionPower;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!level().isClientSide) {
            boolean canGrief = EventHooks.canEntityGrief(level(), this);
            level().explode(this, getX(), getY(), getZ(), explosionPower, canGrief, Level.ExplosionInteraction.MOB);
            discard();
        }
    }
}
