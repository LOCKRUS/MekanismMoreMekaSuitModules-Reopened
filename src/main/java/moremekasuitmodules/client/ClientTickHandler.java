package moremekasuitmodules.client;

import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.item.gear.ItemMekaTool;
import moremekasuitmodules.common.AntimatterStrikeActivation;
import moremekasuitmodules.common.config.MoreModulesConfig;
import moremekasuitmodules.common.content.gear.mekanism.mekatool.ModuleMekaToolFlamethrowerUnit;
import moremekasuitmodules.common.registries.MekaSuitMoreModules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class ClientTickHandler {

    public static Minecraft minecraft = Minecraft.getInstance();
    private static int cameraShakeTicks;
    private static float cameraShakeIntensity;
    private static float recoilPitch;
    private static float recoilYaw;
    private static float recoilRoll;
    private static int recoilTicks;
    private static int steamCoolingTicks;
    private static int steamCoolingTotal;
    private static boolean steamCoolingUltra;
    private static boolean ultraChargeSoundPlayed;
    private static int antimatterCooldownSoundTicks;
    private static AntimatterChargeSoundInstance antimatterChargeSound;


    public static final int ANTIMATTER_CHARGE_DURATION = 72_000;
    public static final int ULTRA_CHARGE_TICKS = 20 * 15;

    public static void triggerCameraShake(int duration, float intensity) {
        cameraShakeTicks = Math.max(cameraShakeTicks, Math.min(300, duration));
        cameraShakeIntensity = Math.max(cameraShakeIntensity, Math.min(0.85F, intensity));
    }

    public static void triggerOrbShot(boolean ultra) {
        triggerCameraShake(ultra ? 14 : 9, ultra ? 0.24F : 0.09F);
        recoilPitch = Math.max(recoilPitch, ultra ? 3.8F : 1.45F);
        recoilYaw = (minecraft.level != null && minecraft.level.random.nextBoolean() ? 1 : -1)
                * (ultra ? 0.78F : 0.28F);
        recoilRoll = (minecraft.level != null && minecraft.level.random.nextBoolean() ? 1 : -1)
                * (ultra ? 0.54F : 0.19F);
        recoilTicks = Math.max(recoilTicks, ultra ? 14 : 9);
        steamCoolingUltra = ultra;
        steamCoolingTotal = ultra ? 32 : 20;
        steamCoolingTicks = Math.max(steamCoolingTicks, steamCoolingTotal);
    }

    @SubscribeEvent
    public void clientTick(ClientTickEvent.Post event) {
        updateAntimatterChargeSound();
        updateAntimatterCooldownSound();
        if (cameraShakeTicks > 0) {
            cameraShakeTicks--;
            // Exponential decay reaches almost zero at the end of the ten-second window.
            cameraShakeIntensity *= 0.965F;
        } else {
            cameraShakeIntensity = 0;
        }
        if (recoilTicks > 0) {
            recoilTicks--;
            recoilPitch *= 0.72F;
            recoilYaw *= 0.70F;
            recoilRoll *= 0.68F;
        } else {
            recoilPitch = 0;
            recoilYaw = 0;
            recoilRoll = 0;
        }
        if (steamCoolingTicks > 0) {
            spawnCoolingSteam();
            steamCoolingTicks--;
        }
        spawnFlamethrowerSteam();
    }

    public static float antimatterChargeProgress(ItemStack stack) {
        LocalPlayer player = minecraft.player;
        if (player == null || !(stack.getItem() instanceof ItemMekaTool)
                || !player.isUsingItem() || player.getUseItem().getItem() != stack.getItem()
                || player.getMainHandItem().getItem() != stack.getItem()
                || !isAntimatterModuleEnabled(stack)) {
            return 0.0F;
        }
        int heldTicks = Math.max(0, ANTIMATTER_CHARGE_DURATION - player.getUseItemRemainingTicks());
        return Math.min(1.0F, heldTicks / (float) ULTRA_CHARGE_TICKS);
    }

    public static boolean isAntimatterChargeReady(ItemStack stack) {
        return antimatterChargeProgress(stack) >= 1.0F;
    }

    private static void updateAntimatterChargeSound() {
        LocalPlayer player = minecraft.player;
        ItemStack stack = player == null ? ItemStack.EMPTY : player.getMainHandItem();
        boolean charging = player != null && isAntimatterCharging(player, stack);
        if (!charging) {
            ultraChargeSoundPlayed = false;
            if (antimatterChargeSound != null) {
                antimatterChargeSound.fadeOut(player == null ? Vec3.ZERO : mekaToolMuzzleOrigin(player));
                if (antimatterChargeSound.isStopped()) {
                    antimatterChargeSound = null;
                }
            }
            return;
        }
        float progress = antimatterChargeProgress(stack);
        Vec3 origin = mekaToolMuzzleOrigin(player);
        if (antimatterChargeSound == null || antimatterChargeSound.isStopped()) {
            antimatterChargeSound = new AntimatterChargeSoundInstance(origin);
            minecraft.getSoundManager().play(antimatterChargeSound);
        }
        antimatterChargeSound.update(origin, progress);
        if (!ultraChargeSoundPlayed && isAntimatterChargeReady(stack)) {
            player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.22F, 1.45F);
            ultraChargeSoundPlayed = true;
        }
    }

    private static boolean isAntimatterCharging(LocalPlayer player, ItemStack stack) {
        return player.isUsingItem() && player.getUseItem().getItem() == stack.getItem()
                && player.getMainHandItem().getItem() == stack.getItem()
                && stack.getItem() instanceof ItemMekaTool && isAntimatterModuleEnabled(stack);
    }

    /** Matches the flamethrower's particle origin: the visible MekaTool muzzle. */
    public static Vec3 mekaToolMuzzleOrigin(LocalPlayer player) {
        return mekaToolMuzzleOrigin(player, player.getViewVector(1.0F).normalize());
    }

    /** A slightly forward offset keeps the first-person charging model clear of the camera. */
    public static Vec3 mekaToolChargeOrigin(LocalPlayer player) {
        Vec3 direction = player.getViewVector(1.0F).normalize();
        return mekaToolMuzzleOrigin(player).add(direction.scale(0.45D));
    }

    private static Vec3 mekaToolMuzzleOrigin(LocalPlayer player, Vec3 direction) {
        return player.getEyePosition().add(direction.scale(0.7D));
    }

    private static void updateAntimatterCooldownSound() {
        LocalPlayer player = minecraft.player;
        ItemStack stack = player == null ? ItemStack.EMPTY : player.getMainHandItem();
        boolean cooling = player != null
                && stack.getItem() instanceof ItemMekaTool
                && isAntimatterModuleEnabled(stack)
                && player.getCooldowns().getCooldownPercent(stack.getItem(), 0.0F) > 0.0F;
        if (!cooling) {
            antimatterCooldownSoundTicks = 0;
            return;
        }
        if (antimatterCooldownSoundTicks > 0) {
            antimatterCooldownSoundTicks--;
            return;
        }
        // Vanilla's lava-extinguish sound is the desired lava-on-water hiss.
        player.playSound(SoundEvents.LAVA_EXTINGUISH, 0.18F, 0.9F);
        antimatterCooldownSoundTicks = 12;
    }

    private static boolean isAntimatterModuleEnabled(ItemStack stack) {
        return stack.getItem() instanceof IModuleContainerItem item
                && AntimatterStrikeActivation.isActive(stack)
                && item.isModuleEnabled(stack, MekaSuitMoreModules.MEKA_TOOL_ANTIMATTER_STRIKE_UNIT);
    }

    @SubscribeEvent
    public void cameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (cameraShakeTicks <= 0 || cameraShakeIntensity <= 0.01F) {
            return;
        }
        float envelope = Math.min(1.0F, cameraShakeTicks / 20.0F);
        float amount = cameraShakeIntensity * envelope;
        long time = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        float yaw = (float) Math.sin(time * 1.91D) * amount;
        float pitch = (float) Math.cos(time * 2.37D) * amount * 0.8F;
        float roll = (float) Math.sin(time * 2.83D) * amount * 0.55F;
        event.setYaw(event.getYaw() + yaw);
        event.setPitch(event.getPitch() + pitch);
        event.setRoll(event.getRoll() + roll);
        event.setPitch(event.getPitch() - recoilPitch);
        event.setYaw(event.getYaw() + recoilYaw);
        event.setRoll(event.getRoll() + recoilRoll);
    }

    private static void spawnCoolingSteam() {
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || !(player.getMainHandItem().getItem() instanceof ItemMekaTool)) {
            return;
        }
        Vec3 direction = player.getViewVector(1.0F).normalize();
        Vec3 origin = player.getEyePosition().add(direction.scale(1.0D)).add(0, 0.16D, 0);
        Vec3 side = direction.cross(new Vec3(0, 1, 0));
        if (side.lengthSqr() < 0.001D) {
            side = new Vec3(1, 0, 0);
        } else {
            side = side.normalize();
        }
        float progress = 1.0F - steamCoolingTicks / (float) steamCoolingTotal;
        double phase = (steamCoolingTotal - steamCoolingTicks) * 0.72D;
        double spiral = Math.sin(phase) * (0.045D + progress * 0.08D);
        Vec3 spawn = origin.add(side.scale(spiral));
        Vec3 velocity = direction.scale(0.022D + progress * 0.04D)
                .add(0, 0.045D + progress * 0.02D, 0)
                .add(side.scale(Math.cos(phase) * 0.012D));
        if (steamCoolingTicks % (steamCoolingUltra ? 2 : 3) == 0) {
            minecraft.level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, spawn.x, spawn.y, spawn.z,
                    velocity.x, velocity.y, velocity.z);
        }
    }

    private static void spawnFlamethrowerSteam() {
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || !player.isUsingItem()
                || player.getUsedItemHand() != net.minecraft.world.InteractionHand.MAIN_HAND
                || !(player.getMainHandItem().getItem() instanceof ItemMekaTool)) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        IModule<ModuleMekaToolFlamethrowerUnit> module = IModuleHelper.INSTANCE.getIfEnabled(
                stack, MekaSuitMoreModules.MEKA_TOOL_FLAMETHROWER_UNIT);
        if (module == null) {
            return;
        }

        ModuleMekaToolFlamethrowerUnit.FlameMode mode = module.getCustomInstance().flameMode();
        int puffs = switch (mode) {
            case COMBAT -> 1;
            case HEAT -> 1;
            case INFERNO -> 2;
        };
        Vec3 direction = player.getViewVector(1.0F).normalize();
        Vec3 origin = mekaToolMuzzleOrigin(player, direction);
        Vec3 side = direction.cross(new Vec3(0, 1, 0));
        if (side.lengthSqr() < 0.001D) {
            side = new Vec3(1, 0, 0);
        } else {
            side = side.normalize();
        }
        long tick = minecraft.level.getGameTime();
        for (int i = 0; i < puffs; i++) {
            double phase = tick * 0.62D + i * 2.1D;
            double curl = Math.sin(phase) * (0.025D + i * 0.01D);
            Vec3 spawn = origin.add(direction.scale(0.04D + i * 0.04D))
                    .add(side.scale(curl))
                    .add(0, 0.012D + i * 0.012D, 0);
            Vec3 velocity = direction.scale(0.025D + i * 0.01D)
                    .add(0, 0.028D + i * 0.01D, 0)
                    .add(side.scale(Math.cos(phase) * 0.009D));
            minecraft.level.addParticle(ParticleTypes.SMOKE,
                    spawn.x, spawn.y, spawn.z, velocity.x, velocity.y, velocity.z);
        }
        minecraft.level.addParticle(ParticleTypes.FLAME, origin.x, origin.y, origin.z,
                direction.x * 0.08D, direction.y * 0.08D, direction.z * 0.08D);
    }

    @SubscribeEvent
    public void GuiScreenEvent(ScreenEvent.Opening event) {
        if (MoreModulesConfig.config.isLoaded() &&!MoreModulesConfig.config.mekaSuitOverloadProtection.get()) {
            return;
        }
        if (event.getNewScreen() instanceof DeathScreen) {
            if (minecraft.player instanceof LocalPlayer) {
                ItemStack head = minecraft.player.getItemBySlot(EquipmentSlot.HEAD);
                if (!minecraft.player.isAlive()) {
                    if (head.getItem() instanceof IModuleContainerItem item) {
                        if (item.isModuleEnabled(head, MekaSuitMoreModules.EMERGENCY_RESCUE_UNIT) || item.isModuleEnabled(head, MekaSuitMoreModules.ADVANCED_INTERCEPTION_SYSTEM_UNIT) || item.hasModule(head, MekaSuitMoreModules.INFINITE_INTERCEPTION_AND_RESCUE_SYSTEM_UNIT)) {
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }
    }
}
