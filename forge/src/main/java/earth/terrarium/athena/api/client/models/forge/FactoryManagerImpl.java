package earth.terrarium.athena.api.client.models.forge;

import earth.terrarium.athena.api.client.forge.AthenaUnbakedModelLoader;
import earth.terrarium.athena.api.client.models.AthenaModelFactory;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FactoryManagerImpl {

    private static final Map<ResourceLocation, AthenaUnbakedModelLoader> FACTORIES = new HashMap<>();

    public static void register(ResourceLocation type, AthenaModelFactory factory) {
        if (FACTORIES.containsKey(type)) {
            throw new IllegalArgumentException("Factory already registered for type: " + type);
        }
        FACTORIES.put(type, new AthenaUnbakedModelLoader(type, factory));
    }

    public static AthenaUnbakedModelLoader get(ResourceLocation type) {
        return FACTORIES.get(type);
    }

    public static Collection<ResourceLocation> getTypes() {
        return FACTORIES.keySet();
    }
}
