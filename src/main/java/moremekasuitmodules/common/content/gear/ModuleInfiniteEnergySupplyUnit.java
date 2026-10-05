package moremekasuitmodules.common.content.gear;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@ParametersAreNotNullByDefault
public class ModuleInfiniteEnergySupplyUnit implements ICustomModule<ModuleInfiniteEnergySupplyUnit> {


    @Override
    public void tickClient(IModule<ModuleInfiniteEnergySupplyUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player) {
        // Energy storage is authoritative on the server; do not mutate it locally.
    }

    @Override
    public void tickServer(IModule<ModuleInfiniteEnergySupplyUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player) {
        IEnergyContainer energyContainer = module.getEnergyContainer(stack);
        if (energyContainer != null && !energyContainer.isEmpty()) {
            energyContainer.insert(energyContainer.getNeeded(), Action.EXECUTE, AutomationType.MANUAL);
        }
    }
}
