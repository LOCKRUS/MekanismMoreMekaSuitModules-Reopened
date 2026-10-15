package moremekasuitmodules.mixin.minecraft;

import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.common.content.gear.IModuleContainerItem;
import moremekasuitmodules.common.content.gear.mekanism.mekasuit.ModuleHyperArmorUnit;
import moremekasuitmodules.common.registries.MekaSuitMoreModules;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Applies the solid-hitbox option to Player's own isPushable override. */
@Mixin(Player.class)
public abstract class MixinPlayer {
    @Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
    private void moremekasuitmodules$hyperArmorSolidHitbox(CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        ItemStack chest = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof IModuleContainerItem)) {
            return;
        }
        IModule<?> module = IModuleHelper.INSTANCE.getIfEnabled(chest, MekaSuitMoreModules.HYPER_ARMOR_UNIT);
        if (module != null && module.getCustomInstance() instanceof ModuleHyperArmorUnit unit && unit.solidHitbox()) {
            cir.setReturnValue(true);
        }
    }
}
