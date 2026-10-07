package moremekasuitmodules.client;

import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.item.gear.ItemMekaTool;
import moremekasuitmodules.common.config.MoreModulesConfig;
import moremekasuitmodules.common.registries.MekaSuitMoreModules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class ClientTickHandler {

    public static Minecraft minecraft = Minecraft.getInstance();
    private static int cameraShakeTicks;
    private static float cameraShakeIntensity;
    private static boolean ultraChargeSoundPlayed;

    public static final int ANTIMATTER_CHARGE_DURATION = 72_000;
    public static final int ULTRA_CHARGE_TICKS = 20 * 15;

    public static void triggerCameraShake(int duration, float intensity) {
        cameraShakeTicks = Math.max(cameraShakeTicks, Math.min(200, duration));
        cameraShakeIntensity = Math.max(cameraShakeIntensity, Math.min(0.85F, intensity));
    }

    @SubscribeEvent
    public void clientTick(ClientTickEvent.Post event) {
        updateAntimatterChargeSound();
        if (cameraShakeTicks > 0) {
            cameraShakeTicks--;
            // Exponential decay reaches almost zero at the end of the ten-second window.
            cameraShakeIntensity *= 0.965F;
        } else {
            cameraShakeIntensity = 0;
        }
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
        boolean charging = player != null && player.isUsingItem() && player.getUseItem().getItem() == stack.getItem()
                && player.getMainHandItem().getItem() == stack.getItem()
                && stack.getItem() instanceof ItemMekaTool && isAntimatterModuleEnabled(stack);
        if (!charging) {
            ultraChargeSoundPlayed = false;
            return;
        }
        if (!ultraChargeSoundPlayed && isAntimatterChargeReady(stack)) {
            player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.22F, 1.45F);
            ultraChargeSoundPlayed = true;
        }
    }

    private static boolean isAntimatterModuleEnabled(ItemStack stack) {
        return stack.getItem() instanceof IModuleContainerItem item
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
