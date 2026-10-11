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
 * The MekaTool must contain the Lava Tank module and 10 mB of lava per flame.
 */
public final class MekaToolFlamethrowerHandler {
    private static final int LAVA_PER_FLAME = 10;
    private static final long HYDROGEN_PER_FLAME = 2;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.isCanceled() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof ItemMekaTool)
                || IModuleHelper.INSTANCE.getIfEnabled(stack, MekaSuitMoreModules.MEKA_TOOL_FLAMETHROWER_UNIT) == null) {
            return;
        }
        if (!canFire(player, stack)) {
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
        if (!canFire(player, stack)) {
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
        if (lavaTank == null || hydrogenTank == null) {
            return;
        }
        lavaTank.drain(LAVA_PER_FLAME, IFluidHandler.FluidAction.EXECUTE);
        hydrogenTank.extractChemical(MekanismChemicals.HYDROGEN.asStack(HYDROGEN_PER_FLAME), Action.EXECUTE);
        player.level().addFreshEntity(flame);
    }

    private static boolean canFire(Player player, ItemStack stack) {
        IFluidHandlerItem lavaTank = MekaToolLavaHandler.create(stack);
        if (!MekaToolLavaHandler.hasLava(lavaTank, LAVA_PER_FLAME)) {
            return false;
        }
        IChemicalHandler hydrogenTank = getHydrogenTank(player);
        if (hydrogenTank == null) {
            return false;
        }
        ChemicalStack extracted = hydrogenTank.extractChemical(
                MekanismChemicals.HYDROGEN.asStack(HYDROGEN_PER_FLAME), Action.SIMULATE);
        return extracted.getAmount() >= HYDROGEN_PER_FLAME;
    }

    private static IChemicalHandler getHydrogenTank(Player player) {
        ItemStack armor = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
        return Capabilities.CHEMICAL.getCapability(armor);
    }
}
