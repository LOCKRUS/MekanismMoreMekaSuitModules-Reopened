package moremekasuitmodules.mixin.mekanism;

import moremekasuitmodules.common.AntimatterStrikeActivation;
import moremekasuitmodules.common.registries.MekaSuitMoreModulesItem;
import mekanism.common.inventory.slot.InputInventorySlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "mekanism.common.tile.TileEntityModificationStation")
public abstract class MixinTileEntityModificationStation {
    @Shadow
    private InputInventorySlot moduleSlot;

    @Shadow
    public InputInventorySlot containerSlot;

    @Inject(
            method = "onUpdateServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lmekanism/common/content/gear/ModuleContainer;addModule(Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/Holder;I)I"
            )
    )
    private void moreMekaSuitModules$copyActivationToContainer(CallbackInfoReturnable<Boolean> cir) {
        ItemStack moduleStack = moduleSlot.getStack();
        ItemStack containerStack = containerSlot.getStack();
        if (moduleStack.is(MekaSuitMoreModulesItem.MODULE_MEKA_TOOL_ANTIMATTER_STRIKE.get())
                && AntimatterStrikeActivation.isActive(moduleStack)) {
            AntimatterStrikeActivation.activate(containerStack);
        }
    }

    @ModifyArg(
            method = "removeModule",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"
            ),
            index = 0
    )
    private ItemStack moreMekaSuitModules$copyActivationToRemovedModule(ItemStack moduleStack) {
        if (moduleStack.is(MekaSuitMoreModulesItem.MODULE_MEKA_TOOL_ANTIMATTER_STRIKE.get())
                && AntimatterStrikeActivation.isActive(containerSlot.getStack())) {
            AntimatterStrikeActivation.activate(moduleStack);
        }
        return moduleStack;
    }
}
