package moremekasuitmodules.common.registries;

import mekanism.common.recipe.serializer.MekanismRecipeSerializer;
import moremekasuitmodules.common.MoreMekaSuitModules;
import moremekasuitmodules.common.recipe.SolipsismiumNucleosynthesizingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MoreMekaSuitModulesRecipeSerializers {

    private MoreMekaSuitModulesRecipeSerializers() {
    }

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MoreMekaSuitModules.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> SOLIPSISMIUM_NUCLEOSYNTHESIZING = RECIPE_SERIALIZERS.register(
            "solipsismium_nucleosynthesizing",
            () -> MekanismRecipeSerializer.nucleosynthesizing(SolipsismiumNucleosynthesizingRecipe::new)
    );
}
