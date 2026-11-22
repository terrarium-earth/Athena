package earth.terrarium.athena.impl.platform;

import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.neoforge.AthenaBakedModel;
import earth.terrarium.athena.impl.internal.AthenaUnbakedModel;
import earth.terrarium.athena.neoforge.client.AthenaCustomUnbakedBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ModelLoaderServiceNeoForgeImpl implements ModelLoaderService {

    @Override
    public AthenaUnbakedModel load(BlockState state, AthenaBlockModel model) {
        return AthenaBakedModel.unbaked(state, model);
    }

    @Override
    public BlockStateModel.Unbaked createUnbaked(AthenaBlockModel model) {
        return new AthenaCustomUnbakedBlockStateModel(load(Blocks.AIR.defaultBlockState(), model));
    }
}
