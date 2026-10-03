package moremekasuitmodules.mixin.mekanism;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.common.item.gear.ItemMekaTool;
import moremekasuitmodules.common.content.gear.mekanism.mekatool.ModuleMekaToolPerformanceAmplificationUnit;
import moremekasuitmodules.common.registries.MekaSuitMoreModules;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemMekaTool.class)
public abstract class MixinItemMekaTool {
    @ModifyReturnValue(method = "getDestroySpeed", at = @At("RETURN"))
    private float moreMekaSuitModules$amplifyDestroySpeed(float original, ItemStack stack, BlockState state) {
        IModule<ModuleMekaToolPerformanceAmplificationUnit> module = IModuleHelper.INSTANCE.getIfEnabled(stack, MekaSuitMoreModules.MEKA_TOOL_PERFORMANCE_AMPLIFICATION_UNIT);
        if (module == null) {
            return original;
        }
        return original * module.getCustomInstance().getMultiplier(module.getInstalledCount());
    }

}
