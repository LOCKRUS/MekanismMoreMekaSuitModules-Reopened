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
public record ModuleLootingAmplificationUnit(LootingLevel lootingLevel) implements ICustomModule<ModuleLootingAmplificationUnit> {
    public static final ResourceLocation LOOTING_LEVEL = MoreMekaSuitModules.rl("looting_level");
    public static final int MAX_MODULES = 10;

    public ModuleLootingAmplificationUnit(IModule<ModuleLootingAmplificationUnit> module) {
        this(module.<LootingLevel>getConfigOrThrow(LOOTING_LEVEL).get());
    }

    public int getEffectiveLevel(int installedCount) {
        return Math.min(Math.min(installedCount, MAX_MODULES), lootingLevel.getLevel());
    }

    @Override
    public void addHUDStrings(IModule<ModuleLootingAmplificationUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player, Consumer<Component> hudStringAdder) {
        if (module.isEnabled()) {
            hudStringAdder.accept(Component.translatable("module.moremekasuitmodules.looting_level_hud", lootingLevel.getTextComponent()));
        }
    }

    @NothingNullByDefault
    public enum LootingLevel implements IHasTextComponent, StringRepresentable {
        LOW(2),
        NORMAL(4),
        MEDIUM(6),
        HIGH(8),
        ULTRA(10);

        public static final Codec<LootingLevel> CODEC = StringRepresentable.fromEnum(LootingLevel::values);
        public static final IntFunction<LootingLevel> BY_ID = ByIdMap.continuous(LootingLevel::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, LootingLevel> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, LootingLevel::ordinal);

        private final int level;
        private final String serializedName;
        private final Component label;

        LootingLevel(int level) {
            this.level = level;
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.label = TextComponentUtil.getString(Integer.toString(level));
        }

        public int getLevel() {
            return level;
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
