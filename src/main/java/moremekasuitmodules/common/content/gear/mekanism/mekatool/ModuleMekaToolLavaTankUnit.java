package moremekasuitmodules.common.content.gear.mekanism.mekatool;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;

@ParametersAreNotNullByDefault
public record ModuleMekaToolLavaTankUnit() implements ICustomModule<ModuleMekaToolLavaTankUnit> {
    public ModuleMekaToolLavaTankUnit(IModule<ModuleMekaToolLavaTankUnit> module) {
        this();
    }
}
