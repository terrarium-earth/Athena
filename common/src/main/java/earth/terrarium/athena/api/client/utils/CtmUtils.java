package earth.terrarium.athena.api.client.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiPredicate;

public final class CtmUtils {

    public static int getTexture(boolean first, boolean second, boolean firstSecond) {
        if (first && second) {
            return firstSecond ? 1 : 2;
        }
        return first ? 3 : second ? 4 : 0;
    }

    public static Rotation getPillarRotation(Direction.Axis axis, Direction direction) {
        if (axis == Direction.Axis.X) {
            return direction.getAxis().isHorizontal() && !AthenaUtils.asBool(direction.getAxisDirection()) ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90;
        } else if (axis == Direction.Axis.Z) {
            if (direction.getAxis().isVertical()) {
                return AthenaUtils.ternary(direction.getAxisDirection(), Rotation.NONE, Rotation.CLOCKWISE_180);
            } else {
                return AthenaUtils.ternary(direction.getAxisDirection(), Rotation.CLOCKWISE_90, Rotation.COUNTERCLOCKWISE_90);
            }
        }
        return Rotation.NONE;
    }

    public static CtmState.ConnectionCheck check(AppearanceAndTintGetter level, BlockState state, BlockPos pos, Direction direction, BiPredicate<BlockState, BlockState> predicate) {
        return (fromPos, fromState, fromAppearance) -> predicate.test(level.getAppearance(state, pos, direction, fromState, fromPos), fromAppearance);
    }

    public static boolean checkRelative(AppearanceAndTintGetter level, BlockState state, BlockPos pos, Direction direction) {
        BlockPos relativePos = pos.relative(direction);
        BlockState otherState = level.getBlockState(relativePos);
        BlockState stateAppearance = level.getAppearance(state, pos, direction, otherState, relativePos);
        BlockState otherStateAppearance = level.getAppearance(otherState, relativePos, direction.getOpposite(), state, pos);
        return !stateAppearance.isAir() && otherStateAppearance.is(stateAppearance.getBlock());
    }
}
