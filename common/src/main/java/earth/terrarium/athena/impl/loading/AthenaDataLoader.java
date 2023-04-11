package earth.terrarium.athena.impl.loading;

import com.google.gson.JsonObject;
import earth.terrarium.athena.impl.client.DefaultModels;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.List;
import java.util.function.Function;

public class AthenaDataLoader {

    private static Function<ResourceLocation, List<ModelBakery.LoadedJson>> getter;

    public static void setGetter(Function<ResourceLocation, List<ModelBakery.LoadedJson>> getter) {
        AthenaDataLoader.getter = getter;
    }

    public static JsonObject getData(ResourceLocation modelType, ResourceLocation modelId) {
        if (getter == null) return null;
        List<ModelBakery.LoadedJson> jsons = getter.apply(convertModelIdToBlockStatePath(modelId));
        if (jsons == null) return null;
        for (ModelBakery.LoadedJson json : jsons) {
            if (json.data() instanceof JsonObject object) {
                String type = GsonHelper.getAsString(object, DefaultModels.MODID + ":loader", "");
                if (modelType.toString().equals(type)) {
                    return object;
                }
            }
        }
        return null;
    }

    private static ResourceLocation convertModelIdToBlockStatePath(ResourceLocation modelId) {
        return new ResourceLocation(modelId.getNamespace(), "blockstates/" + modelId.getPath() + ".json");
    }
}
