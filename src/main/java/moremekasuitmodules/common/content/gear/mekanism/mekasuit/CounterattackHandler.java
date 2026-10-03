package moremekasuitmodules.common.content.gear.mekanism.mekasuit;

import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.common.content.gear.IModuleContainerItem;
import moremekasuitmodules.common.registries.MekaSuitMoreModules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public final class CounterattackHandler {
    private static final ThreadLocal<Boolean> APPLYING_COUNTERATTACK = ThreadLocal.withInitial(() -> false);

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (APPLYING_COUNTERATTACK.get() || event.getEntity().level().isClientSide() || event.getAmount() <= 0.0F) {
            return;
        }
        DamageSource source = event.getSource();
        if (source == null || isCounterattackDamage(source)) {
            return;
        }
        Entity attackerEntity = source.getEntity();
        if (!(attackerEntity instanceof LivingEntity attacker) || attacker == event.getEntity() || !attacker.isAlive()) {
            return;
        }

        LivingEntity defender = event.getEntity();
        int counterModules = getEffectiveCounterModules(defender);
        if (counterModules <= 0) {
            return;
        }
        float counterDamage = event.getAmount() * counterModules / ModuleCounterattackUnit.DAMAGE_DIVISOR;
        if (counterDamage <= 0.0F) {
            return;
        }

        APPLYING_COUNTERATTACK.set(true);
        try {
            attacker.hurt(defender.damageSources().thorns(defender), counterDamage);
        } finally {
            APPLYING_COUNTERATTACK.set(false);
        }
    }

    private static boolean isCounterattackDamage(DamageSource source) {
        // The thorns source is used for the reflected hit; this also avoids reacting to vanilla thorns.
        return "thorns".equals(source.type().msgId());
    }

    public static int getEffectiveCounterModules(LivingEntity wearer) {
        int total = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = wearer.getItemBySlot(slot);
            if (!(stack.getItem() instanceof IModuleContainerItem)) {
                continue;
            }
            IModule<ModuleCounterattackUnit> module = IModuleHelper.INSTANCE.getIfEnabled(stack, MekaSuitMoreModules.COUNTERATTACK_UNIT);
            if (module != null) {
                total += module.getCustomInstance().getEffectiveCount(module.getInstalledCount());
            }
        }
        return total;
    }
}
