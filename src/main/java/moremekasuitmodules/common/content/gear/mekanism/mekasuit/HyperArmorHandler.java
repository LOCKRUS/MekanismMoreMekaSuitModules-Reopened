package moremekasuitmodules.common.content.gear.mekanism.mekasuit;

import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.common.content.gear.IModuleContainerItem;
import moremekasuitmodules.common.registries.MekaSuitMoreModules;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class HyperArmorHandler {
    private static final double MIN_SPEED_SQUARED = 1.0D;
    private static final Map<UUID, Vec3> PREVIOUS_MOTION = new HashMap<>();
    private static final Map<UUID, Integer> COOLDOWN = new HashMap<>();

    @SubscribeEvent
    public void onPlayerTickPre(PlayerTickEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PREVIOUS_MOTION.put(player.getUUID(), player.getDeltaMovement());
        }
    }

    @SubscribeEvent
    public void onPlayerTickPost(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.horizontalCollision) {
            return;
        }
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof IModuleContainerItem)) {
            return;
        }
        IModule<ModuleHyperArmorUnit> module = IModuleHelper.INSTANCE.getIfEnabled(chest, MekaSuitMoreModules.HYPER_ARMOR_UNIT);
        if (module == null || PREVIOUS_MOTION.getOrDefault(player.getUUID(), Vec3.ZERO).lengthSqr() < MIN_SPEED_SQUARED) {
            return;
        }
        int cooldown = COOLDOWN.getOrDefault(player.getUUID(), 0);
        if (cooldown > 0) {
            COOLDOWN.put(player.getUUID(), cooldown - 1);
            return;
        }
        Vec3 motion = PREVIOUS_MOTION.getOrDefault(player.getUUID(), Vec3.ZERO);
        double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        if (horizontal < 0.05D) {
            return;
        }
        int broken = 0;
        int reach = Math.min(3, Math.max(1, (int) Math.sqrt(PREVIOUS_MOTION.get(player.getUUID()).lengthSqr())));
        for (int distance = 0; distance < reach; distance++) {
            double x = player.getX() + motion.x / horizontal * (0.8D + distance);
            double z = player.getZ() + motion.z / horizontal * (0.8D + distance);
            int minX = (int) Math.floor(x - 0.45D), maxX = (int) Math.floor(x + 0.45D);
            int minY = (int) Math.floor(player.getBoundingBox().minY), maxY = (int) Math.floor(player.getBoundingBox().maxY - 0.01D);
            int minZ = (int) Math.floor(z - 0.45D), maxZ = (int) Math.floor(z + 0.45D);
            for (BlockPos pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
                var state = player.level().getBlockState(pos);
                if (!state.isAir() && state.getDestroySpeed(player.level(), pos) >= 0.0F && player.mayInteract(player.level(), pos) && player.level().destroyBlock(pos, true)) {
                    broken++;
                }
            }
        }
        if (broken > 0) COOLDOWN.put(player.getUUID(), 5);
    }
}
