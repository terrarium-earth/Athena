package earth.terrarium.athena.neoforge.client;

import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.client.event.ModelEvent;

public class AthenaNeoForgeClient {

    public static void init() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(AthenaNeoForgeClient::onRegisterGeometryLoaders);
    }

    public static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register("athena", new AthenaGeometryLoader());
    }
}