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
public record ModuleMekaToolBlasterUnit(FireMode fireMode) implements ICustomModule<ModuleMekaToolBlasterUnit> {
    public static final ResourceLocation FIRE_MODE = MoreMekaSuitModules.rl("blaster_fire_mode");

    public ModuleMekaToolBlasterUnit(IModule<ModuleMekaToolBlasterUnit> module) {
        this(module.<FireMode>getConfigOrThrow(FIRE_MODE).get());
    }

    @Override
    public void addHUDStrings(IModule<ModuleMekaToolBlasterUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player, Consumer<Component> hudStringAdder) {
        if (module.isEnabled()) {
            hudStringAdder.accept(Component.translatable(
                    "module.moremekasuitmodules.meka_tool_blaster_mode_hud",
                    fireMode.getTextComponent()));
        }
    }

    @NothingNullByDefault
    public enum FireMode implements IHasTextComponent, StringRepresentable {
        HEAVY(4, 20, 10_000_000, 20_000),
        STANDARD(2, 8, 7_500_000, 10_000),
        RAPID(1, 3, 5_000_000, 2_000);

        public static final Codec<FireMode> CODEC = StringRepresentable.fromEnum(FireMode::values);
        public static final IntFunction<FireMode> BY_ID = ByIdMap.continuous(FireMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, FireMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, FireMode::ordinal);

        private final int explosionPower;
        private final int cooldownTicks;
        private final long energyCost;
        private final int lavaCost;
        private final String serializedName;
        private final Component label;

        FireMode(int explosionPower, int cooldownTicks, long energyCost, int lavaCost) {
            this.explosionPower = explosionPower;
            this.cooldownTicks = cooldownTicks;
            this.energyCost = energyCost;
            this.lavaCost = lavaCost;
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.label = TextComponentUtil.getString(serializedName);
        }

        public int getExplosionPower() {
            return explosionPower;
        }

        public int getCooldownTicks() {
            return cooldownTicks;
        }

        public long getEnergyCost() {
            return energyCost;
        }

        public int getLavaCost() {
            return lavaCost;
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
