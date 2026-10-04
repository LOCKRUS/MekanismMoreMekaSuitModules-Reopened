package moremekasuitmodules.common.content.gear.mekanism.mekasuit;

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
public record ModuleImpactWaveUnit(TriggerHeight triggerHeight, ImpactRadius radius, DamageScale damageScale)
        implements ICustomModule<ModuleImpactWaveUnit> {
    public static final int MAX_MODULES = 4;
    public static final ResourceLocation TRIGGER_HEIGHT = MoreMekaSuitModules.rl("impact_trigger_height");
    public static final ResourceLocation RADIUS = MoreMekaSuitModules.rl("impact_radius");
    public static final ResourceLocation DAMAGE = MoreMekaSuitModules.rl("impact_damage");

    public ModuleImpactWaveUnit(IModule<ModuleImpactWaveUnit> module) {
        this(module.<TriggerHeight>getConfigOrThrow(TRIGGER_HEIGHT).get(),
                module.<ImpactRadius>getConfigOrThrow(RADIUS).get(),
                module.<DamageScale>getConfigOrThrow(DAMAGE).get());
    }

    public float getTriggerHeight() { return triggerHeight.blocks; }
    public float getRadius() { return radius.blocks; }
    public float getDamageMultiplier() { return damageScale.multiplier; }

    @NothingNullByDefault
    public enum TriggerHeight implements IHasTextComponent, StringRepresentable {
        LOW(20), MEDIUM(15), HIGH(10), ULTRA(5);
        public static final Codec<TriggerHeight> CODEC = StringRepresentable.fromEnum(TriggerHeight::values);
        public static final IntFunction<TriggerHeight> BY_ID = ByIdMap.continuous(TriggerHeight::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, TriggerHeight> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, TriggerHeight::ordinal);
        private final float blocks; private final String name; private final Component label;
        TriggerHeight(float blocks) { this.blocks = blocks; this.name = name().toLowerCase(Locale.ROOT); this.label = TextComponentUtil.getString(Integer.toString((int) blocks)); }
        @Override public Component getTextComponent() { return label; }
        @Override public String getSerializedName() { return name; }
    }

    @NothingNullByDefault
    public enum ImpactRadius implements IHasTextComponent, StringRepresentable {
        LOW(5), MEDIUM(10), HIGH(15), ULTRA(20);
        public static final Codec<ImpactRadius> CODEC = StringRepresentable.fromEnum(ImpactRadius::values);
        public static final IntFunction<ImpactRadius> BY_ID = ByIdMap.continuous(ImpactRadius::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, ImpactRadius> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ImpactRadius::ordinal);
        private final float blocks; private final String name; private final Component label;
        ImpactRadius(float blocks) { this.blocks = blocks; this.name = name().toLowerCase(Locale.ROOT); this.label = TextComponentUtil.getString(Integer.toString((int) blocks)); }
        @Override public Component getTextComponent() { return label; }
        @Override public String getSerializedName() { return name; }
    }

    @NothingNullByDefault
    public enum DamageScale implements IHasTextComponent, StringRepresentable {
        LOW(0.45F), MEDIUM(0.65F), HIGH(0.85F), ULTRA(1.05F);
        public static final Codec<DamageScale> CODEC = StringRepresentable.fromEnum(DamageScale::values);
        public static final IntFunction<DamageScale> BY_ID = ByIdMap.continuous(DamageScale::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, DamageScale> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, DamageScale::ordinal);
        private final float multiplier; private final String name; private final Component label;
        DamageScale(float multiplier) { this.multiplier = multiplier; this.name = name().toLowerCase(Locale.ROOT); this.label = TextComponentUtil.getString(Float.toString(multiplier)); }
        @Override public Component getTextComponent() { return label; }
        @Override public String getSerializedName() { return name; }
    }
}
