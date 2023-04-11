package earth.terrarium.athena.api.client.utils.fabric;

import earth.terrarium.athena.api.client.utils.AthenaUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;

public class CtmUtilsImpl {
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
}
