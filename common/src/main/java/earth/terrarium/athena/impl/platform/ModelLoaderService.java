package earth.terrarium.athena.impl.platform;

import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.impl.internal.AthenaUnbakedModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@PlatformService
public interface ModelLoaderService {

    AthenaUnbakedModel load(BlockState state, AthenaBlockModel model);
    BlockStateModel.Unbaked createUnbaked(AthenaBlockModel model);

    static ModelLoaderService create() {
        throw new AssertionError("Failed to create ModelLoaderService, no implementation found");
    }
}
