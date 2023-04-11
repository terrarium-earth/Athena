package earth.terrarium.athena.api.client.forge;

import com.google.gson.JsonObject;
import earth.terrarium.athena.api.client.models.AthenaModelFactory;
import earth.terrarium.athena.impl.loading.AthenaDataLoader;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class AthenaUnbakedModelLoader {

    private final ResourceLocation id;
    private final AthenaModelFactory factory;

    public AthenaUnbakedModelLoader(ResourceLocation id, AthenaModelFactory factory) {
        this.id = id;
        this.factory = factory;
    }

    public @Nullable UnbakedModel loadModel(ModelResourceLocation modelId) {
        if ("inventory".equals(modelId.getVariant())) return null;
        JsonObject json = AthenaDataLoader.getData(this.id, modelId);
        if (json != null) {
            return new AthenaUnbakedModel(this.factory.create(json));
        }
        return null;
    }
}
