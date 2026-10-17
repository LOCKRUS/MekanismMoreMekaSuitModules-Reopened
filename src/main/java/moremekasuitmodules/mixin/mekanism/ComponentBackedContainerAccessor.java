package moremekasuitmodules.mixin.mekanism;

import mekanism.common.attachments.containers.ComponentBackedContainer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ComponentBackedContainer.class)
public interface ComponentBackedContainerAccessor {

    @Accessor("attachedTo")
    ItemStack moreMekaSuitModules$getAttachedTo();
}
