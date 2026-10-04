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
public record ModuleWallClingUnit(ClimbSpeed climbSpeed) implements ICustomModule<ModuleWallClingUnit> {
    public static final int MAX_MODULES = 3;
    public static final ResourceLocation CLIMB_SPEED = MoreMekaSuitModules.rl("climb_speed");

    public ModuleWallClingUnit(IModule<ModuleWallClingUnit> module) {
        this(module.<ClimbSpeed>getConfigOrThrow(CLIMB_SPEED).get());
    }

    @Override
    public void tickServer(IModule<ModuleWallClingUnit> module, IModuleContainer container, ItemStack stack, Player player) {
        if (!module.isEnabled() || !canFunction(player) || !isTouchingWall(player)) {
            return;
        }
        boolean climbing = player.getAbilities().flying || player.isCrouching();
        double usage;
        if (player.getDeltaMovement().y > 0.0D && !player.isCrouching()) {
            usage = 250.0D * climbSpeed.energyMultiplier;
            player.setDeltaMovement(player.getDeltaMovement().x, Math.max(player.getDeltaMovement().y,
                    climbSpeed.verticalSpeed), player.getDeltaMovement().z);
        } else if (player.isCrouching()) {
            usage = 80.0D * climbSpeed.energyMultiplier;
            player.setDeltaMovement(player.getDeltaMovement().x, 0.0D, player.getDeltaMovement().z);
        } else {
            usage = 60.0D * climbSpeed.energyMultiplier;
            if (player.getDeltaMovement().y < -0.18D) {
                player.setDeltaMovement(player.getDeltaMovement().x, -0.18D, player.getDeltaMovement().z);
            }
        }
        if (module.canUseEnergy(player, stack, (long) usage, false)) {
            module.useEnergy(player, stack, (long) usage);
            player.fallDistance = 0;
        }
    }

    private static boolean canFunction(Player player) {
        return player.isAlive() && !player.onGround() && !player.isSpectator()
                && !player.getAbilities().flying && !player.isFallFlying()
                && !player.isPassenger() && !player.onClimbable()
                && !player.isInWater() && !player.isInLava();
    }

    private static boolean isTouchingWall(Player player) {
        if (player.horizontalCollision) {
            return true;
        }
        var box = player.getBoundingBox();
        return player.level().getBlockCollisions(player, box.inflate(0.04D, 0.0D, 0.0D)).iterator().hasNext();
    }

    @Override
    public void addHUDStrings(IModule<ModuleWallClingUnit> module, IModuleContainer container, ItemStack stack,
                              Player player, java.util.function.Consumer<Component> hudStringAdder) {
        if (module.isEnabled()) {
            hudStringAdder.accept(Component.translatable("module.moremekasuitmodules.climb_speed_hud", climbSpeed.getTextComponent()));
        }
    }

    @NothingNullByDefault
    public enum ClimbSpeed implements IHasTextComponent, StringRepresentable {
        LOW(0.14D, 1.0D), MEDIUM(0.20D, 1.5D), HIGH(0.28D, 2.25D);
        public static final Codec<ClimbSpeed> CODEC = StringRepresentable.fromEnum(ClimbSpeed::values);
        public static final IntFunction<ClimbSpeed> BY_ID = ByIdMap.continuous(ClimbSpeed::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, ClimbSpeed> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ClimbSpeed::ordinal);
        private final double verticalSpeed;
        private final double energyMultiplier;
        private final String serializedName;
        private final Component label;
        ClimbSpeed(double verticalSpeed, double energyMultiplier) {
            this.verticalSpeed = verticalSpeed;
            this.energyMultiplier = energyMultiplier;
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.label = TextComponentUtil.getString(Double.toString(verticalSpeed));
        }
        @Override public Component getTextComponent() { return label; }
        @Override public String getSerializedName() { return serializedName; }
    }
}
