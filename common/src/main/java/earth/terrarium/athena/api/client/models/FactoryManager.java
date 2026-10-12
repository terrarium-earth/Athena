package earth.terrarium.athena.api.client.models;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import earth.terrarium.athena.api.client.utils.AthenaUnbakedModelLoader;
import earth.terrarium.athena.impl.platform.ModelLoaderService;
import net.minecraft.Optionull;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FactoryManager {

    private static final ModelLoaderService SERVICE = ModelLoaderService.create();
    private static final Map<Identifier, AthenaUnbakedModelLoader> LOADERS = new ConcurrentHashMap<>();
    private static final Codec<AthenaUnbakedModelLoader> CODEC = Identifier.CODEC.comapFlatMap(
            it -> Optionull.mapOrElse(FactoryManager.get(it), DataResult::success, () -> DataResult.error(() -> "Unknown loader: " + it)),
            AthenaUnbakedModelLoader::id
    );

    /**
     * Registers a new model factory, which will be used to create models for the given json.
     * @param id The id of the model factory
     * @param factory The factory to use
     */
    public static void register(Identifier id, AthenaModelType factory) {
        LOADERS.put(id, SERVICE.register(id, factory));
    }

    /**
    * Gets the model loader for the given id.
    * @param id The id of the model loader
    * @return The model loader, or null if no loader is registered for the given id
    */
    public static AthenaUnbakedModelLoader get(Identifier id) {
        return LOADERS.get(id);
    }

    /**
     * Gets all registered model loaders.
     * @return A collection of all registered model loaders
     */
    public static Collection<AthenaUnbakedModelLoader> loaders() {
        return LOADERS.values();
    }

    /**
     * Gets the codec for serializing and deserializing model loaders.
     * @return The codec for model loaders
     */
    public static Codec<AthenaUnbakedModelLoader> codec() {
        return CODEC;
    }
}
