package moremekasuitmodules.common.content.gear;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import moremekasuitmodules.common.config.MoreModulesConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@ParametersAreNotNullByDefault
public class ModuleAutomaticExtinguishUnit implements ICustomModule<ModuleAutomaticExtinguishUnit> {
    @Override
    public void tickServer(IModule<ModuleAutomaticExtinguishUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player) {
        if (!player.isOnFire()) {
            return;
        }
        long energyUsage = MoreModulesConfig.config.mekaSuitEnergyUsageAutomaticExtinguish.get();
        if (module.hasEnoughEnergy(stack, energyUsage)) {
            player.clearFire();
            module.useEnergy(player, stack, energyUsage);
        }
    }
}
