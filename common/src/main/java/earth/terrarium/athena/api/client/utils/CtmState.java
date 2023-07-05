package earth.terrarium.athena.api.client.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

public record CtmState(
        boolean up, boolean down,
        boolean left, boolean right,
        boolean upLeft, boolean upRight,
        boolean downLeft, boolean downRight
) {

    public static CtmState from(AppearanceAndTintGetter level, BlockPos pos, Direction direction, Predicate<BlockState> check) {
        final BlockPos upPos = AthenaUtils.getFacingPos(pos, direction, AthenaUtils.UrMom.UP);
        final BlockPos downPos = AthenaUtils.getFacingPos(pos, direction, AthenaUtils.UrMom.DOWN);

        final boolean up = check.test(level.getAppearance(upPos, direction));
        final boolean down = check.test(level.getAppearance(downPos, direction));
        final boolean left = check.test(level.getAppearance(AthenaUtils.getFacingPos(pos, direction, AthenaUtils.UrMom.LEFT), direction));
        final boolean right = check.test(level.getAppearance(AthenaUtils.getFacingPos(pos, direction, AthenaUtils.UrMom.RIGHT), direction));

        final boolean upLeft = check.test(level.getAppearance(AthenaUtils.getFacingPos(upPos, direction, AthenaUtils.UrMom.LEFT), direction));
        final boolean upRight = check.test(level.getAppearance(AthenaUtils.getFacingPos(upPos, direction, AthenaUtils.UrMom.RIGHT), direction));
        final boolean downLeft = check.test(level.getAppearance(AthenaUtils.getFacingPos(downPos, direction, AthenaUtils.UrMom.LEFT), direction));
        final boolean downRight = check.test(level.getAppearance(AthenaUtils.getFacingPos(downPos, direction, AthenaUtils.UrMom.RIGHT), direction));

        return new CtmState(up, down, left, right, upLeft, upRight, downLeft, downRight);
    }

    public boolean allTrue() {
        return up && down && right && left && upRight && upLeft && downRight && downLeft;
    }
}