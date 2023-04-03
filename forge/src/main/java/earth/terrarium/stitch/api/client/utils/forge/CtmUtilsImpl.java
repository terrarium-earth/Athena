package earth.terrarium.stitch.api.client.utils.forge;

import earth.terrarium.stitch.api.client.utils.StitchUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;

public class CtmUtilsImpl {
    public static Rotation getPillarRotation(Direction.Axis axis, Direction direction) {
        if (axis == Direction.Axis.X) {
            return StitchUtils.ternary(direction.getAxisDirection(), Rotation.CLOCKWISE_90, Rotation.COUNTERCLOCKWISE_90);
        } else if (axis == Direction.Axis.Z && direction.getAxis().isHorizontal()) {
            return StitchUtils.ternary(direction.getAxisDirection(), Rotation.COUNTERCLOCKWISE_90, Rotation.CLOCKWISE_90);
        }
        return Rotation.NONE;
    }
}
