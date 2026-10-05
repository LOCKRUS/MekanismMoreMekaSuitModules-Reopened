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
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class MekaToolBlasterHandler {
    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        // Teleport, farming, and other right-click modules may claim the same item
        // interaction. Never overwrite a handler that already consumed it.
        if (event.isCanceled()) {
            return;
        }
        if (event.getHand() != InteractionHand.MAIN_HAND) {
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
            fireAntimatterStrike(event, player, stack);
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
            if (energyContainer == null || energyContainer.extract(mode.getEnergyCost(), Action.SIMULATE, AutomationType.MANUAL) < mode.getEnergyCost()) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
                return;
            }
            if (!MekaToolLavaHandler.hasLava(lavaTank, mode.getLavaCost())) {
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

    private void fireAntimatterStrike(PlayerInteractEvent.RightClickItem event, Player player, ItemStack stack) {
        final int cooldownTicks = 600;
        final long energyCost = 500_000_000L;
        if (player.getCooldowns().isOnCooldown(stack.getItem())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.CONSUME);
            return;
        }
        boolean creative = player.getAbilities().instabuild;
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        if (!creative && (energyContainer == null
                || energyContainer.extract(energyCost, Action.SIMULATE, AutomationType.MANUAL) < energyCost)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }

        player.getCooldowns().addCooldown(stack.getItem(), cooldownTicks);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.CONSUME);
        if (player.level().isClientSide()) {
            return;
        }

        if (!creative) {
            energyContainer.extract(energyCost, Action.EXECUTE, AutomationType.MANUAL);
        }
        Level level = player.level();
        Vec3 direction = player.getViewVector(1.0F).normalize();
        AntimatterExplosiveOrbEntity orb = new AntimatterExplosiveOrbEntity(
                MoreMekaSuitModulesEntities.ANTIMATTER_EXPLOSIVE_ORB.get(), player, direction, level);
        Vec3 origin = player.getEyePosition().add(direction.scale(0.8));
        orb.setPos(origin.x, origin.y, origin.z);
        level.addFreshEntity(orb);
    }
}
