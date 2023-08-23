package earth.terrarium.athena.api.client.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface AppearanceAndTintGetter extends BlockAndTintGetter {

    BlockState getAppearance(BlockState state, BlockPos pos, Direction direction, BlockState fromState, BlockPos fromPos);

    @Deprecated
    BlockState getAppearance(BlockPos pos, Direction direction);

    BlockState getAppearance(BlockPos pos, Direction direction, BlockState fromState, BlockPos fromPos);

    Query query(BlockPos pos, Direction direction, BlockState fromState, BlockPos fromPos);

    record Query(BlockState state, BlockState appearance){}
}
