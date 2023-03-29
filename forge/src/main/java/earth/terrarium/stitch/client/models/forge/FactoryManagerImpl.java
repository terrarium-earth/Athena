package earth.terrarium.stitch.client.models.forge;

import earth.terrarium.stitch.client.forge.StitchModelLoader;
import earth.terrarium.stitch.client.models.StitchModelFactory;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class FactoryManagerImpl {

    private static final Map<ResourceLocation, StitchModelLoader> FACTORIES = new HashMap<>();

    public static void register(ResourceLocation type, StitchModelFactory factory) {
        if (FACTORIES.containsKey(type)) {
            throw new IllegalArgumentException("Factory already registered for type: " + type);
        }
        FACTORIES.put(type, new StitchModelLoader(factory));
    }

    public static StitchModelLoader get(ResourceLocation type) {
        return FACTORIES.get(type);
    }
}
