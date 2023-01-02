package earth.terrarium.modid.client.fabric;

import earth.terrarium.modid.client.ModIdClient;
import net.fabricmc.api.ClientModInitializer;

public class ModIdClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModIdClient.init();
    }
}
