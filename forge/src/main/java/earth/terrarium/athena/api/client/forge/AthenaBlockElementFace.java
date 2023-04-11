package earth.terrarium.athena.api.client.forge;

import earth.terrarium.athena.api.client.models.AthenaQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

public class AthenaBlockElementFace extends BlockElementFace {

    public AthenaBlockElementFace(AthenaQuad quad, Direction direction, Vector3f start, Vector3f end) {
        super(quad.depth() == 0 ? direction : null, -1, "", new BlockFaceUV(getUVs(start, end, direction), (int)((quad.rotation().ordinal() * 90f) % 360f)
        ));
    }

    private static float[] getUVs(Vector3f from, Vector3f to, Direction direction) {
        float[] uvs = switch (direction) {
            case UP -> new float[] { from.x(), to.z(), to.x(), from.z() };
            case DOWN -> new float[]{ from.x(), from.z(), to.x(), to.z() };
            case NORTH -> new float[]{ from.x, 16 - to.y, to.x, 16 - from.y };
            case SOUTH -> new float[] { 16 - to.x, 16 - to.y, 16 - from.x, 16 - from.y };
            case WEST -> new float[]{ 16.0F - to.z(), 16.0F - to.y(), 16.0F - from.z(), 16.0F - from.y() };
            case EAST -> new float[]{from.z(), 16.0F - to.y(), to.z(), 16.0F - from.y()};
        };
        return new float[] { uvs[0], uvs[1], uvs[2], uvs[3] };
    }
}
