package earth.terrarium.athena.neoforge.client;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ModelEvent;

public class AthenaNeoForgeClient {

    public static void init(IEventBus mobEventBus) {
        mobEventBus.addListener(AthenaNeoForgeClient::onRegisterGeometryLoaders);
    }

    public static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(ResourceLocation.fromNamespaceAndPath("athena", "athena"), new AthenaGeometryLoader());
    }
}