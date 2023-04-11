package earth.terrarium.athena.api.client.models.fabric;

import earth.terrarium.athena.api.client.fabric.AthenaModelVariantProvider;
import earth.terrarium.athena.api.client.models.AthenaModelFactory;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.resources.ResourceLocation;

public class FactoryManagerImpl {

    public static void register(ResourceLocation type, AthenaModelFactory factory) {
        ModelLoadingRegistry.INSTANCE.registerVariantProvider(manager -> new AthenaModelVariantProvider(type, factory));
    }
}
