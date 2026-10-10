package moremekasuitmodules.mixin.minecraft;

import moremekasuitmodules.common.registries.MekaSuitMoreModules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.common.content.gear.IModuleContainerItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
    private void moremekasuitmodules$hyperArmorHitbox(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (self instanceof Player player) {
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (chest.getItem() instanceof IModuleContainerItem) {
                IModule<?> module = IModuleHelper.INSTANCE.getIfEnabled(chest, MekaSuitMoreModules.HYPER_ARMOR_UNIT);
                if (module != null && module.getCustomInstance() instanceof moremekasuitmodules.common.content.gear.mekanism.mekasuit.ModuleHyperArmorUnit unit && unit.solidHitbox()) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
