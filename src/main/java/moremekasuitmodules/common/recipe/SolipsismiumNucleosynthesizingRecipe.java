package moremekasuitmodules.common.recipe;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.basic.BasicNucleosynthesizingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Nucleosynthesis recipe for Solipsismium-296 with a 50% uranium fallback.
 * The random roll is made when the machine requests the completed output.
 */
public class SolipsismiumNucleosynthesizingRecipe extends BasicNucleosynthesizingRecipe {

    private static final ResourceLocation URANIUM_INGOT =
            ResourceLocation.fromNamespaceAndPath("mekanism", "ingot_uranium");
    private final ItemStack uraniumOutput;

    public SolipsismiumNucleosynthesizingRecipe(ItemStackIngredient itemInput,
                                                 ChemicalStackIngredient chemicalInput,
                                                 ItemStack solipsismiumOutput,
                                                 int duration,
                                                 boolean perTickUsage) {
        super(itemInput, chemicalInput, solipsismiumOutput, duration, perTickUsage);
        this.uraniumOutput = new ItemStack(BuiltInRegistries.ITEM.get(URANIUM_INGOT));
    }

    @Override
    public ItemStack getOutput(ItemStack input, ChemicalStack chemicalInput) {
        return ThreadLocalRandom.current().nextBoolean() ? super.getOutput(input, chemicalInput) : uraniumOutput.copy();
    }

    @Override
    public List<ItemStack> getOutputDefinition() {
        return List.of(getOutputRaw(), uraniumOutput);
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return getOutputRaw().copy();
    }
}
