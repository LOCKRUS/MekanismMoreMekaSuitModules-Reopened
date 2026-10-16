package moremekasuitmodules.mixin.mekanism;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import mekanism.api.gear.IModuleHelper;
import mekanism.common.attachments.containers.energy.ComponentBackedEnergyContainer;
import moremekasuitmodules.common.registries.MekaSuitMoreModules;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/** Multiplies the capacity of the specific MekaSuit/MekaTool stack containing the module. */
@Mixin(ComponentBackedEnergyContainer.class)
public abstract class MixinComponentBackedEnergyContainer {

    @Shadow @Final protected ItemStack attachedTo;

    @ModifyReturnValue(method = "getMaxEnergy", at = @At("RETURN"))
    private long moreMekaSuitModules$amplifyCapacity(long original) {
        return IModuleHelper.INSTANCE.getIfEnabled(attachedTo, MekaSuitMoreModules.CAPACITY_AMPLIFICATION_UNIT) == null
                ? original
                : Math.multiplyExact(original, 10L);
    }
}
