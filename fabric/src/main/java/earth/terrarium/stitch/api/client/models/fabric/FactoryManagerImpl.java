package earth.terrarium.stitch.api.client.models.fabric;

import com.google.gson.JsonObject;
import earth.terrarium.stitch.api.client.fabric.StitchModelVariantProvider;
import earth.terrarium.stitch.api.client.models.StitchModelFactory;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class FactoryManagerImpl {

    private static final Map<ResourceLocation, Map<ResourceLocation, JsonObject>> MODEL_RAW_DATA = new HashMap<>();
    private static Function<ResourceLocation, List<ModelBakery.LoadedJson>> getter;

    public static void register(ResourceLocation type, StitchModelFactory factory) {
        if (MODEL_RAW_DATA.containsKey(type)) {
            throw new IllegalArgumentException("Factory for type " + type + " already registered");
        }
        ModelLoadingRegistry.INSTANCE.registerVariantProvider(manager -> new StitchModelVariantProvider(type, factory));
        MODEL_RAW_DATA.put(type, new HashMap<>());
    }

    public static boolean hasType(ResourceLocation type) {
        return MODEL_RAW_DATA.containsKey(type);
    }

    public static void addData(ResourceLocation type, ResourceLocation id, JsonObject data) {
        MODEL_RAW_DATA.computeIfPresent(type, (k, v) -> {
            v.put(id, data);
            return v;
        });
    }

    public static JsonObject getData(ResourceLocation type, ResourceLocation id) {
        if (getter == null) return null;
        List<ModelBakery.LoadedJson> loadedJsons = getter.apply(new ResourceLocation(id.getNamespace(), "blockstates/" + id.getPath() + ".json"));
        if (loadedJsons == null || loadedJsons.isEmpty()) return null;
        for (ModelBakery.LoadedJson loadedJson : loadedJsons) {
            if (loadedJson.data() instanceof JsonObject json) {
                if (json.get("variants") instanceof JsonObject variants) {
                    for (var entry : variants.entrySet()) {
                        boolean valid = entry.getKey().equals("");
                        if (!valid) {
                            try {
                                ModelResourceLocation modelId = new ModelResourceLocation(id, entry.getKey());
                                if (id.equals(modelId)) valid = true;
                            } catch (Exception ignored){}
                        }

                        if (valid && variants.get(entry.getKey()) instanceof JsonObject variant) {
                            String model = GsonHelper.getAsString(variant, "model", "");
                            var output = MODEL_RAW_DATA.get(type).get(ResourceLocation.tryParse(model));
                            if (output != null) return output;
                        }
                    }
                }
            }
        }
        return null;
    }

    @ApiStatus.Internal
    public static void setGetter(Function<ResourceLocation, List<ModelBakery.LoadedJson>> getter) {
        FactoryManagerImpl.getter = getter;
    }
}
