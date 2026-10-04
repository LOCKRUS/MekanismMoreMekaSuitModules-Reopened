package moremekasuitmodules.common.content.gear.mekanism.mekatool;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import org.joml.Vector3f;

public class AntimatterExplosiveOrbEntity extends AbstractHurtingProjectile {
    public static final float SPEED = 2.75F;
    public static final float EXPLOSION_POWER = 32.0F;
    private static final double DAMAGE_RADIUS = 30.0D;
    private static final float ENTITY_DAMAGE = 160.0F;
    private static final DustParticleOptions ANTIMATTER_PURPLE =
            new DustParticleOptions(new Vector3f(0.48F, 0.04F, 0.95F), 2.8F);
    private static final DustParticleOptions ANTIMATTER_WHITE =
            new DustParticleOptions(new Vector3f(0.92F, 0.82F, 1.0F), 2.0F);

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
            // Inspired by HBM NTM's staged nuclear visual language: flash, core, rings, plume.
            serverLevel.playSound(null, center.x, center.y, center.z,
                    SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 16.0F, 0.45F);
            serverLevel.playSound(null, center.x, center.y, center.z,
                    SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 8.0F, 0.55F);
            serverLevel.sendParticles(ParticleTypes.FLASH, center.x, center.y, center.z, 1, 0, 0, 0, 0);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y, center.z, 4, 1, 1, 1, 0);
            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, center.x, center.y, center.z, 4, 2, 2, 2, 0);
            serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, center.x, center.y, center.z, 420, 10, 8, 10, 0.35);
            serverLevel.sendParticles(ParticleTypes.END_ROD, center.x, center.y, center.z, 260, 12, 10, 12, 0.35);
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, 220, 10, 8, 10, 0.5);
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y, center.z, 180, 9, 8, 9, 0.25);
            sendShockwaveRing(serverLevel, center, 4.0D, ANTIMATTER_WHITE, 96);
            sendShockwaveRing(serverLevel, center, 10.0D, ANTIMATTER_PURPLE, 160);
            sendShockwaveRing(serverLevel, center, 18.0D, ANTIMATTER_PURPLE, 224);
            serverLevel.sendParticles(ParticleTypes.CLOUD, center.x, center.y + 1.0D, center.z, 160, 12, 2, 12, 0.25);
        }
        discard();
    }

    private static void sendShockwaveRing(net.minecraft.server.level.ServerLevel level, Vec3 center,
                                          double radius, DustParticleOptions particle, int points) {
        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2.0D * i) / points;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            level.sendParticles(particle, x, center.y + 0.35D, z, 1, 0, 0, 0, 0);
            level.sendParticles(particle, x, center.y + 2.0D, z, 1, 0, 0, 0, 0);
        }
    }
}
