package moremekasuitmodules.common;

import moremekasuitmodules.common.registries.MekaSuitMoreModulesItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

public final class AntimatterStrikeActivationHandler {
    @SubscribeEvent
    public void onExplosion(ExplosionEvent.Detonate event) {
        double activationRadius = Math.max(2.0D, event.getExplosion().radius() + 1.0D);
        AABB searchBox = new AABB(event.getExplosion().center(), event.getExplosion().center())
                .inflate(activationRadius);

        for (ItemEntity itemEntity : event.getLevel().getEntitiesOfClass(ItemEntity.class, searchBox)) {
            ItemStack stack = itemEntity.getItem();
            if (!stack.is(MekaSuitMoreModulesItem.MODULE_MEKA_TOOL_ANTIMATTER_STRIKE.get())
                    || AntimatterStrikeActivation.isActive(stack)) {
                continue;
            }
            AntimatterStrikeActivation.activate(stack);
            itemEntity.setItem(stack);
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(event.getLevel());
            if (lightning != null) {
                lightning.setVisualOnly(true);
                lightning.setPos(itemEntity.getX(), itemEntity.getY(), itemEntity.getZ());
                event.getLevel().addFreshEntity(lightning);
            }
            // The module survives the activating explosion, regardless of its power.
            event.getAffectedEntities().remove(itemEntity);
        }
    }
}
