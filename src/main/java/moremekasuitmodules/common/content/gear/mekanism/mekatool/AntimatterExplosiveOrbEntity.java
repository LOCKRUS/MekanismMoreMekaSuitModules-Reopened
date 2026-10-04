package moremekasuitmodules.common.content.gear.mekanism.mekatool;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

public class AntimatterExplosiveOrbEntity extends AbstractHurtingProjectile {
    public static final float SPEED = 2.75F;
    public static final float EXPLOSION_POWER = 32.0F;
    private static final double DAMAGE_RADIUS = 24.0D;
    private static final float ENTITY_DAMAGE = 80.0F;

    public AntimatterExplosiveOrbEntity(EntityType<AntimatterExplosiveOrbEntity> type, Level level) {
        super(type, level);
    }

    public AntimatterExplosiveOrbEntity(EntityType<? extends AntimatterExplosiveOrbEntity> type, LivingEntity owner, Vec3 direction, Level level) {
        super(type, owner, direction, level);
        setDeltaMovement(direction.normalize().scale(SPEED));
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide()) {
            detonate();
        }
    }

    private void detonate() {
        boolean canGrief = EventHooks.canEntityGrief(level(), this);
        Vec3 center = position();

        // The vanilla blast supplies the block-breaking shockwave; the extra radial pulse
        // makes this behave like an antimatter detonation rather than an ordinary fireball.
        level().explode(this, center.x, center.y, center.z, EXPLOSION_POWER, canGrief, Level.ExplosionInteraction.BLOCK);
        DamageSource source = level().damageSources().explosion(this, getOwner());
        for (Entity entity : level().getEntities(this, getBoundingBox().inflate(DAMAGE_RADIUS),
                entity -> entity instanceof LivingEntity && entity != getOwner())) {
            double distance = Math.max(1.0D, distanceTo(entity));
            float damage = (float) (ENTITY_DAMAGE * Math.max(0.15D, 1.0D - distance / DAMAGE_RADIUS));
            entity.hurt(source, damage);
        }

        if (level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y, center.z, 1, 0, 0, 0, 0);
            serverLevel.sendParticles(ParticleTypes.FLASH, center.x, center.y, center.z, 8, 4, 4, 4, 0);
            serverLevel.sendParticles(ParticleTypes.END_ROD, center.x, center.y, center.z, 160, 8, 8, 8, 0.3);
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y, center.z, 100, 6, 6, 6, 0.15);
        }
        discard();
    }
}
