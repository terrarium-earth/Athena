package earth.terrarium.stitch.client.forge;

import earth.terrarium.stitch.client.models.StitchQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.core.Direction;

public class StitchBlockElementFace extends BlockElementFace {

    public StitchBlockElementFace(StitchQuad quad, Direction direction, BlockElement element) {
        super(direction, -1, "", new BlockFaceUV(getUVs(element, direction), (int)((quad.rotation().ordinal() * 90f) % 360f)
        ));
    }

    private static float[] getUVs(BlockElement element, Direction direction) {
        float[] uvs = switch (direction) {
            case UP -> new float[] { element.from.x(), element.to.z(), element.to.x(), element.from.z() };
            case DOWN -> new float[]{ element.from.x(), element.from.z(), element.to.x(), element.to.z() };
            case NORTH -> new float[]{ element.from.x, 16 - element.to.y, element.to.x, 16 - element.from.y };
            case SOUTH -> new float[] { 16 - element.to.x, 16 - element.to.y, 16 - element.from.x, 16 - element.from.y };
            case WEST -> new float[]{ 16.0F - element.to.z(), 16.0F - element.to.y(), 16.0F - element.from.z(), 16.0F - element.from.y() };
            case EAST -> new float[]{element.from.z(), 16.0F - element.to.y(), element.to.z(), 16.0F - element.from.y()};
        };
        return new float[] { uvs[0], uvs[1], uvs[2], uvs[3] };
    }
}
