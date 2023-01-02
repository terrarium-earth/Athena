package earth.terrarium.modid.fabric;

import earth.terrarium.modid.ModId;
import net.fabricmc.api.ModInitializer;

public class ModIdFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModId.init();
    }
}
