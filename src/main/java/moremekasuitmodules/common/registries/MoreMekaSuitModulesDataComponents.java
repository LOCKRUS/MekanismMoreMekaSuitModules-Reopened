package moremekasuitmodules.common.registries;


import mekanism.common.registration.MekanismDeferredHolder;
import moremekasuitmodules.common.MoreMekaSuitModules;
import moremekasuitmodules.common.registries.impl.DataComponentDeferredRegisterEX;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

public class MoreMekaSuitModulesDataComponents {

    private MoreMekaSuitModulesDataComponents() {
    }

    public static final DataComponentDeferredRegisterEX DATA_COMPONENTS = new DataComponentDeferredRegisterEX(MoreMekaSuitModules.MODID);

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Double>> PROTECTION_POINTS = DATA_COMPONENTS.registerDouble("protection_points");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Double>> SHIELD_ENTROPY = DATA_COMPONENTS.registerDouble("shield_entropy");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<SimpleFluidContent>> MEKA_TOOL_LAVA = DATA_COMPONENTS.simple(
            "meka_tool_lava",
            builder -> builder.persistent(SimpleFluidContent.CODEC).networkSynchronized(SimpleFluidContent.STREAM_CODEC));
}
