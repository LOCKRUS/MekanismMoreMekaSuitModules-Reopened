package moremekasuitmodules.common.content.gear.mekanism.mekasuit;

import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.common.content.gear.IModuleContainerItem;
import moremekasuitmodules.common.registries.MekaSuitMoreModules;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import org.joml.Vector3f;

import java.util.List;

public final class ImpactWaveHandler {
    private static final float ENERGY_PER_RADIUS = 1_500.0F;
    private static final DustParticleOptions SHOCKWAVE_PARTICLE =
            new DustParticleOptions(new Vector3f(0.20F, 0.85F, 1.0F), 1.5F);

    // Mekanism and flight-related modules may cancel fall damage before the
    // wave handler sees it. The original 1.12.2 implementation explicitly
    // listened to canceled events, which was missed in the NeoForge port.
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onLivingFall(LivingFallEvent event) {
        // Do not reject fall-flying here: custom flight modules may use the same
        // entity flag while the boots still need to process a landing.
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()
                || player.isSpectator() || event.getDistance() <= 0.0F
                || player.isInWater() || player.isInLava()) {
            return;
        }
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (!(boots.getItem() instanceof IModuleContainerItem)) {
            return;
        }
        IModule<ModuleImpactWaveUnit> module = IModuleHelper.INSTANCE.getIfEnabled(boots, MekaSuitMoreModules.IMPACT_WAVE_UNIT);
        if (module == null) {
            return;
        }
        ModuleImpactWaveUnit unit = module.getCustomInstance();
        if (event.getDistance() < unit.getTriggerHeight()) {
            return;
        }
        long energy = (long) (ENERGY_PER_RADIUS * unit.getRadius());
        if (!module.canUseEnergy(player, boots, energy, false)) {
            return;
        }
        module.useEnergy(player, boots, energy);
        event.setCanceled(true);
        createWave(player, unit, event.getDistance());
    }

    private static void createWave(Player player, ModuleImpactWaveUnit unit, float fallDistance) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        double radius = unit.getRadius();
        float baseDamage = Math.max(1.0F, (fallDistance - unit.getTriggerHeight()) * unit.getDamageMultiplier());
        double x = player.getX();
        double y = player.getY() + 0.1D;
        double z = player.getZ();
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 3, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.SONIC_BOOM, x, y, z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.CLOUD, x, y, z, 140, radius * 0.45D, 0.6D, radius * 0.45D, 0.25D);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 120, radius * 0.55D, 0.5D, radius * 0.55D, 0.3D);
        for (int i = 0; i < 96; i++) {
            double angle = Math.PI * 2.0D * i / 96.0D;
            double ringRadius = radius * (0.35D + 0.65D * i / 96.0D);
            level.sendParticles(SHOCKWAVE_PARTICLE, x + Math.cos(angle) * ringRadius, y + 0.15D,
                    z + Math.sin(angle) * ringRadius, 1, 0, 0, 0, 0);
        }
        AABB area = new AABB(x - radius, y - 2.0D, z - radius, x + radius, y + 3.0D, z + radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                target -> target != player && target.isAlive());
        for (LivingEntity target : targets) {
            double dx = target.getX() - x;
            double dz = target.getZ() - z;
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance > radius) {
                continue;
            }
            float falloff = 1.0F - (float) (distance / radius);
            float damage = Math.max(1.0F, baseDamage * (0.35F + 0.65F * falloff));
            target.hurt(player.damageSources().playerAttack(player), damage);
            if (distance > 0.001D) {
                target.push(dx / distance * (0.35D + 0.65D * falloff), 0.25D + 0.45D * falloff,
                        dz / distance * (0.35D + 0.65D * falloff));
            } else {
                target.push(0, 0.5D, 0);
            }
        }
    }
}
