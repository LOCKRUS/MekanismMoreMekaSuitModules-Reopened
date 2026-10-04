package moremekasuitmodules.mixin.mekanism;

import mekanism.common.item.ItemEnergized;
import mekanism.common.item.gear.ItemMekaTool;
import moremekasuitmodules.common.content.gear.mekanism.mekatool.MekaToolLavaHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEnergized.class)
public abstract class MixinItemEnergized {
    @Inject(method = "appendHoverText", at = @At("TAIL"))
    private void moreMekaSuitModules$addLavaTooltip(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag, CallbackInfo cir) {
        if (stack.getItem() instanceof ItemMekaTool) {
            IFluidHandlerItem tank = MekaToolLavaHandler.create(stack);
            if (tank != null && tank.getTanks() > 0) {
                tooltip.add(Component.translatable("tooltip.moremekasuitmodules.lava_storage",
                        tank.getFluidInTank(0).getAmount(), tank.getTankCapacity(0)));
            }
        }
    }
}
