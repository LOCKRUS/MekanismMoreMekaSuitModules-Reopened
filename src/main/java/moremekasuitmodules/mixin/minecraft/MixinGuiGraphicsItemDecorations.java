package moremekasuitmodules.mixin.minecraft;

import moremekasuitmodules.common.content.gear.mekanism.mekatool.MekaToolLavaHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class MixinGuiGraphicsItemDecorations {
    @Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At("TAIL"))
    private void moreMekaSuitModules$renderLavaBar(Font font, ItemStack stack, int x, int y, String countString, CallbackInfo ci) {
        IFluidHandlerItem tank = MekaToolLavaHandler.create(stack);
        if (tank == null || tank.getTanks() == 0) {
            return;
        }
        int capacity = tank.getTankCapacity(0);
        int amount = tank.getFluidInTank(0).getAmount();
        int width = capacity <= 0 ? 0 : Math.min(13, amount * 13 / capacity);
        int barY = y + 12;
        GuiGraphics graphics = (GuiGraphics) (Object) this;
        graphics.fill(RenderType.guiOverlay(), x + 2, barY, x + 15, barY + 1, 0xFF000000);
        if (width > 0) {
            graphics.fill(RenderType.guiOverlay(), x + 2, barY, x + 2 + width, barY + 1, 0xFFE67E22);
        }
    }
}
