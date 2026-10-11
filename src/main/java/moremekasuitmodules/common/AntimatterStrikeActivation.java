package moremekasuitmodules.common;

import moremekasuitmodules.common.registries.MoreMekaSuitModulesDataComponents;
import net.minecraft.world.item.ItemStack;

public final class AntimatterStrikeActivation {
    private AntimatterStrikeActivation() {
    }

    public static boolean isActive(ItemStack stack) {
        return stack.getOrDefault(MoreMekaSuitModulesDataComponents.ANTIMATTER_STRIKE_ACTIVE, false);
    }

    public static void activate(ItemStack stack) {
        stack.set(MoreMekaSuitModulesDataComponents.ANTIMATTER_STRIKE_ACTIVE, true);
    }
}
