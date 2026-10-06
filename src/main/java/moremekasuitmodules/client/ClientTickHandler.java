package moremekasuitmodules.client;

import mekanism.common.content.gear.IModuleContainerItem;
import moremekasuitmodules.common.config.MoreModulesConfig;
import moremekasuitmodules.common.registries.MekaSuitMoreModules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.player.LocalPlayer;
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

    public static void triggerCameraShake(int duration, float intensity) {
        cameraShakeTicks = Math.max(cameraShakeTicks, Math.min(240, duration));
        cameraShakeIntensity = Math.max(cameraShakeIntensity, Math.min(3.0F, intensity));
    }

    @SubscribeEvent
    public void clientTick(ClientTickEvent.Post event) {
        if (cameraShakeTicks > 0) {
            cameraShakeTicks--;
            cameraShakeIntensity *= 0.985F;
        } else {
            cameraShakeIntensity = 0;
        }
    }

    @SubscribeEvent
    public void cameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (cameraShakeTicks <= 0 || cameraShakeIntensity <= 0.01F) {
            return;
        }
        float envelope = Math.min(1.0F, cameraShakeTicks / 12.0F);
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
