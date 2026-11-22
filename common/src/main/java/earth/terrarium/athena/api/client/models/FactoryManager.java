package earth.terrarium.athena.api.client.models;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import earth.terrarium.athena.api.client.utils.AthenaUnbakedModelLoader;
import net.minecraft.Optionull;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FactoryManager {

    private static final Map<Identifier, AthenaModelType> TYPES = new ConcurrentHashMap<>();
    private static final Map<AthenaModelType, Identifier> TYPE_IDS = new ConcurrentHashMap<>();

    private static final Map<AthenaModelType, AthenaUnbakedModelLoader> LOADERS = new ConcurrentHashMap<>();

    private static final Codec<AthenaUnbakedModelLoader> CODEC = Identifier.CODEC.comapFlatMap(
        it -> Optionull.mapOrElse(FactoryManager.get(it), DataResult::success, () -> DataResult.error(() -> "Unknown loader: " + it)),
        AthenaUnbakedModelLoader::id
    );

    private static AthenaUnbakedModelLoader unbakedModelLoader(AthenaModelType modelType) {
        return LOADERS.computeIfAbsent(
            modelType,
            (type) -> new AthenaUnbakedModelLoader(null, type, null)
        );
    }

    /**
     * Registers a new model factory, which will be used to create models for the given json.
     * @param id The id of the model type
     * @param type The model type to register
     */
    public static void register(Identifier id, AthenaModelType type) {
        TYPES.put(id, type);
        TYPE_IDS.put(type, id);
    }

    /**
    * Gets the model loader for the given id.
    * @param id The id of the model loader
    * @return The model loader, or null if no loader is registered for the given id
    */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "26.4")
    public static AthenaUnbakedModelLoader get(Identifier id) {
        return unbakedModelLoader(TYPES.get(id));
    }

    /**
     * Gets all registered model loaders.
     * @return A collection of all registered model loaders
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "26.4")
    public static Collection<AthenaUnbakedModelLoader> loaders() {
        return TYPES.values().stream().map(FactoryManager::unbakedModelLoader).toList();
    }

    /**
     * Gets the codec for serializing and deserializing model loaders.
     * @return The codec for model loaders
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "26.4")
    public static Codec<AthenaUnbakedModelLoader> codec() {
        return CODEC;
    }

    @ApiStatus.Internal
    public static AthenaModelType getType(Identifier id) {
        return TYPES.get(id);
    }

    public static Identifier getTypeId(AthenaModelType type) {
        return TYPE_IDS.get(type);
    }
}
