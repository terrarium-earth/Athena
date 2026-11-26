package earth.terrarium.athena.neoforge.client;

import com.mojang.serialization.MapCodec;
import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.neoforge.AthenaBakedModel;
import earth.terrarium.athena.impl.internal.AthenaUnbakedModel;
import earth.terrarium.athena.impl.loading.AthenaResourceLoader;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.jetbrains.annotations.NotNull;

public record AthenaCustomUnbakedBlockStateModel(
    @NotNull AthenaUnbakedModel root
) implements CustomUnbakedBlockStateModel {
    public static final MapCodec<AthenaCustomUnbakedBlockStateModel> CODEC = AthenaResourceLoader.CODEC_FOR_BLOCK_STATES.xmap(
        (model) -> new AthenaCustomUnbakedBlockStateModel(AthenaBakedModel.unbaked(Blocks.AIR.defaultBlockState(), model)),
        (model) -> model.root().model()
    );

    @Override
    public @NotNull MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return CODEC;
    }

    @Override
    public @NotNull BlockStateModel bake(@NotNull ModelBaker baker) {
        return root.bake(Blocks.AIR.defaultBlockState(), baker);
    }

    @Override
    public void resolveDependencies(@NotNull Resolver arg) {

    }

    @Override
    public BlockStateModel.UnbakedRoot asRoot() {
        return this.root;
    }
}
