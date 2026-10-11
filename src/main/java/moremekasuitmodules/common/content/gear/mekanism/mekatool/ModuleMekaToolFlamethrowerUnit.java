package moremekasuitmodules.common.content.gear.mekanism.mekatool;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.config.ModuleEnumConfig;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import moremekasuitmodules.common.MoreMekaSuitModules;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntFunction;

@ParametersAreNotNullByDefault
public record ModuleMekaToolFlamethrowerUnit(FlameMode flameMode)
        implements ICustomModule<ModuleMekaToolFlamethrowerUnit> {
    public static final ResourceLocation FLAME_MODE = MoreMekaSuitModules.rl("flamethrower_mode");

    public ModuleMekaToolFlamethrowerUnit(IModule<ModuleMekaToolFlamethrowerUnit> module) {
        this(module.<FlameMode>getConfigOrThrow(FLAME_MODE).get());
    }

    @Override
    public void addHUDStrings(IModule<ModuleMekaToolFlamethrowerUnit> module,
                               IModuleContainer moduleContainer, ItemStack stack, Player player,
                               Consumer<Component> hudStringAdder) {
        if (module.isEnabled()) {
            hudStringAdder.accept(Component.translatable(
                    "module.moremekasuitmodules.meka_tool_flamethrower_mode_hud",
                    flameMode.getTextComponent()));
        }
    }

    @NothingNullByDefault
    public enum FlameMode implements IHasTextComponent, StringRepresentable {
        COMBAT("combat"),
        HEAT("heat"),
        INFERNO("inferno");

        public static final Codec<FlameMode> CODEC = StringRepresentable.fromEnum(FlameMode::values);
        public static final IntFunction<FlameMode> BY_ID = ByIdMap.continuous(
                FlameMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, FlameMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, FlameMode::ordinal);
        private final String serializedName;
        private final Component label;

        FlameMode(String serializedName) {
            this.serializedName = serializedName.toLowerCase(Locale.ROOT);
            this.label = TextComponentUtil.getString("flamethrower_" + this.serializedName);
        }

        public mekanism.common.item.gear.ItemFlamethrower.FlamethrowerMode mekanismMode() {
            return mekanism.common.item.gear.ItemFlamethrower.FlamethrowerMode.valueOf(name());
        }

        @Override
        public Component getTextComponent() {
            return label;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}
