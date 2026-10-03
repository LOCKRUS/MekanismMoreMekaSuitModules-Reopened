package moremekasuitmodules.common.content.gear.mekanism.mekasuit;

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
import java.util.function.IntFunction;

@ParametersAreNotNullByDefault
public record ModuleCounterattackUnit(CounterLevel counterLevel) implements ICustomModule<ModuleCounterattackUnit> {
    public static final int MAX_MODULES_PER_ARMOR = 10;
    public static final float DAMAGE_DIVISOR = 10.0F;
    public static final ResourceLocation COUNTER_LEVEL = MoreMekaSuitModules.rl("counter_level");

    public ModuleCounterattackUnit(IModule<ModuleCounterattackUnit> module) {
        this(module.<CounterLevel>getConfigOrThrow(COUNTER_LEVEL).get());
    }

    public int getEffectiveCount(int installedCount) {
        return Math.min(Math.min(installedCount, MAX_MODULES_PER_ARMOR), counterLevel.maxModules);
    }

    @Override
    public void addHUDStrings(IModule<ModuleCounterattackUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player, java.util.function.Consumer<Component> hudStringAdder) {
        if (module.isEnabled()) {
            hudStringAdder.accept(Component.translatable("module.moremekasuitmodules.counter_level_hud", counterLevel.getTextComponent()));
        }
    }

    @NothingNullByDefault
    public enum CounterLevel implements IHasTextComponent, StringRepresentable {
        LOW(2), NORMAL(4), MEDIUM(6), HIGH(8), ULTRA(10);

        public static final Codec<CounterLevel> CODEC = StringRepresentable.fromEnum(CounterLevel::values);
        public static final IntFunction<CounterLevel> BY_ID = ByIdMap.continuous(CounterLevel::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, CounterLevel> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, CounterLevel::ordinal);

        private final int maxModules;
        private final String serializedName;
        private final Component label;

        CounterLevel(int maxModules) {
            this.maxModules = maxModules;
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.label = TextComponentUtil.getString(Integer.toString(maxModules));
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
