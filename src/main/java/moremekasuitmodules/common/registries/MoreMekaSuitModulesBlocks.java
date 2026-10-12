package moremekasuitmodules.common.registries;

import moremekasuitmodules.common.MoreMekaModulesReopen;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;

public final class MoreMekaSuitModulesBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, MoreMekaModulesReopen.MODID);

    public static final DeferredHolder<Block, Block> BURNT_GRASS = BLOCKS.register(
            "burnt_grass", () -> new Block(Block.Properties.of()
                    .strength(0.6F)
                    .sound(SoundType.GRASS)));

    public static final DeferredHolder<Block, Block> CHARRED_WOOD = BLOCKS.register(
            "charred_wood", () -> new Block(Block.Properties.of()
                    .strength(2.0F)
                    .sound(SoundType.WOOD)));

    private MoreMekaSuitModulesBlocks() {
    }
}
