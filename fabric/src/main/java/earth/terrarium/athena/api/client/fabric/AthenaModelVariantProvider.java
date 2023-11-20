package earth.terrarium.athena.api.client.fabric;

import earth.terrarium.athena.api.client.models.AthenaModelFactory;
import earth.terrarium.athena.api.client.utils.AthenaUnbakedModelLoader;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class AthenaModelVariantProvider implements ModelVariantProvider {

    private final AthenaUnbakedModelLoader loader;

    public AthenaModelVariantProvider(ResourceLocation id, AthenaModelFactory factory) {
        this.loader = new AthenaUnbakedModelLoader(id, factory, AthenaUnbakedModel::new);
    }

    @Override
    public @Nullable UnbakedModel loadModelVariant(ModelResourceLocation modelId, ModelProviderContext context) {
        return this.loader.loadModel(modelId);
    }
}
