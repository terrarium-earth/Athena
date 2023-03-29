package earth.terrarium.stitch.client.models;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.resources.ResourceLocation;

public class FactoryManager {

    /**
     * Registers a new model factory, which will be used to create models for the given json.
     * @param id The id of the model factory
     * @param factory The factory to use
     */
    @ExpectPlatform
    public static void register(ResourceLocation id, StitchModelFactory factory) {
        throw new AssertionError();
    }
}
