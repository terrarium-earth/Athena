package earth.terrarium.athena.api.client.utils;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.AthenaModelType;
import earth.terrarium.athena.api.client.models.FactoryManager;
import earth.terrarium.athena.impl.internal.AthenaUnbakedModel;
import earth.terrarium.athena.impl.loading.AthenaResourceLoader;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.util.function.BiFunction;
import java.util.function.Function;

@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "26.4")
public class AthenaUnbakedModelLoader {

    public static final Logger LOGGER = LogUtils.getLogger();

    private final AthenaModelType type;

    public AthenaUnbakedModelLoader(Identifier id, AthenaModelType type, BiFunction<AthenaBlockModel, Function<Material, Material.Baked>, BlockStateModel> baker) {
        this.type = type;
    }

    public Identifier id() {
        return FactoryManager.getTypeId(type);
    }

    public BlockStateModel bake(AthenaUnbakedModel model, @NonNull ModelBaker baker) {
        return model.bake(Blocks.AIR.defaultBlockState(), baker);
    }

    public @Nullable AthenaUnbakedModel loadModel(BlockState state) {
        JsonObject json = AthenaResourceLoader.getData(state);

        if (json == null) {
            return null;
        }

        var blockModel = type.codec()
            .codec()
            .parse(JsonOps.INSTANCE, json)
            .result()
            .orElse(null);

        if (blockModel == null) {
            return null;
        }

        return AthenaResourceLoader.SERVICE.load(state, blockModel);
    }

    @SuppressWarnings("unchecked")
    public MapCodec<AthenaUnbakedModel> codec() {
        return ((MapCodec<AthenaBlockModel>) type.codec())
            .xmap((model) -> AthenaResourceLoader.SERVICE.load(Blocks.AIR.defaultBlockState(), model), AthenaUnbakedModel::model);
    }
}
