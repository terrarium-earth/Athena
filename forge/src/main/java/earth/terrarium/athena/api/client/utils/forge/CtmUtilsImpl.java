package earth.terrarium.athena.api.client.utils.forge;

import earth.terrarium.athena.api.client.utils.AthenaUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;

public class CtmUtilsImpl {
    public static Rotation getPillarRotation(Direction.Axis axis, Direction direction) {
        if (axis == Direction.Axis.X) {
            if (direction.getAxis() == Direction.Axis.Y) {
                return Rotation.CLOCKWISE_90;
            }
            return AthenaUtils.ternary(direction.getAxisDirection(), Rotation.CLOCKWISE_90, Rotation.COUNTERCLOCKWISE_90);
        } else if (axis == Direction.Axis.Z) {
            if (direction.getAxis() == Direction.Axis.Y) {
                return AthenaUtils.ternary(direction.getAxisDirection(), Rotation.NONE, Rotation.CLOCKWISE_180);
            }
            return AthenaUtils.ternary(direction.getAxisDirection(), Rotation.COUNTERCLOCKWISE_90, Rotation.CLOCKWISE_90);
        }
        return Rotation.NONE;
    }
}
