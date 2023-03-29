package earth.terrarium.stitch.client.models.fabric;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

public interface BlockModelHook {

    void stitch$setModelData(ResourceLocation modelType, JsonObject data);

    ResourceLocation stitch$getModelType();

    JsonObject stitch$getModelData();

    default boolean stitch$hasModelData() {
        return stitch$getModelData() != null && stitch$getModelType() != null;
    }
}