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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import org.joml.Vector3f;

public class AntimatterExplosiveOrbEntity extends AbstractHurtingProjectile {
    public static final float SPEED = 2.75F;
    public static final float EXPLOSION_POWER = 32.0F;
    private static final double DAMAGE_RADIUS = 40.0D;
    private static final float ENTITY_DAMAGE = 240.0F;
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
    public void tick() {
        clearFire();
        super.tick();
        clearFire();
        if (level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // Visible but restrained: four small purple motes per tick.
            serverLevel.sendParticles(ANTIMATTER_PURPLE, getX(), getY(), getZ(), 4,
                    0.08D, 0.08D, 0.08D, 0.015D);
        }
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
        createSecondaryBlastCores(center, canGrief);
        DamageSource source = level().damageSources().explosion(this, getOwner());
        for (Entity entity : level().getEntities(this, getBoundingBox().inflate(DAMAGE_RADIUS),
                entity -> entity instanceof LivingEntity && entity != getOwner())) {
            double distance = Math.max(1.0D, distanceTo(entity));
            float damage = (float) (ENTITY_DAMAGE * Math.max(0.15D, 1.0D - distance / DAMAGE_RADIUS));
            entity.hurt(source, damage);
            if (entity instanceof LivingEntity living) {
                // The pressure/sonic wave stuns nearby living targets for 10 seconds.
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 10));
                living.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200, 4));
                living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            }
        }

        if (level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // Inspired by HBM NTM's staged nuclear visual language: flash, core, rings, plume.
            serverLevel.playSound(null, center.x, center.y, center.z,
                    SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 16.0F, 0.45F);
            serverLevel.playSound(null, center.x, center.y, center.z,
                    SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 12.0F, 0.55F);
            serverLevel.playSound(null, center.x, center.y, center.z,
                    SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 8.0F, 0.55F);
            // Unlike a normal fireball, antimatter collapses inward first and then
            // throws out a purple-white spherical shell. The portal/smoke layers are
            // intentionally used in addition to vanilla explosion particles so the
            // detonation has a clearly different silhouette in-game.
            serverLevel.sendParticles(ParticleTypes.FLASH, center.x, center.y, center.z, 1, 0, 0, 0, 0);
            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, center.x, center.y, center.z, 8, 3, 3, 3, 0);
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z, 900, 13, 11, 13, 0.7);
            serverLevel.sendParticles(ParticleTypes.PORTAL, center.x, center.y, center.z, 700, 12, 10, 12, 1.0);
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y + 2.0D, center.z, 500, 13, 9, 13, 0.08);
            serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, center.x, center.y, center.z, 520, 11, 9, 11, 0.5);
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, 320, 12, 10, 12, 0.7);
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y, center.z, 260, 10, 10, 10, 0.35);
            sendShockwaveRing(serverLevel, center, 3.0D, ANTIMATTER_WHITE, 128);
            sendShockwaveRing(serverLevel, center, 8.0D, ANTIMATTER_PURPLE, 192);
            sendShockwaveRing(serverLevel, center, 16.0D, ANTIMATTER_PURPLE, 288);
            sendShockwaveSphere(serverLevel, center, 22.0D, ANTIMATTER_PURPLE, 420);
            serverLevel.sendParticles(ParticleTypes.CLOUD, center.x, center.y + 1.0D, center.z, 260, 14, 3, 14, 0.35);
            moremekasuitmodules.common.network.AntimatterShockwaveNetwork.sendNear(serverLevel, center, 64.0D);
        }
        discard();
    }

    /**
     * HBM-style staged detonation: the central antimatter collapse is followed by
     * several offset shock cores. This gives the blast a deep, irregular profile
     * instead of one ordinary spherical Minecraft explosion.
     */
    private void createSecondaryBlastCores(Vec3 center, boolean canGrief) {
        double ring = 7.0D;
        double[][] offsets = {
                {ring, 0.0D}, {-ring, 0.0D}, {0.0D, ring}, {0.0D, -ring},
                {ring * 0.7D, ring * 0.7D}, {-ring * 0.7D, ring * 0.7D},
                {ring * 0.7D, -ring * 0.7D}, {-ring * 0.7D, -ring * 0.7D}
        };
        for (double[] offset : offsets) {
            level().explode(this, center.x + offset[0], center.y + 1.0D, center.z + offset[1],
                    8.0F, canGrief, Level.ExplosionInteraction.BLOCK);
        }
        level().explode(this, center.x, center.y + 9.0D, center.z, 7.0F,
                canGrief, Level.ExplosionInteraction.BLOCK);
        level().explode(this, center.x, center.y - 6.0D, center.z, 7.0F,
                canGrief, Level.ExplosionInteraction.BLOCK);
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

    private static void sendShockwaveSphere(net.minecraft.server.level.ServerLevel level, Vec3 center,
                                            double radius, DustParticleOptions particle, int points) {
        for (int i = 0; i < points; i++) {
            double y = 1.0D - (2.0D * i / points);
            double ringRadius = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
            double angle = i * Math.PI * (3.0D - Math.sqrt(5.0D));
            level.sendParticles(particle,
                    center.x + Math.cos(angle) * ringRadius * radius,
                    center.y + y * radius,
                    center.z + Math.sin(angle) * ringRadius * radius,
                    1, 0, 0, 0, 0);
        }
    }
}
