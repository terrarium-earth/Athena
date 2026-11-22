package earth.terrarium.athena.impl.platform;

import earth.terrarium.athena.api.client.fabric.AthenaBakedModel;
import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.fabric.client.AthenaCustomUnbakedBlockStateModel;
import earth.terrarium.athena.impl.internal.AthenaUnbakedModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ModelLoaderServiceFabricImpl implements ModelLoaderService {

    @Override
    public AthenaUnbakedModel load(BlockState state, AthenaBlockModel model) {
        return AthenaBakedModel.unbaked(state, model);
    }

    @Override
    public BlockStateModel.Unbaked createUnbaked(AthenaBlockModel model) {
        return new AthenaCustomUnbakedBlockStateModel(load(Blocks.AIR.defaultBlockState(), model));
    }
}
