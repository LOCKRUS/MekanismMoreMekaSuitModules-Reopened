package moremekasuitmodules.common.content.gear.mekanism.mekatool;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.common.item.gear.ItemMekaTool;
import mekanism.common.util.StorageUtils;
import moremekasuitmodules.common.registries.MekaSuitMoreModules;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class MekaToolBlasterHandler {
    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof ItemMekaTool)) {
            return;
        }

        IModule<ModuleMekaToolBlasterUnit> module = IModuleHelper.INSTANCE.getIfEnabled(stack, MekaSuitMoreModules.MEKA_TOOL_BLASTER_UNIT);
        if (module == null) {
            return;
        }
        ModuleMekaToolBlasterUnit.FireMode mode = module.getCustomInstance().fireMode();
        if (player.getCooldowns().isOnCooldown(stack.getItem())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.CONSUME);
            return;
        }

        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        if (energyContainer == null || energyContainer.extract(mode.getEnergyCost(), Action.SIMULATE, AutomationType.MANUAL) < mode.getEnergyCost()) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }

        player.getCooldowns().addCooldown(stack.getItem(), mode.getCooldownTicks());
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.CONSUME);
        if (player.level().isClientSide()) {
            return;
        }

        energyContainer.extract(mode.getEnergyCost(), Action.EXECUTE, AutomationType.MANUAL);
        Level level = player.level();
        Vec3 direction = player.getViewVector(1.0F).normalize();
        MekaToolBlasterFireball fireball = new MekaToolBlasterFireball(level, player, direction, mode.getExplosionPower());
        Vec3 origin = player.getEyePosition().add(direction.scale(0.7));
        fireball.setPos(origin.x, origin.y, origin.z);
        level.addFreshEntity(fireball);
    }
}
