package moremekasuitmodules.common.content.gear.mekanism.mekatool;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
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

import java.util.Locale;
import java.util.function.IntFunction;

@ParametersAreNotNullByDefault
public record ModuleMekaToolAntimatterStrikeUnit(StrikeMode mode)
        implements ICustomModule<ModuleMekaToolAntimatterStrikeUnit> {
    public static final ResourceLocation STRIKE_MODE = MoreMekaSuitModules.rl("antimatter_strike_mode");

    public ModuleMekaToolAntimatterStrikeUnit(IModule<ModuleMekaToolAntimatterStrikeUnit> module) {
        this(module.<StrikeMode>getConfigOrThrow(STRIKE_MODE).get());
    }

    @NothingNullByDefault
    public enum StrikeMode implements IHasTextComponent, StringRepresentable {
        STANDARD,
        ULTRA;

        public static final Codec<StrikeMode> CODEC = StringRepresentable.fromEnum(StrikeMode::values);
        public static final IntFunction<StrikeMode> BY_ID = ByIdMap.continuous(StrikeMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, StrikeMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, StrikeMode::ordinal);
        private final String serializedName = name().toLowerCase(Locale.ROOT);
        private final Component label = Component.translatable("module.moremekasuitmodules.antimatter_strike_mode." + serializedName);

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
