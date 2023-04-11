package earth.terrarium.athena.api.client.fabric;

import com.google.gson.JsonObject;
import earth.terrarium.athena.api.client.models.AthenaModelFactory;
import earth.terrarium.athena.impl.loading.AthenaDataLoader;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class AthenaModelVariantProvider implements ModelVariantProvider {

    private final ResourceLocation id;
    private final AthenaModelFactory factory;

    public AthenaModelVariantProvider(ResourceLocation id, AthenaModelFactory factory) {
        this.id = id;
        this.factory = factory;
    }

    @Override
    public @Nullable UnbakedModel loadModelVariant(ModelResourceLocation modelId, ModelProviderContext context) {
        if ("inventory".equals(modelId.getVariant())) return null;
        JsonObject json = AthenaDataLoader.getData(this.id, modelId);
        if (json != null) {
            return new AthenaUnbakedModel(this.factory.create(json));
        }
        return null;
    }
}
