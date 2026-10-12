package earth.terrarium.athena.api.client.utils;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.AthenaModelType;
import earth.terrarium.athena.impl.internal.AthenaUnbakedModel;
import earth.terrarium.athena.impl.loading.AthenaResourceLoader;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.function.BiFunction;
import java.util.function.Function;

public class AthenaUnbakedModelLoader {

    private final Identifier id;
    private final AthenaModelType type;
    private final BiFunction<AthenaBlockModel, Function<Material, Material.Baked>, BlockStateModel> baker;

    public AthenaUnbakedModelLoader(Identifier id, AthenaModelType type, BiFunction<AthenaBlockModel, Function<Material, Material.Baked>, BlockStateModel> baker) {
        this.id = id;
        this.type = type;
        this.baker = baker;
    }

    public Identifier id() {
        return this.id;
    }

    public BlockStateModel bake(AthenaUnbakedModel model, @NonNull ModelBaker baker) {
        return this.baker.apply(model.model(), material -> baker.materials().get(material, () -> "Athena: ?"));
    }

    public @Nullable AthenaUnbakedModel loadModel(BlockState state) {
        var id = state.getBlock().builtInRegistryHolder().key().identifier();
        JsonObject json = AthenaResourceLoader.getData(this.id, id);

        if (json == null) {
            return null;
        }

        return codec()
            .codec()
            .parse(JsonOps.INSTANCE, json)
            .result()
            .orElse(null);
    }

    @SuppressWarnings("unchecked")
    public MapCodec<AthenaUnbakedModel> codec() {
        return ((MapCodec<AthenaBlockModel>) type.codec()).xmap((model) -> new AthenaUnbakedModel(model, this), AthenaUnbakedModel::model);
    }
}
