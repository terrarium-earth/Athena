package earth.terrarium.athena.neoforge;

import earth.terrarium.athena.impl.client.DefaultModels;
import earth.terrarium.athena.neoforge.client.AthenaNeoForgeClient;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;

@Mod("athena")
public class AthenaNeoForge {

    public AthenaNeoForge() {
        if (FMLLoader.getDist().isClient()) {
            DefaultModels.init();
            AthenaNeoForgeClient.init();
        }
    }
}
