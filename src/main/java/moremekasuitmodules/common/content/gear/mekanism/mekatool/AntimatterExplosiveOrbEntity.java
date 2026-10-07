package moremekasuitmodules.common.content.gear.mekanism.mekatool;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.block.Blocks;
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
    private static final EntityDataAccessor<Boolean> ULTRA_MODE = SynchedEntityData.defineId(
            AntimatterExplosiveOrbEntity.class, EntityDataSerializers.BOOLEAN);

    public AntimatterExplosiveOrbEntity(EntityType<AntimatterExplosiveOrbEntity> type, Level level) {
        super(type, level);
    }

    public AntimatterExplosiveOrbEntity(EntityType<? extends AntimatterExplosiveOrbEntity> type, LivingEntity owner, Vec3 direction, Level level) {
        this(type, owner, direction, level, false);
    }

    public AntimatterExplosiveOrbEntity(EntityType<? extends AntimatterExplosiveOrbEntity> type, LivingEntity owner,
                                        Vec3 direction, Level level, boolean ultra) {
        super(type, owner, direction, level);
        setUltraMode(ultra);
        setDeltaMovement(direction.normalize().scale(SPEED));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ULTRA_MODE, false);
    }

    public boolean isUltraMode() {
        return entityData.get(ULTRA_MODE);
    }

    private void setUltraMode(boolean ultra) {
        entityData.set(ULTRA_MODE, ultra);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("UltraMode", isUltraMode());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setUltraMode(tag.getBoolean("UltraMode"));
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
            detonate(isUltraMode());
        }
    }

    private void detonate(boolean ultra) {
        boolean canGrief = EventHooks.canEntityGrief(level(), this);
        Vec3 center = position();

        if (ultra) {
            // Keep vanilla explosion visuals, while the custom pass below creates the exact
            // 100 x 100 hemispherical crater and can spare bedrock deterministically.
            level().explode(this, center.x, center.y, center.z, 8.0F, true, Level.ExplosionInteraction.NONE);
            createUltraCrater(center, canGrief);
        } else {
            // Standard mode preserves the existing nuclear-scale blast.
            level().explode(this, center.x, center.y, center.z, EXPLOSION_POWER, canGrief, Level.ExplosionInteraction.BLOCK);
            createSecondaryBlastCores(center, canGrief);
        }
        double damageRadius = ultra ? 75.0D : DAMAGE_RADIUS;
        float maxDamage = ultra ? ENTITY_DAMAGE * 2.0F : ENTITY_DAMAGE;
        DamageSource source = level().damageSources().explosion(this, getOwner());
        for (Entity entity : level().getEntities(this, getBoundingBox().inflate(damageRadius),
                entity -> entity instanceof LivingEntity && entity != getOwner())) {
            double distance = Math.max(1.0D, distanceTo(entity));
            float damage = (float) (maxDamage * Math.max(0.15D, 1.0D - distance / damageRadius));
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
            if (ultra) {
                // The Ultra pressure shell matches the 100 x 100 crater footprint.
                sendShockwaveRing(serverLevel, center, 32.0D, ANTIMATTER_PURPLE, 512);
                sendShockwaveRing(serverLevel, center, 50.0D, ANTIMATTER_PURPLE, 800);
                sendShockwaveSphere(serverLevel, center, 50.0D, ANTIMATTER_PURPLE, 1200);
            } else {
                sendShockwaveSphere(serverLevel, center, 22.0D, ANTIMATTER_PURPLE, 420);
            }
            serverLevel.sendParticles(ParticleTypes.CLOUD, center.x, center.y + 1.0D, center.z, 260, 14, 3, 14, 0.35);
            moremekasuitmodules.common.network.AntimatterShockwaveNetwork.sendNear(
                    serverLevel, center, ultra ? 128.0D : 64.0D, ultra ? 300 : 200);
        }
        discard();
    }

    /**
     * Removes a radius-50 sphere (100x100 footprint) around the impact. The lower
     * half forms the crater, while the upper half clears structures above it. The
     * pass intentionally destroys without drops so an endgame shot cannot create
     * hundreds of thousands of item entities. Bedrock is the one absolute exception.
     */
    private void createUltraCrater(Vec3 center, boolean canGrief) {
        if (!(level() instanceof net.minecraft.server.level.ServerLevel)) {
            return;
        }
        final int radius = 50;
        final int centerX = (int) Math.floor(center.x);
        final int centerY = (int) Math.floor(center.y);
        final int centerZ = (int) Math.floor(center.z);
        if (canGrief) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int horizontalSquared = dx * dx + dz * dz;
                    if (horizontalSquared > radius * radius) {
                        continue;
                    }
                    int depth = (int) Math.floor(Math.sqrt(radius * radius - horizontalSquared));
                    for (int dy = -depth; dy <= depth; dy++) {
                        BlockPos pos = new BlockPos(centerX + dx, centerY + dy, centerZ + dz);
                        if (level().getBlockState(pos).is(Blocks.BEDROCK)
                                || level().getBlockState(pos).isAir()) {
                            continue;
                        }
                        level().destroyBlock(pos, false, this);
                    }
                }
            }
        }
        // Fire is a visual aftermath and must not disappear merely because the
        // world has mobGriefing disabled; block destruction remains gated above.
        createUltraFire(centerX, centerY, centerZ);
    }

    /** Adds the burning surface left by the Ultra detonation, like the standard blast. */
    private void createUltraFire(int centerX, int centerY, int centerZ) {
        final int radius = 52;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius || level().random.nextInt(4) != 0) {
                    continue;
                }
                // Search from above the impact down to the crater floor. This leaves
                // fire on the exposed rim even when the whole central column was removed.
                for (int dy = 60; dy >= -80; dy--) {
                    BlockPos solid = new BlockPos(centerX + dx, centerY + dy, centerZ + dz);
                    BlockPos above = solid.above();
                    if (!level().getBlockState(solid).isAir() && level().isEmptyBlock(above)
                            && !level().getBlockState(solid).is(Blocks.BEDROCK)) {
                        level().setBlock(above, Blocks.FIRE.defaultBlockState(), 3);
                        break;
                    }
                }
            }
        }
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
