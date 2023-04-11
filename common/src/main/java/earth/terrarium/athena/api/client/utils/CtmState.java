package earth.terrarium.athena.api.client.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

public record CtmState(
        boolean up, boolean down,
        boolean left, boolean right,
        boolean upLeft, boolean upRight,
        boolean downLeft, boolean downRight
) {

    public static CtmState from(BlockAndTintGetter level, BlockPos pos, Direction direction, Predicate<BlockState> check) {
        final BlockPos upPos = AthenaUtils.getFacingPos(pos, direction, AthenaUtils.UrMom.UP);
        final BlockPos downPos = AthenaUtils.getFacingPos(pos, direction, AthenaUtils.UrMom.DOWN);

        final boolean up = check.test(level.getBlockState(upPos));
        final boolean down = check.test(level.getBlockState(downPos));
        final boolean left = check.test(level.getBlockState(AthenaUtils.getFacingPos(pos, direction, AthenaUtils.UrMom.LEFT)));
        final boolean right = check.test(level.getBlockState(AthenaUtils.getFacingPos(pos, direction, AthenaUtils.UrMom.RIGHT)));

        final boolean upLeft = check.test(level.getBlockState(AthenaUtils.getFacingPos(upPos, direction, AthenaUtils.UrMom.LEFT)));
        final boolean upRight = check.test(level.getBlockState(AthenaUtils.getFacingPos(upPos, direction, AthenaUtils.UrMom.RIGHT)));
        final boolean downLeft = check.test(level.getBlockState(AthenaUtils.getFacingPos(downPos, direction, AthenaUtils.UrMom.LEFT)));
        final boolean downRight = check.test(level.getBlockState(AthenaUtils.getFacingPos(downPos, direction, AthenaUtils.UrMom.RIGHT)));

        return new CtmState(up, down, left, right, upLeft, upRight, downLeft, downRight);
    }

    public boolean allTrue() {
        return up && down && right && left && upRight && upLeft && downRight && downLeft;
    }
}