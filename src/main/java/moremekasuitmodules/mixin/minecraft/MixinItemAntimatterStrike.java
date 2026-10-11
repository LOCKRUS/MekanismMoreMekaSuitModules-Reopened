package moremekasuitmodules.mixin.minecraft;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import moremekasuitmodules.common.AntimatterStrikeActivation;
import moremekasuitmodules.common.registries.MekaSuitMoreModulesItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public abstract class MixinItemAntimatterStrike {
    @ModifyReturnValue(method = "getName", at = @org.spongepowered.asm.mixin.injection.At("RETURN"))
    private Component moreMekaSuitModules$appendActivationState(Component original, ItemStack stack) {
        if (!stack.is(MekaSuitMoreModulesItem.MODULE_MEKA_TOOL_ANTIMATTER_STRIKE.get())) {
            return original;
        }
        boolean active = AntimatterStrikeActivation.isActive(stack);
        Component state = Component.translatable(active
                        ? "item.moremekasuitmodules.antimatter_strike_active"
                        : "item.moremekasuitmodules.antimatter_strike_disabled")
                .withStyle(active ? ChatFormatting.GREEN : ChatFormatting.RED);
        return original.copy()
                .append(Component.literal(" (").withStyle(ChatFormatting.WHITE))
                .append(state)
                .append(Component.literal(")").withStyle(ChatFormatting.WHITE));
    }

    @ModifyReturnValue(method = "isFoil", at = @org.spongepowered.asm.mixin.injection.At("RETURN"))
    private boolean moreMekaSuitModules$activeGlint(boolean original, ItemStack stack) {
        return original || (stack.is(MekaSuitMoreModulesItem.MODULE_MEKA_TOOL_ANTIMATTER_STRIKE.get())
                && AntimatterStrikeActivation.isActive(stack));
    }
}
