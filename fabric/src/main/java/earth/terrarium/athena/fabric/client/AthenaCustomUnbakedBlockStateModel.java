package earth.terrarium.athena.fabric.client;

import com.mojang.serialization.MapCodec;
import earth.terrarium.athena.api.client.models.FactoryManager;
import earth.terrarium.athena.impl.client.DefaultModels;
import earth.terrarium.athena.impl.internal.AthenaUnbakedModel;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

public record AthenaCustomUnbakedBlockStateModel(
        @NotNull AthenaUnbakedModel root
) implements CustomUnbakedBlockStateModel {
    public static final MapCodec<AthenaCustomUnbakedBlockStateModel> CODEC = FactoryManager.codec()
        .dispatchMap(
            DefaultModels.MODID + ":loader",
            (stateModel) -> stateModel.root().loader(),
            (loader) -> loader.codec().xmap(AthenaCustomUnbakedBlockStateModel::new, AthenaCustomUnbakedBlockStateModel::root)
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
    public void resolveDependencies(@NotNull ResolvableModel.Resolver arg) {

    }
}
