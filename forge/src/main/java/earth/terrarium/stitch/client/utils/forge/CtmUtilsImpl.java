package earth.terrarium.stitch.client.utils.forge;

import earth.terrarium.stitch.client.utils.StitchUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;

public class CtmUtilsImpl {
    public static Rotation getPillarRotation(Direction.Axis axis, Direction direction) {
        if (axis == Direction.Axis.X) {
            if (direction.getAxis().isVertical()) {
                return Rotation.COUNTERCLOCKWISE_90;
            } else {
                return StitchUtils.ternary(direction.getAxisDirection(), Rotation.CLOCKWISE_90, Rotation.COUNTERCLOCKWISE_90);
            }
        } else if (axis == Direction.Axis.Z) {
            if (direction.getAxis().isVertical()) {
                return StitchUtils.ternary(direction.getAxisDirection(), Rotation.CLOCKWISE_180, Rotation.NONE);
            } else {
                return StitchUtils.ternary(direction.getAxisDirection(), Rotation.COUNTERCLOCKWISE_90, Rotation.CLOCKWISE_90);
            }
        }
        return Rotation.NONE;
    }
}
