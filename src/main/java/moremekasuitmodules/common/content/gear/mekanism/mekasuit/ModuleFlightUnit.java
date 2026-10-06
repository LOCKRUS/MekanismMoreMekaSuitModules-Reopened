package moremekasuitmodules.common.content.gear.mekanism.mekasuit;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.IntFunction;

@ParametersAreNotNullByDefault
public record ModuleFlightUnit(FlightLevel level) implements ICustomModule<ModuleFlightUnit> {
    public static final int MAX_MODULES = 4;
    public static final ResourceLocation FLIGHT_LEVEL = ResourceLocation.fromNamespaceAndPath("moremekasuitmodules", "flight_level");
    private static final long BASE_ENERGY_PER_TICK = 7_500L;
    private static final Set<UUID> ACTIVE_GLIDE = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public ModuleFlightUnit(IModule<ModuleFlightUnit> module) {
        this(module.<FlightLevel>getConfigOrThrow(FLIGHT_LEVEL).get());
    }

    @Override
    public void tickServer(IModule<ModuleFlightUnit> module, IModuleContainer container, ItemStack stack, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || !module.isEnabled() || player.isSpectator()) {
            return;
        }
        // Takeoff follows the Jet Suit model: leave the ground, then apply a
        // small camera-directed impulse every tick instead of teleporting the
        // player to a fixed speed or granting creative flight.
        if (player.onGround() || player.isCrouching()) {
            stopGlide(serverPlayer);
            return;
        }
        long usage = Math.round(BASE_ENERGY_PER_TICK * level.energyMultiplier);
        if (!module.canUseEnergy(player, stack, usage, false)) {
            disableFlight(serverPlayer);
            return;
        }
        module.useEnergy(player, stack, usage);
        propel(serverPlayer);
    }

    @Override
    public void onRemoved(IModule<ModuleFlightUnit> module, IModuleContainer container, ItemStack stack, boolean duringRemoval) {
        // The next player tick also calls cleanup when the chest item is gone.
        // Keeping this callback side-effect free avoids changing the module container
        // while Mekanism is removing an installed module.
    }

    private void propel(ServerPlayer player) {
        if (!ACTIVE_GLIDE.contains(player.getUUID())) {
            ACTIVE_GLIDE.add(player.getUUID());
        }
        // Vanilla normally cancels fall flying when no Elytra is equipped. Reassert
        // the state every tick because this module intentionally has no Elytra item.
        if (!player.isFallFlying()) {
            player.startFallFlying();
        }
        double acceleration = 0.075D * level.speedMultiplier;
        double maxSpeed = 2.0D * level.speedMultiplier;
        var movement = player.getDeltaMovement().add(player.getLookAngle().normalize().scale(acceleration));
        if (movement.lengthSqr() > maxSpeed * maxSpeed) {
            movement = movement.normalize().scale(maxSpeed);
        }
        player.setDeltaMovement(movement);
        player.fallDistance = Math.max(player.fallDistance / 1.5F, 0.0F);
    }

    private static void stopGlide(ServerPlayer player) {
        if (ACTIVE_GLIDE.remove(player.getUUID())) {
            player.stopFallFlying();
            player.setNoGravity(false);
            player.fallDistance = 0;
        }
    }

    private void disableFlight(ServerPlayer player) {
        stopGlide(player);
    }

    public static void cleanup(ServerPlayer player) {
        stopGlide(player);
    }

    @NothingNullByDefault
    public enum FlightLevel implements IHasTextComponent, StringRepresentable {
        // Ad Astra-style impulse and top speed multipliers. More modules add
        // acceleration instead of replacing the player's velocity each tick.
        ONE(1.0F, 1.0D, 1.0D),
        TWO(1.333F, 1.333D, 1.333D),
        THREE(1.667F, 1.667D, 1.667D),
        FOUR(2.0F, 2.0D, 2.0D);

        public static final Codec<FlightLevel> CODEC = StringRepresentable.fromEnum(FlightLevel::values);
        public static final IntFunction<FlightLevel> BY_ID = ByIdMap.continuous(FlightLevel::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, FlightLevel> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, FlightLevel::ordinal);
        private final double speedMultiplier;
        private final double energyMultiplier;
        private final String serializedName;
        private final Component label;

        FlightLevel(float multiplier, double speedMultiplier, double energyMultiplier) {
            this.speedMultiplier = speedMultiplier;
            this.energyMultiplier = energyMultiplier;
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.label = TextComponentUtil.getString("x" + (multiplier == 1.333F ? "1.33" : multiplier == 1.667F ? "1.67" : multiplier == 1.0F ? "1" : "2"));
        }

        @Override
        public Component getTextComponent() { return label; }

        @Override
        public String getSerializedName() { return serializedName; }
    }
}
