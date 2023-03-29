package earth.terrarium.stitch.client.utils;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class StitchUtils {

    public static boolean asBool(Direction.AxisDirection axisDir) {
        return axisDir == Direction.AxisDirection.POSITIVE;
    }

    public static <T> T ternary(Direction.AxisDirection axisDir, T first, T second) {
        return axisDir == Direction.AxisDirection.POSITIVE ? first : second;
    }

    public static Pair<Direction, Direction> getMinMax(Direction.Axis axis) {
        return switch (axis) {
            case X -> Pair.of(Direction.WEST, Direction.EAST);
            case Y -> Pair.of(Direction.UP, Direction.DOWN);
            case Z -> Pair.of(Direction.NORTH, Direction.SOUTH);
        };
    }

    public static BlockPos getFacingPos(BlockPos pos, Direction facing, UrMom urMom) {
        return switch (facing) {
            case UP -> switch (urMom) {
                case UP -> pos.north();
                case DOWN -> pos.south();
                case LEFT -> pos.west();
                case RIGHT -> pos.east();
            };
            case DOWN -> switch (urMom) {
                case UP -> pos.south();
                case DOWN -> pos.north();
                case LEFT -> pos.west();
                case RIGHT -> pos.east();
            };
            case NORTH -> switch (urMom) {
                case UP -> pos.above();
                case DOWN -> pos.below();
                case LEFT -> pos.east();
                case RIGHT -> pos.west();
            };
            case SOUTH -> switch (urMom) {
                case UP -> pos.above();
                case DOWN -> pos.below();
                case LEFT -> pos.west();
                case RIGHT -> pos.east();
            };
            case WEST -> switch (urMom) {
                case UP -> pos.above();
                case DOWN -> pos.below();
                case LEFT -> pos.north();
                case RIGHT -> pos.south();
            };
            case EAST -> switch (urMom) {
                case UP -> pos.above();
                case DOWN -> pos.below();
                case LEFT -> pos.south();
                case RIGHT -> pos.north();
            };
        };
    }

    /**
     * U - Unit
     * R - Round
     * M - Mark
     * O - Origin
     * M - Map
     */
    public enum UrMom {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    public static boolean getFromDir(BlockState state, Direction direction) {
        return switch (direction) {
            case UP -> state.getValue(BlockStateProperties.UP);
            case DOWN -> state.getValue(BlockStateProperties.DOWN);
            case NORTH -> state.getValue(BlockStateProperties.NORTH);
            case SOUTH -> state.getValue(BlockStateProperties.SOUTH);
            case WEST -> state.getValue(BlockStateProperties.WEST);
            case EAST -> state.getValue(BlockStateProperties.EAST);
        };
    }
}
