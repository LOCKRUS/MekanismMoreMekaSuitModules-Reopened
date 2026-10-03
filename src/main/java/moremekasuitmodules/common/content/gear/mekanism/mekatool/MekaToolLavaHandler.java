package moremekasuitmodules.common.content.gear.mekanism.mekatool;

import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismItems;
import moremekasuitmodules.common.registries.MoreMekaSuitModulesDataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class MekaToolLavaHandler {
    public static final int CAPACITY = 10_000;
    public static final int COST_PER_SHOT = 1_000;

    private MekaToolLavaHandler() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerItem(
                Capabilities.FLUID.item(),
                (ICapabilityProvider<ItemStack, Void, IFluidHandlerItem>) (stack, context) -> create(stack),
                MekanismItems.MEKA_TOOL.get());
    }

    public static IFluidHandlerItem create(ItemStack stack) {
        return new LavaTank(stack);
    }

    private static final class LavaTank extends FluidHandlerItemStack {
        private LavaTank(ItemStack stack) {
            super(MoreMekaSuitModulesDataComponents.MEKA_TOOL_LAVA, stack, CAPACITY);
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack resource) {
            return resource.is(Fluids.LAVA);
        }

        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            return fluid.is(Fluids.LAVA);
        }

        @Override
        public boolean canDrainFluidType(FluidStack fluid) {
            return fluid.is(Fluids.LAVA);
        }
    }

    public static boolean hasLava(IFluidHandlerItem handler) {
        return handler.drain(COST_PER_SHOT, IFluidHandler.FluidAction.SIMULATE).getAmount() >= COST_PER_SHOT;
    }
}
