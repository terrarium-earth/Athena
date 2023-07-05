package earth.terrarium.athena.api.client.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface AppearanceAndTintGetter extends BlockAndTintGetter {

    BlockState getAppearance(BlockPos pos, Direction direction);
}
