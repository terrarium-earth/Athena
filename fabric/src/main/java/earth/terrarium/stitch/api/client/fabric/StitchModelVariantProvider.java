package earth.terrarium.stitch.api.client.fabric;

import com.google.gson.JsonObject;
import earth.terrarium.stitch.api.client.models.StitchModelFactory;
import earth.terrarium.stitch.api.client.models.fabric.FactoryManagerImpl;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class StitchModelVariantProvider implements ModelVariantProvider {

    private final ResourceLocation id;
    private final StitchModelFactory factory;

    public StitchModelVariantProvider(ResourceLocation id, StitchModelFactory factory) {
        this.id = id;
        this.factory = factory;
    }

    @Override
    public @Nullable UnbakedModel loadModelVariant(ModelResourceLocation modelId, ModelProviderContext context) {
        if ("inventory".equals(modelId.getVariant())) return null;
        JsonObject json = FactoryManagerImpl.getData(this.id, modelId);
        if (json != null) {
            return new StitchUnbakedModel(this.factory.create(json));
        }
        return null;
    }
}
