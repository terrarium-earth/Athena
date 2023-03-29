package earth.terrarium.stitch.client.forge;

import earth.terrarium.stitch.client.models.StitchQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

import java.util.Map;

public class StitchBlockElement extends BlockElement {

    public StitchBlockElement(StitchQuad quad, Direction direction) {
        super(getStartPos(quad, direction), getEndPos(quad, direction), Map.of(), null, true);
    }

    private static Vector3f getStartPos(StitchQuad quad, Direction direction) {
        return switch (direction) {
            case NORTH -> new Vector3f(quad.left() * 16f, quad.top() * 16f, quad.depth() * 16f);
            case SOUTH -> new Vector3f(quad.left() * 16f, quad.top() * 16f, (1-quad.depth()) * 16f);
            case WEST -> new Vector3f(quad.depth() * 16f, quad.top() * 16f, quad.left() * 16f);
            case EAST -> new Vector3f((1 - quad.depth()) * 16f, quad.top() * 16f, quad.left() * 16f);
            case DOWN -> new Vector3f(quad.left() * 16f, quad.depth() * 16f, quad.top() * 16f);
            case UP -> new Vector3f(quad.left() * 16f, (1 - quad.depth()) * 16f, quad.top() * 16f);
        };
    }

    private static Vector3f getEndPos(StitchQuad quad, Direction direction) {
        return switch (direction) {
            case NORTH -> new Vector3f(quad.right() * 16f, quad.bottom() * 16f, quad.depth() * 16f);
            case SOUTH -> new Vector3f(quad.right() * 16f, quad.bottom() * 16f, (1 - quad.depth()) * 16f);
            case WEST -> new Vector3f(quad.depth() * 16f, quad.bottom() * 16f, quad.right() * 16f);
            case EAST -> new Vector3f((1 - quad.depth()) * 16f, quad.bottom() * 16f, quad.right() * 16f);
            case DOWN -> new Vector3f(quad.right() * 16f, quad.depth() * 16f, quad.bottom() * 16f);
            case UP -> new Vector3f(quad.right() * 16f, quad.depth() * 16f, quad.bottom() * 16f);
        };
    }
}
