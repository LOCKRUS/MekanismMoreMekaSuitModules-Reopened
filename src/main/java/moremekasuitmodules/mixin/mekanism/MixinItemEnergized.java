package moremekasuitmodules.mixin.mekanism;

import mekanism.common.item.ItemEnergized;
import mekanism.common.item.gear.ItemMekaTool;
import moremekasuitmodules.common.content.gear.mekanism.mekatool.MekaToolLavaHandler;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEnergized.class)
public abstract class MixinItemEnergized {
    @Inject(method = "isBarVisible", at = @At("HEAD"), cancellable = true)
    private void moreMekaSuitModules$showLavaBar(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof ItemMekaTool && MekaToolLavaHandler.create(stack) != null) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getBarWidth", at = @At("HEAD"), cancellable = true)
    private void moreMekaSuitModules$lavaBarWidth(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (!(stack.getItem() instanceof ItemMekaTool)) {
            return;
        }
        IFluidHandlerItem tank = MekaToolLavaHandler.create(stack);
        if (tank != null) {
            int capacity = tank.getTankCapacity(0);
            int amount = tank.getFluidInTank(0).getAmount();
            cir.setReturnValue(capacity <= 0 ? 0 : Math.min(13, amount * 13 / capacity));
        }
    }

    @Inject(method = "getBarColor", at = @At("HEAD"), cancellable = true)
    private void moreMekaSuitModules$lavaBarColor(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.getItem() instanceof ItemMekaTool && MekaToolLavaHandler.create(stack) != null) {
            cir.setReturnValue(0xE67E22);
        }
    }
}
