package moremekasuitmodules.common.content.gear.mekanism.mekatool;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.common.item.gear.ItemMekaTool;
import mekanism.common.util.StorageUtils;
import moremekasuitmodules.common.registries.MekaSuitMoreModules;
import moremekasuitmodules.common.registries.MoreMekaSuitModulesEntities;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

public final class MekaToolBlasterHandler {
    private static final int ANTIMATTER_CHARGE_DURATION = 72_000;
    private static final int ULTRA_CHARGE_TICKS = 20 * 15;
    private static final int STANDARD_COOLDOWN_TICKS = 20 * 15;
    private static final int ULTRA_COOLDOWN_TICKS = 20 * 35;
    private static final long STANDARD_ENERGY_COST = 500_000_000L;
    private static final long ULTRA_ENERGY_COST = STANDARD_ENERGY_COST * 10L;

    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.isCanceled() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof ItemMekaTool)) {
            return;
        }

        if (MekaToolLavaHandler.fillFromOffhand(player, stack)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.CONSUME);
            return;
        }

        IModule<ModuleMekaToolAntimatterStrikeUnit> antimatterModule = IModuleHelper.INSTANCE.getIfEnabled(
                stack, MekaSuitMoreModules.MEKA_TOOL_ANTIMATTER_STRIKE_UNIT);
        if (antimatterModule != null) {
            beginAntimatterCharge(event, player, stack);
            return;
        }

        IModule<ModuleMekaToolBlasterUnit> module = IModuleHelper.INSTANCE.getIfEnabled(stack, MekaSuitMoreModules.MEKA_TOOL_BLASTER_UNIT);
        if (module == null) {
            return;
        }
        ModuleMekaToolBlasterUnit.FireMode mode = module.getCustomInstance().fireMode();
        if (player.getCooldowns().isOnCooldown(stack.getItem())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.CONSUME);
            return;
        }

        boolean creative = player.getAbilities().instabuild;
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        IFluidHandlerItem lavaTank = MekaToolLavaHandler.create(stack);
        if (!creative) {
            if (energyContainer == null || energyContainer.extract(mode.getEnergyCost(), Action.SIMULATE, AutomationType.MANUAL) < mode.getEnergyCost()
                    || !MekaToolLavaHandler.hasLava(lavaTank, mode.getLavaCost())) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
                return;
            }
        }

        player.getCooldowns().addCooldown(stack.getItem(), mode.getCooldownTicks());
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.CONSUME);
        if (player.level().isClientSide()) {
            return;
        }

        if (!creative) {
            energyContainer.extract(mode.getEnergyCost(), Action.EXECUTE, AutomationType.MANUAL);
            lavaTank.drain(mode.getLavaCost(), IFluidHandler.FluidAction.EXECUTE);
        }
        Level level = player.level();
        Vec3 direction = player.getViewVector(1.0F).normalize();
        MekaToolBlasterFireball fireball = new MekaToolBlasterFireball(level, player, direction, mode.getExplosionPower());
        Vec3 origin = player.getEyePosition().add(direction.scale(0.7));
        fireball.setPos(origin.x, origin.y, origin.z);
        level.addFreshEntity(fireball);
    }

    private void beginAntimatterCharge(PlayerInteractEvent.RightClickItem event, Player player, ItemStack stack) {
        if (player.getCooldowns().isOnCooldown(stack.getItem())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.CONSUME);
            return;
        }
        IModule<ModuleMekaToolAntimatterStrikeUnit> module = IModuleHelper.INSTANCE.getIfEnabled(
                stack, MekaSuitMoreModules.MEKA_TOOL_ANTIMATTER_STRIKE_UNIT);
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        // The final cost depends on how long the button is held, so do not require
        // the Ultra amount here. The actual cost is checked on release.
        if (!player.getAbilities().instabuild && (energyContainer == null
                || energyContainer.extract(STANDARD_ENERGY_COST, Action.SIMULATE, AutomationType.MANUAL) < STANDARD_ENERGY_COST)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }
        // ItemMekaTool is normally not a bow-like item. The mixin supplies a long use
        // duration and bow animation only while the antimatter module is enabled.
        player.startUsingItem(event.getHand());
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.CONSUME);
    }

    @SubscribeEvent
    public void onAntimatterChargeReleased(LivingEntityUseItemEvent.Stop event) {
        if (!(event.getEntity() instanceof Player player) || event.getHand() != InteractionHand.MAIN_HAND
                || !(event.getItem().getItem() instanceof ItemMekaTool)) {
            return;
        }
        ItemStack stack = player.getItemInHand(event.getHand());
        IModule<ModuleMekaToolAntimatterStrikeUnit> module = IModuleHelper.INSTANCE.getIfEnabled(
                stack, MekaSuitMoreModules.MEKA_TOOL_ANTIMATTER_STRIKE_UNIT);
        if (module == null) {
            return;
        }
        event.setCanceled(true);
        int heldTicks = Math.max(0, ANTIMATTER_CHARGE_DURATION - event.getDuration());
        // One unified mode: release before 15 seconds fires Standard; holding for
        // at least 15 seconds automatically fires the Ultra strike.
        boolean ultra = heldTicks >= ULTRA_CHARGE_TICKS;
        fireAntimatterStrike(player, stack, ultra);
    }

    private void fireAntimatterStrike(Player player, ItemStack stack, boolean ultra) {
        int cooldownTicks = ultra ? ULTRA_COOLDOWN_TICKS : STANDARD_COOLDOWN_TICKS;
        long energyCost = ultra ? ULTRA_ENERGY_COST : STANDARD_ENERGY_COST;
        boolean creative = player.getAbilities().instabuild;
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        if (!creative && (energyContainer == null
                || energyContainer.extract(energyCost, Action.SIMULATE, AutomationType.MANUAL) < energyCost)) {
            return;
        }
        player.getCooldowns().addCooldown(stack.getItem(), cooldownTicks);
        if (player.level().isClientSide()) {
            return;
        }
        if (!creative) {
            energyContainer.extract(energyCost, Action.EXECUTE, AutomationType.MANUAL);
        }
        Level level = player.level();
        Vec3 direction = player.getViewVector(1.0F).normalize();
        AntimatterExplosiveOrbEntity orb = new AntimatterExplosiveOrbEntity(
                MoreMekaSuitModulesEntities.ANTIMATTER_EXPLOSIVE_ORB.get(), player, direction, level, ultra);
        Vec3 origin = player.getEyePosition().add(direction.scale(0.8));
        orb.setPos(origin.x, origin.y, origin.z);
        level.addFreshEntity(orb);
    }
}
