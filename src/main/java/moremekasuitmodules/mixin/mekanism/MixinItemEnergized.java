package moremekasuitmodules.mixin.mekanism;

import mekanism.common.item.ItemEnergized;
import mekanism.common.item.gear.ItemMekaTool;
import mekanism.common.util.StorageUtils;
import moremekasuitmodules.common.content.gear.mekanism.mekatool.MekaToolLavaHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEnergized.class)
public abstract class MixinItemEnergized {
    @Inject(method = "appendHoverText", at = @At("TAIL"))
    private void moreMekaSuitModules$addLavaTooltip(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag, CallbackInfo cir) {
        if (stack.getItem() instanceof ItemMekaTool && MekaToolLavaHandler.create(stack) != null) {
            StorageUtils.addStoredFluid(stack, tooltip);
        }
    }
}
