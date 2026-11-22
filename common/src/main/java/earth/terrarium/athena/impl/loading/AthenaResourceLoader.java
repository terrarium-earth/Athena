package earth.terrarium.athena.impl.loading;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.AthenaModelType;
import earth.terrarium.athena.api.client.models.FactoryManager;
import earth.terrarium.athena.impl.client.DefaultModels;
import earth.terrarium.athena.impl.internal.AthenaUnbakedModel;
import earth.terrarium.athena.impl.platform.ModelLoaderService;
import net.minecraft.Optionull;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AthenaResourceLoader extends SimpleJsonResourceReloadListener<@NotNull JsonElement> {

    public static final ModelLoaderService SERVICE = ModelLoaderService.create();

    public static final String KEY = DefaultModels.MODID + ":loader";

    private static final Codec<AthenaBlockModel> CODEC_FOR_BLOCK_STATES = Identifier.CODEC
        .comapFlatMap(
            it -> Optionull.mapOrElse(getTypeForBlockStates(it), DataResult::success, () -> DataResult.error(() -> "Unknown loader: " + it)),
            FactoryManager::getTypeId
        )
        .dispatch(
            KEY,
            AthenaBlockModel::type,
            AthenaModelType::codec
        );

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final AthenaResourceLoader INSTANCE = new AthenaResourceLoader();

    private final Map<Identifier, JsonObject> blockstateData = new ConcurrentHashMap<>();
    private final Map<Identifier, JsonElement> data = new HashMap<>();

    public AthenaResourceLoader() {
        super(ExtraCodecs.JSON, FileToIdConverter.json("athena"));
    }

    public static void clearBlockstateData() {
        INSTANCE.blockstateData.clear();
    }

    public static void addBlockstateData(Identifier stateId, JsonElement data) {
        if (!(data instanceof JsonObject object)) return;
        if (!object.has(KEY)) return;
        INSTANCE.blockstateData.put(stateId, object);
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> object, @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
        this.data.clear();
        this.data.putAll(object);
    }

    private static AthenaUnbakedModel tryLoad(
        JsonElement json,
        BlockState state,
        Codec<AthenaBlockModel> codec
    ) {
        AthenaBlockModel model = codec
            .parse(JsonOps.INSTANCE, json)
            .ifError((error) -> LOGGER.error("Failed to load Athena model for {}. {}", state, error))
            .result()
            .orElse(null);

        if (model == null) {
            return null;
        }

        return SERVICE.load(state, model);
    }

    public static AthenaUnbakedModel tryLoadModel(BlockState state) {
        var id = state.getBlock().builtInRegistryHolder().key().identifier();

        var modelData = INSTANCE.data.get(id);

        if (modelData != null) {
            return tryLoad(modelData, state, AthenaBlockModel.CODEC.codec());
        }

        JsonObject blockStateData = INSTANCE.blockstateData.get(id);

        if (blockStateData != null) {
            return tryLoad(blockStateData, state, CODEC_FOR_BLOCK_STATES);
        }

        return null;
    }

    private static JsonObject checkObject(Identifier modelType, JsonElement data) {
        if (data instanceof JsonObject object) {
            String type = GsonHelper.getAsString(object, KEY, "");
            if (modelType.toString().equals(type)) {
                return object;
            }
        }

        return null;
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "26.4")
    public static JsonObject getData(BlockState state) {
        var id = state.getBlock().builtInRegistryHolder().key().identifier();

        var modelData = INSTANCE.data.get(id);

        if (modelData != null) {
            return checkObject(id, modelData);
        }

        JsonObject blockStateData = INSTANCE.blockstateData.get(id);

        if (blockStateData != null) {
            return checkObject(id, blockStateData);
        }

        return null;
    }

    private static AthenaModelType getTypeForBlockStates(Identifier id) {
        AthenaModelType type = FactoryManager.getType(id);

        if (!type.allowedInBlockStates()) {
            return null;
        }

        return type;
    }
}
