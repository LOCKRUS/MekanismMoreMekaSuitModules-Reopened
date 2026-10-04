package moremekasuitmodules.common.content.gear.mekanism.mekasuit;

import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.common.content.gear.IModuleContainerItem;
import moremekasuitmodules.common.registries.MekaSuitMoreModules;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

import java.util.List;

public final class ImpactWaveHandler {
    private static final float ENERGY_PER_RADIUS = 1_500.0F;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()
                || player.isSpectator() || event.getDistance() <= 0.0F
                || player.isInWater() || player.isInLava() || player.isFallFlying()) {
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
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 2, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.CLOUD, x, y, z, 100, radius * 0.4D, 0.6D, radius * 0.4D, 0.2D);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 80, radius * 0.5D, 0.5D, radius * 0.5D, 0.25D);
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
