package earth.terrarium.stitch.fabric;

import earth.terrarium.stitch.Stitch;
import net.fabricmc.api.ModInitializer;

public class StitchFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Stitch.init();
    }
}
