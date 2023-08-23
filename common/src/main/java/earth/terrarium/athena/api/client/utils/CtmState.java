package earth.terrarium.athena.api.client.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public record CtmState(
        boolean up, boolean down,
        boolean left, boolean right,
        boolean upLeft, boolean upRight,
        boolean downLeft, boolean downRight
) {

    public static CtmState from(AppearanceAndTintGetter level, BlockState state, BlockPos pos, Direction direction, ConnectionCheck check) {
        final BlockPos upPos = AthenaUtils.getFacingPos(pos, direction, AthenaUtils.UrMom.UP);
        final BlockPos downPos = AthenaUtils.getFacingPos(pos, direction, AthenaUtils.UrMom.DOWN);

        final boolean up = ConnectionCheck.test(check, level, state, pos, upPos, direction);
        final boolean down = ConnectionCheck.test(check, level, state, pos, downPos, direction);
        final boolean left = ConnectionCheck.test(check, level, state, pos, AthenaUtils.getFacingPos(pos, direction, AthenaUtils.UrMom.LEFT), direction);
        final boolean right = ConnectionCheck.test(check, level, state, pos, AthenaUtils.getFacingPos(pos, direction, AthenaUtils.UrMom.RIGHT), direction);

        final boolean upLeft = ConnectionCheck.test(check, level, state, pos, AthenaUtils.getFacingPos(upPos, direction, AthenaUtils.UrMom.LEFT), direction);
        final boolean upRight = ConnectionCheck.test(check, level, state, pos, AthenaUtils.getFacingPos(upPos, direction, AthenaUtils.UrMom.RIGHT), direction);
        final boolean downLeft = ConnectionCheck.test(check, level, state, pos, AthenaUtils.getFacingPos(downPos, direction, AthenaUtils.UrMom.LEFT), direction);
        final boolean downRight = ConnectionCheck.test(check, level, state, pos, AthenaUtils.getFacingPos(downPos, direction, AthenaUtils.UrMom.RIGHT), direction);

        return new CtmState(up, down, left, right, upLeft, upRight, downLeft, downRight);
    }

    public boolean allTrue() {
        return up && down && right && left && upRight && upLeft && downRight && downLeft;
    }

    @FunctionalInterface
    public interface ConnectionCheck {
        boolean test(BlockPos pos, BlockState state, BlockState appearanceState);

        static boolean test(ConnectionCheck check, AppearanceAndTintGetter level, BlockState fromState, BlockPos fromPos, BlockPos pos, Direction direction) {
            final AppearanceAndTintGetter.Query query = level.query(pos, direction, fromState, fromPos);
            if (query.appearance().isAir()) return false;
            return check.test(pos, query.state(), query.appearance());
        }
    }
}