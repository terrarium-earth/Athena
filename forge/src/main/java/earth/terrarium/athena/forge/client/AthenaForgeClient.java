package earth.terrarium.athena.forge.client;

import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class AthenaForgeClient {

    public static void init() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(AthenaForgeClient::onRegisterGeometryLoaders);
    }

    public static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register("athena", new AthenaGeometryLoader());
    }
}
