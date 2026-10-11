package moremekasuitmodules.common.content.gear.mekanism.mekatool;

import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.common.entity.EntityFlame;
import mekanism.common.item.gear.ItemMekaTool;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.capabilities.Capabilities;
import moremekasuitmodules.common.registries.MekaSuitMoreModules;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

/**
 * Reuses Mekanism's EntityFlame while making a MekaTool act as the flamethrower.
 * The MekaTool must contain the Lava Tank module. Consumption is mode-dependent.
 */
public final class MekaToolFlamethrowerHandler {
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.isCanceled() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof ItemMekaTool)) {
            return;
        }
        IModule<ModuleMekaToolFlamethrowerUnit> module = IModuleHelper.INSTANCE.getIfEnabled(
                stack, MekaSuitMoreModules.MEKA_TOOL_FLAMETHROWER_UNIT);
        if (module == null || (!player.getAbilities().instabuild
                && !canFire(player, stack, module.getCustomInstance().flameMode()))) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }
        player.startUsingItem(event.getHand());
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.CONSUME);
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !player.isUsingItem()
                || player.getUsedItemHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        ItemStack stack = player.getUseItem();
        IModule<ModuleMekaToolFlamethrowerUnit> module = IModuleHelper.INSTANCE.getIfEnabled(
                stack, MekaSuitMoreModules.MEKA_TOOL_FLAMETHROWER_UNIT);
        if (!(stack.getItem() instanceof ItemMekaTool) || module == null) {
            return;
        }
        ModuleMekaToolFlamethrowerUnit.FlameMode mode = module.getCustomInstance().flameMode();
        if (!player.getAbilities().instabuild && !canFire(player, stack, mode)) {
            player.releaseUsingItem();
            return;
        }
        EntityFlame flame = EntityFlame.create(player.level(), player, InteractionHand.MAIN_HAND,
                module.getCustomInstance().flameMode().mekanismMode());
        if (flame == null) {
            return;
        }
        IFluidHandlerItem lavaTank = MekaToolLavaHandler.create(stack);
        IChemicalHandler hydrogenTank = getHydrogenTank(player);
        if (!player.getAbilities().instabuild && (lavaTank == null || hydrogenTank == null)) {
            return;
        }
        if (!player.getAbilities().instabuild) {
            lavaTank.drain(mode.lavaPerFlame(), IFluidHandler.FluidAction.EXECUTE);
            hydrogenTank.extractChemical(MekanismChemicals.HYDROGEN.asStack(mode.hydrogenPerFlame()), Action.EXECUTE);
        }
        player.level().addFreshEntity(flame);
    }

    private static boolean canFire(Player player, ItemStack stack, ModuleMekaToolFlamethrowerUnit.FlameMode mode) {
        IFluidHandlerItem lavaTank = MekaToolLavaHandler.create(stack);
        if (!MekaToolLavaHandler.hasLava(lavaTank, mode.lavaPerFlame())) {
            return false;
        }
        IChemicalHandler hydrogenTank = getHydrogenTank(player);
        if (hydrogenTank == null) {
            return false;
        }
        ChemicalStack extracted = hydrogenTank.extractChemical(
                MekanismChemicals.HYDROGEN.asStack(mode.hydrogenPerFlame()), Action.SIMULATE);
        return extracted.getAmount() >= mode.hydrogenPerFlame();
    }

    private static IChemicalHandler getHydrogenTank(Player player) {
        ItemStack armor = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
        return Capabilities.CHEMICAL.getCapability(armor);
    }
}
