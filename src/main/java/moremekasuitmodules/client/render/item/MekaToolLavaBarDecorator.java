package moremekasuitmodules.client.render.item;

import mekanism.common.item.gear.ItemMekaTool;
import moremekasuitmodules.common.content.gear.mekanism.mekatool.MekaToolLavaHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

/** Renders the MekaTool lava amount on the line above Mekanism's energy bar. */
public final class MekaToolLavaBarDecorator implements IItemDecorator {
    public static final MekaToolLavaBarDecorator INSTANCE = new MekaToolLavaBarDecorator();

    private MekaToolLavaBarDecorator() {
    }

    @Override
    public boolean render(GuiGraphics graphics, Font font, ItemStack stack, int x, int y) {
        if (!(stack.getItem() instanceof ItemMekaTool)) {
            return false;
        }
        IFluidHandlerItem tank = MekaToolLavaHandler.create(stack);
        if (tank == null || tank.getTanks() == 0) {
            return false;
        }
        int capacity = tank.getTankCapacity(0);
        int amount = tank.getFluidInTank(0).getAmount();
        int width = capacity <= 0 ? 0 : Math.min(13, amount * 13 / capacity);
        int barY = y + 12;
        graphics.fill(RenderType.guiOverlay(), x + 2, barY, x + 15, barY + 1, 0xFF000000);
        if (width > 0) {
            graphics.fill(RenderType.guiOverlay(), x + 2, barY, x + 2 + width, barY + 1, 0xFFE67E22);
        }
        return true;
    }
}
