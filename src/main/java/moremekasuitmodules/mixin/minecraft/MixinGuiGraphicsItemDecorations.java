package moremekasuitmodules.mixin.minecraft;

import moremekasuitmodules.common.content.gear.mekanism.mekatool.MekaToolLavaHandler;
import moremekasuitmodules.client.ClientTickHandler;
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
        float charge = ClientTickHandler.antimatterChargeProgress(stack);
        if (charge > 0.0F) {
            int barY = y + 12;
            int width = Math.max(1, Math.min(13, Math.round(charge * 13.0F)));
            int color = ClientTickHandler.isAntimatterChargeReady(stack)
                    ? 0xFFFFD54A : gradientColor(0xFFE53935, 0xFF43D17A, charge);
            GuiGraphics graphics = (GuiGraphics) (Object) this;
            graphics.fill(RenderType.guiOverlay(), x + 2, barY, x + 15, barY + 1, 0xFF160B0B);
            graphics.fill(RenderType.guiOverlay(), x + 2, barY, x + 2 + width, barY + 1, color);
            return;
        }
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

    private static int gradientColor(int from, int to, float progress) {
        int r = interpolate((from >> 16) & 0xFF, (to >> 16) & 0xFF, progress);
        int g = interpolate((from >> 8) & 0xFF, (to >> 8) & 0xFF, progress);
        int b = interpolate(from & 0xFF, to & 0xFF, progress);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static int interpolate(int from, int to, float progress) {
        return Math.round(from + (to - from) * progress);
    }
}
