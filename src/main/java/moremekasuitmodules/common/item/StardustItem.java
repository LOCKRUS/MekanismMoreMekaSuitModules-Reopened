package moremekasuitmodules.common.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Stardust is condensed Nether Star material and permanently keeps its star-like glint.
 */
public class StardustItem extends Item {
    public StardustItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
