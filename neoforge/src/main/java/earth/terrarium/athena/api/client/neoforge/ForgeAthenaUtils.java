package earth.terrarium.athena.api.client.neoforge;

import earth.terrarium.athena.api.client.models.AthenaQuad;
import earth.terrarium.athena.api.client.models.TintProvider;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.UnbakedElementsHelper;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;

@ApiStatus.Internal
public class ForgeAthenaUtils {

    public static List<BakedQuad> bakeQuad(AthenaQuad quad, Direction direction, TextureAtlasSprite sprite, TintProvider tint) {
        final Vector3f start = getStartPos(quad, direction);
        final Vector3f end = getEndPos(quad, direction);
        final BlockElementFace face = AthenaBlockElementFace.of(quad, direction, start, end, tint);
        final BlockElement element = new BlockElement(start, end, Map.of(direction.getOpposite(), face));
        return UnbakedElementsHelper.bakeElements(
                List.of(element),
                mat -> sprite,
                BlockModelRotation.X0_Y0
        );
    }

    private static Vector3f getStartPos(AthenaQuad quad, Direction direction) {
        return switch (direction) {
            case NORTH -> new Vector3f((1 - quad.right()) * 16f, quad.top() * 16f, quad.depth() * 16f);
            case SOUTH -> new Vector3f(quad.left() * 16f, quad.top() * 16f, (1-quad.depth()) * 16f);
            case WEST -> new Vector3f(quad.depth() * 16f, quad.top() * 16f, quad.left() * 16f);
            case EAST -> new Vector3f((1 - quad.depth()) * 16f, quad.top() * 16f,  (1 - quad.right()) * 16f);
            case DOWN -> new Vector3f(quad.left() * 16f, quad.depth() * 16f, quad.top() * 16f);
            case UP -> new Vector3f(quad.left() * 16f, (1 - quad.depth()) * 16f, (1 - quad.bottom()) * 16f);
        };
    }

    private static Vector3f getEndPos(AthenaQuad quad, Direction direction) {
        return switch (direction) {
            case NORTH -> new Vector3f((1 - quad.left()) * 16f, quad.bottom() * 16f, quad.depth() * 16f);
            case SOUTH -> new Vector3f(quad.right() * 16f, quad.bottom() * 16f, (1 - quad.depth()) * 16f);
            case WEST -> new Vector3f(quad.depth() * 16f, quad.bottom() * 16f, quad.right() * 16f);
            case EAST -> new Vector3f((1 - quad.depth()) * 16f, quad.bottom() * 16f, (1 - quad.left()) * 16f);
            case DOWN -> new Vector3f(quad.right() * 16f, quad.depth() * 16f, quad.bottom() * 16f);
            case UP -> new Vector3f(quad.right() * 16f, quad.depth() * 16f, (1 - quad.top()) * 16f);
        };
    }
}
