package moremekasuitmodules.common.content.gear.mekanism.mekatool;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import moremekasuitmodules.common.MoreMekaSuitModules;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

import java.util.List;
import java.util.function.Consumer;

@ParametersAreNotNullByDefault
public final class ModuleMekaToolPerformanceAmplificationUnit implements ICustomModule<ModuleMekaToolPerformanceAmplificationUnit> {
    public static final int MAX_MODULES = 4;

    public int getMultiplier(int installedCount) {
        return Math.max(1, Math.min(MAX_MODULES + 1, installedCount + 1));
    }

    @Override
    public void addHUDStrings(IModule<ModuleMekaToolPerformanceAmplificationUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player, Consumer<Component> hudStringAdder) {
        if (module.isEnabled()) {
            hudStringAdder.accept(Component.translatable(
                    "module.moremekasuitmodules.meka_tool_performance_multiplier_hud",
                    getMultiplier(module.getInstalledCount())));
        }
    }

    @Override
    public void adjustAttributes(IModule<ModuleMekaToolPerformanceAmplificationUnit> module, ItemAttributeModifierEvent event) {
        if (!module.isEnabled()) {
            return;
        }
        double multiplier = getMultiplier(module.getInstalledCount());
        for (var entry : List.copyOf(event.getModifiers())) {
            if (entry.attribute().equals(Attributes.ATTACK_DAMAGE)
                    && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                AttributeModifier old = entry.modifier();
                event.replaceModifier(entry.attribute(), new AttributeModifier(old.id(), old.amount() * multiplier, old.operation()), entry.slot());
            }
        }
    }
}
