package earth.terrarium.athena.impl.loading;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import earth.terrarium.athena.impl.client.DefaultModels;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class AthenaResourceLoader {

    private static Function<ResourceLocation, List<BlockStateModelLoader.LoadedJson>> getter = id -> null;
    private static final Map<ResourceLocation, JsonElement> data = new ConcurrentHashMap<>();

    public static void setGetter(Function<ResourceLocation, List<BlockStateModelLoader.LoadedJson>> getter) {
        AthenaResourceLoader.getter = Objects.requireNonNullElse(getter, id -> null);
    }

    public static void reload(ResourceManager manager) {
        AthenaResourceLoader.data.clear();
        SimpleJsonResourceReloadListener.scanDirectory(manager, "athena", new Gson(), AthenaResourceLoader.data);
    }

    public static JsonObject getData(ResourceLocation modelType, ResourceLocation modelId) {
        var modelData = AthenaResourceLoader.data.get(modelId);
        if (modelData != null) {
            return checkObject(modelType, modelData);
        }
        List<BlockStateModelLoader.LoadedJson> jsons = AthenaResourceLoader.getter.apply(convertModelIdToBlockStatePath(modelId));
        if (jsons == null) return null;
        for (BlockStateModelLoader.LoadedJson json : jsons) {
            JsonObject object = checkObject(modelType, json.data());
            if (object != null) {
                return object;
            }
        }
        return null;
    }

    private static JsonObject checkObject(ResourceLocation modelType, JsonElement data) {
        if (data instanceof JsonObject object) {
            String type = GsonHelper.getAsString(object, DefaultModels.MODID + ":loader", "");
            if (modelType.toString().equals(type)) {
                return object;
            }
        }
        return null;
    }

    private static ResourceLocation convertModelIdToBlockStatePath(ResourceLocation modelId) {
        return ResourceLocation.fromNamespaceAndPath(modelId.getNamespace(), "blockstates/" + modelId.getPath() + ".json");
    }
}
