package moremekasuitmodules.common.content.gear.mekanism.mekatool;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.common.util.StorageUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

/**
 * Converts lava stored in the MekaTool's lava-tank expansion into MekaTool energy.
 * The conversion intentionally follows the requested 2 mB lava per 1 FE ratio.
 */
@ParametersAreNotNullByDefault
public final class ModuleMekaToolHeatGeneratorUnit implements ICustomModule<ModuleMekaToolHeatGeneratorUnit> {
    public static final int MAX_MODULES = 4;
    public static final long LAVA_PER_FE = 2L;
    public static final long FE_PER_TICK_PER_MODULE = 200L;

    @Override
    public void tickServer(IModule<ModuleMekaToolHeatGeneratorUnit> module,
                           IModuleContainer moduleContainer,
                           ItemStack stack,
                           Player player) {
        if (!module.isEnabled() || player.level().isClientSide() || player.isSpectator()) {
            return;
        }
        IEnergyContainer energy = StorageUtils.getEnergyContainer(stack, 0);
        IFluidHandlerItem lava = MekaToolLavaHandler.create(stack);
        if (energy == null || lava == null || energy.getNeeded() <= 0) {
            return;
        }

        long requested = Math.min(
                Math.min(energy.getNeeded(), FE_PER_TICK_PER_MODULE * Math.min(MAX_MODULES, module.getInstalledCount())),
                lava.getFluidInTank(0).getAmount() / LAVA_PER_FE);
        if (requested <= 0) {
            return;
        }

        long inserted = energy.insert(requested, Action.EXECUTE, AutomationType.MANUAL);
        if (inserted > 0) {
            lava.drain(Math.toIntExact(inserted * LAVA_PER_FE), IFluidHandler.FluidAction.EXECUTE);
        }
    }
}
