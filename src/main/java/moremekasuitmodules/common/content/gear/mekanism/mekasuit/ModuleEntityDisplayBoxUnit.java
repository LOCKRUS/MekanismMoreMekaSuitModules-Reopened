package moremekasuitmodules.common.content.gear.mekanism.mekasuit;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.ModuleConfig;
import mekanism.api.gear.config.ModuleColorConfig;
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
public record ModuleEntityDisplayBoxUnit(Range range, MaxBoxes maxBoxes, HealthDisplay healthDisplay,
                                         int boxColor, int nameColor, int distanceColor)
        implements ICustomModule<ModuleEntityDisplayBoxUnit> {
    public static final int DEFAULT_BOX_COLOR = 0xFF3CFE9A;
    public static final int DEFAULT_NAME_COLOR = 0xFFFFFFFF;
    public static final int DEFAULT_DISTANCE_COLOR = 0xFFFFFF55;
    public static final ResourceLocation RANGE = MoreMekaSuitModules.rl("entity_display_range");
    public static final ResourceLocation MAX_BOXES = MoreMekaSuitModules.rl("entity_display_max_boxes");
    public static final ResourceLocation HEALTH_DISPLAY = MoreMekaSuitModules.rl("entity_display_health");
    public static final ResourceLocation BOX_COLOR = MoreMekaSuitModules.rl("entity_display_box_color");
    public static final ResourceLocation NAME_COLOR = MoreMekaSuitModules.rl("entity_display_name_color");
    public static final ResourceLocation DISTANCE_COLOR = MoreMekaSuitModules.rl("entity_display_distance_color");

    public ModuleEntityDisplayBoxUnit(IModule<ModuleEntityDisplayBoxUnit> module) {
        this(module.<Range>getConfigOrThrow(RANGE).get(),
                module.<MaxBoxes>getConfigOrThrow(MAX_BOXES).get(),
                module.<HealthDisplay>getConfigOrThrow(HEALTH_DISPLAY).get(),
                module.<Integer>getConfigOrThrow(BOX_COLOR).get(),
                module.<Integer>getConfigOrThrow(NAME_COLOR).get(),
                module.<Integer>getConfigOrThrow(DISTANCE_COLOR).get());
    }

    public int rangeBlocks() { return range.blocks; }
    public int maxBoxCount() { return maxBoxes.count; }
    public boolean drawHealthBar() { return healthDisplay.bar; }
    public boolean drawHealthText() { return healthDisplay.text; }

    @NothingNullByDefault
    public enum Range implements IHasTextComponent, StringRepresentable {
        OFF(0), LOW(8), MEDIUM(16), HIGH(32), ULTRA(64);
        public static final Codec<Range> CODEC = StringRepresentable.fromEnum(Range::values);
        public static final IntFunction<Range> BY_ID = ByIdMap.continuous(Range::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, Range> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Range::ordinal);
        private final int blocks;
        private final String serializedName;
        private final Component label;
        Range(int blocks) { this.blocks = blocks; this.serializedName = name().toLowerCase(Locale.ROOT); this.label = TextComponentUtil.getString(Integer.toString(blocks)); }
        @Override public Component getTextComponent() { return label; }
        @Override public String getSerializedName() { return serializedName; }
    }

    @NothingNullByDefault
    public enum MaxBoxes implements IHasTextComponent, StringRepresentable {
        LOW(64), MEDIUM(256), HIGH(512), ULTRA(1024), ALL(Integer.MAX_VALUE);
        public static final Codec<MaxBoxes> CODEC = StringRepresentable.fromEnum(MaxBoxes::values);
        public static final IntFunction<MaxBoxes> BY_ID = ByIdMap.continuous(MaxBoxes::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, MaxBoxes> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, MaxBoxes::ordinal);
        private final int count;
        private final String serializedName;
        private final Component label;
        MaxBoxes(int count) { this.count = count; this.serializedName = name().toLowerCase(Locale.ROOT); this.label = TextComponentUtil.getString(count == Integer.MAX_VALUE ? "All" : Integer.toString(count)); }
        @Override public Component getTextComponent() { return label; }
        @Override public String getSerializedName() { return serializedName; }
    }

    @NothingNullByDefault
    public enum HealthDisplay implements IHasTextComponent, StringRepresentable {
        OFF(false, false), BAR(true, false), TEXT(false, true), BOTH(true, true);
        public static final Codec<HealthDisplay> CODEC = StringRepresentable.fromEnum(HealthDisplay::values);
        public static final IntFunction<HealthDisplay> BY_ID = ByIdMap.continuous(HealthDisplay::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, HealthDisplay> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, HealthDisplay::ordinal);
        private final boolean bar;
        private final boolean text;
        private final String serializedName;
        private final Component label;
        HealthDisplay(boolean bar, boolean text) { this.bar = bar; this.text = text; this.serializedName = name().toLowerCase(Locale.ROOT); this.label = TextComponentUtil.getString(name().toLowerCase(Locale.ROOT)); }
        @Override public Component getTextComponent() { return label; }
        @Override public String getSerializedName() { return serializedName; }
    }
}
